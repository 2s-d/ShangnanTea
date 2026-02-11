#!/bin/bash
# 服务器环境检测脚本
# 检测商南茶城和个人博客所需的所有环境

echo "=========================================="
echo "  服务器环境检测报告"
echo "=========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 检测结果统计
TOTAL=0
SUCCESS=0
FAILED=0

check_command() {
    TOTAL=$((TOTAL + 1))
    if command -v $1 &> /dev/null; then
        echo -e "${GREEN}[✓]${NC} $2"
        SUCCESS=$((SUCCESS + 1))
        if [ ! -z "$3" ]; then
            echo "    版本: $($3 2>&1 | head -n 1)"
        fi
        return 0
    else
        echo -e "${RED}[✗]${NC} $2"
        FAILED=$((FAILED + 1))
        return 1
    fi
}

check_service() {
    TOTAL=$((TOTAL + 1))
    if systemctl is-active --quiet $1; then
        echo -e "${GREEN}[✓]${NC} $2 (运行中)"
        SUCCESS=$((SUCCESS + 1))
        return 0
    else
        if systemctl list-unit-files | grep -q "^$1"; then
            echo -e "${YELLOW}[!]${NC} $2 (已安装但未运行)"
            echo "    启动命令: sudo systemctl start $1"
        else
            echo -e "${RED}[✗]${NC} $2 (未安装)"
            FAILED=$((FAILED + 1))
        fi
        return 1
    fi
}

echo "【1. 基础工具检测】"
echo "----------------------------"
check_command "curl" "curl" "curl --version"
check_command "wget" "wget" "wget --version"
check_command "git" "git" "git --version"
check_command "vim" "vim" "vim --version"
check_command "unzip" "unzip" "unzip -v"
echo ""

echo "【2. Java 环境检测】"
echo "----------------------------"
check_command "java" "Java JDK" "java -version"
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}')
    if [[ $JAVA_VERSION == 21.* ]]; then
        echo -e "    ${GREEN}Java 21 ✓${NC}"
    else
        echo -e "    ${YELLOW}当前版本: $JAVA_VERSION (需要 Java 21)${NC}"
    fi
fi
check_command "javac" "Java 编译器" "javac -version"
echo ""

echo "【3. Maven 检测】"
echo "----------------------------"
check_command "mvn" "Maven" "mvn -version"
echo ""

echo "【4. Node.js 环境检测】"
echo "----------------------------"
check_command "node" "Node.js" "node -v"
if command -v node &> /dev/null; then
    NODE_VERSION=$(node -v | sed 's/v//')
    NODE_MAJOR=$(echo $NODE_VERSION | cut -d. -f1)
    if [ $NODE_MAJOR -ge 18 ]; then
        echo -e "    ${GREEN}Node.js 版本符合要求 (>= 18)${NC}"
    else
        echo -e "    ${YELLOW}当前版本: $NODE_VERSION (建议 >= 18)${NC}"
    fi
fi
check_command "npm" "npm" "npm -v"
echo ""

echo "【5. 数据库检测】"
echo "----------------------------"
check_command "mysql" "MySQL 客户端" "mysql --version"
check_service "mysql" "MySQL 服务"
if systemctl is-active --quiet mysql; then
    echo "    测试连接: mysql -u root -p"
fi
echo ""

echo "【6. Redis 检测】"
echo "----------------------------"
check_command "redis-server" "Redis 服务器" "redis-server --version"
check_command "redis-cli" "Redis 客户端" "redis-cli --version"
check_service "redis-server" "Redis 服务"
if systemctl is-active --quiet redis-server; then
    echo "    测试连接: redis-cli ping"
    REDIS_PING=$(redis-cli ping 2>&1)
    if [ "$REDIS_PING" = "PONG" ]; then
        echo -e "    ${GREEN}Redis 连接正常${NC}"
    fi
fi
echo ""

echo "【7. Web 服务器检测】"
echo "----------------------------"
check_command "nginx" "Nginx" "nginx -v"
check_service "nginx" "Nginx 服务"
if systemctl is-active --quiet nginx; then
    echo "    配置文件: /etc/nginx/nginx.conf"
    echo "    站点配置: /etc/nginx/sites-available/"
fi
echo ""

echo "【8. 防火墙检测】"
echo "----------------------------"
if command -v ufw &> /dev/null; then
    UFW_STATUS=$(ufw status | head -n 1)
    if [[ $UFW_STATUS == *"active"* ]]; then
        echo -e "${GREEN}[✓]${NC} UFW 防火墙 (已启用)"
        echo ""
        echo "    开放的端口:"
        ufw status | grep ALLOW | awk '{print "    - " $1}'
    else
        echo -e "${YELLOW}[!]${NC} UFW 防火墙 (未启用)"
    fi
else
    echo -e "${RED}[✗]${NC} UFW 防火墙 (未安装)"
fi
echo ""

echo "【9. 项目目录检测】"
echo "----------------------------"
TOTAL=$((TOTAL + 2))
if [ -d "/var/www/shangnantea" ]; then
    echo -e "${GREEN}[✓]${NC} 前端目录: /var/www/shangnantea"
    SUCCESS=$((SUCCESS + 1))
else
    echo -e "${RED}[✗]${NC} 前端目录: /var/www/shangnantea (不存在)"
    echo "    创建命令: sudo mkdir -p /var/www/shangnantea"
    FAILED=$((FAILED + 1))
fi

if [ -d "/opt/shangnantea-server" ]; then
    echo -e "${GREEN}[✓]${NC} 后端目录: /opt/shangnantea-server"
    SUCCESS=$((SUCCESS + 1))
else
    echo -e "${RED}[✗]${NC} 后端目录: /opt/shangnantea-server (不存在)"
    echo "    创建命令: sudo mkdir -p /opt/shangnantea-server"
    FAILED=$((FAILED + 1))
fi
echo ""

echo "【10. 系统信息】"
echo "----------------------------"
echo "操作系统: $(cat /etc/os-release | grep PRETTY_NAME | cut -d'"' -f2)"
echo "内核版本: $(uname -r)"
echo "CPU 核心: $(nproc)"
echo "内存信息: $(free -h | grep Mem | awk '{print $2 " 总计, " $3 " 已用, " $4 " 可用"}')"
echo "磁盘空间: $(df -h / | tail -1 | awk '{print $2 " 总计, " $3 " 已用, " $4 " 可用"}')"
echo ""

echo "=========================================="
echo "  检测结果汇总"
echo "=========================================="
echo -e "总计: $TOTAL 项"
echo -e "${GREEN}成功: $SUCCESS 项${NC}"
echo -e "${RED}失败: $FAILED 项${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✓ 所有环境已就绪，可以开始部署项目！${NC}"
    echo ""
    echo "下一步操作："
    echo "1. 上传项目代码到服务器"
    echo "2. 配置数据库（运行 SQL 脚本）"
    echo "3. 部署商南茶城项目"
    echo "4. 部署个人博客项目"
else
    echo -e "${YELLOW}! 还有 $FAILED 项环境未就绪${NC}"
    echo ""
    echo "建议操作："
    echo "1. 运行环境安装脚本: sudo bash server-deploy.sh"
    echo "2. 或手动安装缺失的软件"
fi
echo "=========================================="
