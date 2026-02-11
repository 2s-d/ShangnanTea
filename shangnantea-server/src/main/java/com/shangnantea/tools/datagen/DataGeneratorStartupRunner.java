package com.shangnantea.tools.datagen;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.awt.*;

/**
 * 在后端启动时提示是否运行数据生成 TUI 的启动器。
 *
 * 设计要点：
 * - 默认不开启，不影响正常启动：datagen.tui.enabled=false
 * - 仅在开发/本地环境下，通过配置 datagen.tui.enabled=true 激活
 * - 在同一个 JVM 进程中调用 {@link DataGeneratorTui}，实现“后端启动即可交互生成数据”
 */
@Component
@Order(1000)
public class DataGeneratorStartupRunner implements CommandLineRunner {

    /**
     * 是否在后端启动时启用数据生成 TUI。
     * 可在 application-local.yml 或启动参数中配置：
     * datagen.tui.enabled: true
     */
    @Value("${datagen.tui.enabled:false}")
    private boolean tuiEnabled;

    @Override
    public void run(String... args) throws Exception {
        if (!tuiEnabled) {
            // 默认静默略过，不打扰正常启动
            return;
        }

        System.out.println("======================================");
        System.out.println(" 商南茶数据生成工具提示 (开发模式)");
        System.out.println("======================================");
        System.out.println("当前服务已启动，可选择是否立即打开内部数据生成控制台页面。");
        System.out.println("注意：仅建议在本地/开发环境使用，不要在生产环境生成测试数据。");
        System.out.println("控制台地址: http://localhost:8080/api/internal/datagen");
        System.out.print("是否现在在默认浏览器中打开该页面？输入 Y 确认，其他任意键跳过：");

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line = in.readLine();
        if (line != null && "Y".equalsIgnoreCase(line.trim())) {
            System.out.println("[DataGen-Startup] 尝试在默认浏览器中打开内部数据生成控制台页面...");
            try {
                if (!java.awt.Desktop.isDesktopSupported()) {
                    System.err.println("[DataGen-Startup] 当前环境不支持 Desktop API，请手动在浏览器中打开: http://localhost:8080/api/internal/datagen");
                    return;
                }
                java.awt.Desktop.getDesktop().browse(new java.net.URI("http://localhost:8080/api/internal/datagen"));
            } catch (Exception e) {
                System.err.println("[DataGen-Startup] 自动打开浏览器失败，请手动访问: http://localhost:8080/api/internal/datagen");
                e.printStackTrace(System.err);
            }
        } else {
            System.out.println("[DataGen-Startup] 已跳过自动打开控制台页面，如需使用请手动访问: http://localhost:8080/api/internal/datagen");
        }
    }
}

