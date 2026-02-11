package com.shangnantea.tools.datagen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.shangnantea.security.util.PasswordEncoder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.*;

/**
 * 数据生成 GUI 应用：在独立窗口中选择表和数量，然后调用 DataGeneratorEngine 生成数据。
 *
 * 仅用于本地/开发环境，不对外暴露。
 */
public class DataGeneratorGuiApp extends JFrame {

    private static final Path RULES_PATH = Path.of("data-generator-rules.yml");

    private record DataSourceConfig(String url, String username, String password) {}

    private final DataGenConfig config;
    private final Map<String, TableRule> tableRules;

    private final Map<String, JCheckBox> tableChecks = new LinkedHashMap<>();
    private final Map<String, JTextField> tableCounts = new LinkedHashMap<>();
    private final JTextArea logArea = new JTextArea();
    private final JButton runButton = new JButton("开始生成");

    public DataGeneratorGuiApp(DataGenConfig config) {
        super("商南茶 - 测试数据生成工具");
        this.config = config;
        this.tableRules = config.getTables();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        initUi();
    }

    private void initUi() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 顶部说明
        JLabel tip = new JLabel("请选择要生成数据的表，并设置每个表的生成数量：");
        root.add(tip, BorderLayout.NORTH);

        // 中间：表列表（左侧勾选 + 数量输入）
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        for (Map.Entry<String, TableRule> e : tableRules.entrySet()) {
            String tableName = e.getKey();
            TableRule rule = e.getValue();
            int defCount = rule.getDefaultCount() > 0 ? rule.getDefaultCount() : 100;

            JCheckBox cb = new JCheckBox(tableName);
            cb.setSelected(false);
            JTextField countField = new JTextField(String.valueOf(defCount), 6);

            tableChecks.put(tableName, cb);
            tableCounts.put(tableName, countField);

            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row.add(cb);
            row.add(new JLabel("数量:"));
            row.add(countField);

            centerPanel.add(row);
        }

        JScrollPane tableScroll = new JScrollPane(centerPanel);
        tableScroll.setPreferredSize(new Dimension(860, 280));
        root.add(tableScroll, BorderLayout.CENTER);

        // 底部区域：按钮 + 日志
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton selectAllBtn = new JButton("全选");
        JButton clearAllBtn = new JButton("全不选");

        selectAllBtn.addActionListener(e -> tableChecks.values().forEach(cb -> cb.setSelected(true)));
        clearAllBtn.addActionListener(e -> tableChecks.values().forEach(cb -> cb.setSelected(false)));

        runButton.addActionListener(e -> runGeneration());

        btnPanel.add(selectAllBtn);
        btnPanel.add(clearAllBtn);
        btnPanel.add(runButton);

        bottomPanel.add(btnPanel, BorderLayout.NORTH);

        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(860, 200));

        bottomPanel.add(logScroll, BorderLayout.CENTER);

        root.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void appendLog(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg);
            if (!msg.endsWith("\n")) {
                logArea.append("\n");
            }
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void setControlsEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            runButton.setEnabled(enabled);
            tableChecks.values().forEach(cb -> cb.setEnabled(enabled));
            tableCounts.values().forEach(tf -> tf.setEnabled(enabled));
        });
    }

    private void runGeneration() {
        // 收集选中的表和数量
        Map<String, Integer> plan = new LinkedHashMap<>();
        for (String tableName : tableChecks.keySet()) {
            JCheckBox cb = tableChecks.get(tableName);
            if (!cb.isSelected()) continue;
            JTextField field = tableCounts.get(tableName);
            String text = field.getText().trim();
            int count;
            try {
                count = Integer.parseInt(text);
                if (count <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "表 " + tableName + " 的数量必须大于0",
                            "输入错误",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "表 " + tableName + " 的数量格式错误，请输入整数",
                        "输入错误",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            plan.put(tableName, count);
        }

        if (plan.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "请至少勾选一个要生成数据的表",
                    "提示",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        setControlsEnabled(false);
        appendLog("开始执行数据生成计划...");

        // 后台线程执行，避免卡住界面
        new Thread(() -> {
            try {
                DataSourceConfig ds = loadDataSourceConfig();
                try (Connection conn = DriverManager.getConnection(ds.url(), ds.username(), ds.password())) {
                    conn.setAutoCommit(false);

                    PasswordEncoder passwordEncoder = new PasswordEncoder();
                    DataGeneratorEngine engine = new DataGeneratorEngine(config, passwordEncoder);

                    for (Map.Entry<String, Integer> e : plan.entrySet()) {
                        String tableName = e.getKey();
                        int count = e.getValue();
                        appendLog("[GUI] 开始为表 " + tableName + " 生成 " + count + " 行数据...");
                        try {
                            engine.generateForTable(conn, tableName, count);
                            conn.commit();
                            appendLog("[GUI] 表 " + tableName + " 生成完成。");
                        } catch (Exception ex) {
                            conn.rollback();
                            appendLog("[GUI] 表 " + tableName + " 生成失败，已回滚本表事务。错误: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }
                }
                appendLog("[GUI] 所有选中表的数据生成流程结束。");
            } catch (Exception ex) {
                appendLog("[GUI] 数据生成过程发生异常: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                setControlsEnabled(true);
            }
        }, "DataGen-GUI-Worker").start();
    }

    /**
     * 复用 application.yml / application-local.yml 中的 spring.datasource 配置。
     * 基本沿用 DataGeneratorMain / DataGeneratorTui 的实现。
     */
    @SuppressWarnings("unchecked")
    private static DataSourceConfig loadDataSourceConfig() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        InputStream appIs = DataGeneratorGuiApp.class.getResourceAsStream("/application.yml");
        if (appIs == null) {
            throw new IllegalStateException("未找到 application.yml，无法加载数据源配置");
        }
        Map<String, Object> root = mapper.readValue(appIs, Map.class);
        Map<String, Object> spring = (Map<String, Object>) root.get("spring");
        if (spring == null) {
            throw new IllegalStateException("application.yml 中缺少 spring 配置");
        }
        Map<String, Object> datasource = (Map<String, Object>) spring.get("datasource");
        if (datasource == null) {
            throw new IllegalStateException("application.yml 中缺少 spring.datasource 配置");
        }

        String url = (String) datasource.get("url");
        String username = (String) datasource.get("username");
        String password = (String) datasource.get("password");

        // 覆盖密码：优先从 application-local.yml 读取
        InputStream localIs = DataGeneratorGuiApp.class.getResourceAsStream("/application-local.yml");
        if (localIs != null) {
            Map<String, Object> localRoot = mapper.readValue(localIs, Map.class);
            Map<String, Object> localSpring = (Map<String, Object>) localRoot.get("spring");
            if (localSpring != null) {
                Map<String, Object> localDs = (Map<String, Object>) localSpring.get("datasource");
                if (localDs != null && localDs.get("password") != null) {
                    password = String.valueOf(localDs.get("password"));
                }
            }
        }

        if (url == null || username == null) {
            throw new IllegalStateException("spring.datasource.url 或 username 为空，请检查 application.yml");
        }

        return new DataSourceConfig(url, username, password);
    }

    /**
     * 对外启动入口：由 Spring Boot StartupRunner 或手动 main 调用。
     */
    public static void startGui() throws IOException {
        if (!Files.exists(RULES_PATH)) {
            JOptionPane.showMessageDialog(null,
                    "未找到规则文件:\n" + RULES_PATH.toAbsolutePath(),
                    "数据生成规则缺失",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        DataGenConfig config = mapper.readValue(Files.newInputStream(RULES_PATH), DataGenConfig.class);
        if (config.getTables() == null || config.getTables().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "规则文件中未定义任何表",
                    "数据生成规则错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        EventQueue.invokeLater(() -> {
            DataGeneratorGuiApp app = new DataGeneratorGuiApp(config);
            app.setVisible(true);
        });
    }
}

