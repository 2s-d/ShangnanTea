#!/bin/bash
# 商南茶城项目更新脚本
# 用于更新已部署的商南茶城项目（后端 + 前端）

set -e

echo "=========================================="
echo "  商南茶城项目更新脚本"
echo "=========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_success() {
    echo -e "${GREEN}[成功]${NC} $1"
}

print_error() {
    echo -e "${RED}[错误]${NC} $1"
}

print_info() {
    echo -e "${YELLOW}[信息]${NC} $1"
}

# 项目路径配置
BACKEND_DIR="/opt/shangnantea-server"
FRONTEND_DIR="/var/www/shangnantea"
BACKEND_JAR="shangnantea-server-1.0.0.jar"

# 检查是否为 root 用户
if [ "$EUID" -ne 0 ]; then 
    print_error "请使用 root 用户运行此脚本"
    exit 1
fi

echo "请选择要更新的部分："
echo "1) 只更新后端"
echo "2) 只更新前端"
echo "3) 同时更新后端和前端"
read -p "请输入选项 (1/2/3): " choice

# ========== 更新后端 ==========
update_backend() {
    print_info "开始更新后端..."
    
    # 1. 进入后端目录
    cd $BACKEND_DIR
    
    # 2. 拉取最新代码（如果使用 Git）
    if [ -d ".git" ]; then
        print_info "拉取最新代码..."
        git pull origin main  # 或者 master，根据你的分支名
        print_success "代码拉取完成"
    else
        print_info "未检测到 Git 仓库，跳过代码拉取"
        print_info "请手动上传新代码到 $BACKEND_DIR"
        read -p "代码已上传？按回车继续..."
    fi
    
    # 3. 停止旧的后端服务
    print_info "停止旧的后端服务..."
    if pgrep -f "$BACKEND_JAR" > /dev/null; then
        pkill -f "$BACKEND_JAR"
        sleep 3
        print_success "旧服务已停止"
    else
        print_info "未检测到运行中的后端服务"
    fi
    
    # 4. 清理旧的构建文件
    print_info "清理旧的构建文件..."
    mvn clean
    
    # 5. 重新构建项目
    print_info "开始构建项目（这可能需要几分钟）..."
    mvn package -DskipTests
    
    if [ $? -eq 0 ]; then
        print_success "项目构建成功"
    else
        print_error "项目构建失败，请检查错误信息"
        exit 1
    fi
    
    # 6. 启动新的后端服务
    print_info "启动新的后端服务..."
    nohup java -jar target/$BACKEND_JAR > logs/app.log 2>&1 &
    
    sleep 5
    
    # 7. 检查服务是否启动成功
    if pgrep -f "$BACKEND_JAR" > /dev/null; then
        print_success "后端服务启动成功"
        print_info "查看日志: tail -f $BACKEND_DIR/logs/app.log"
    else
        print_error "后端服务启动失败，请查看日志"
        exit 1
    fi
}

# ========== 更新前端 ==========
update_frontend() {
    print_info "开始更新前端..."
    
    # 1. 进入前端目录
    cd $FRONTEND_DIR
    
    # 2. 拉取最新代码（如果使用 Git）
    if [ -d ".git" ]; then
        print_info "拉取最新代码..."
        git pull origin main
        print_success "代码拉取完成"
    else
        print_info "未检测到 Git 仓库，跳过代码拉取"
        print_info "请手动上传新代码到 $FRONTEND_DIR"
        read -p "代码已上传？按回车继续..."
    fi
    
    # 3. 安装依赖（如果 package.json 有变化）
    print_info "检查并安装依赖..."
    npm install
    
    # 4. 构建前端项目
    print_info "开始构建前端项目..."
    npm run build
    
    if [ $? -eq 0 ]; then
        print_success "前端构建成功"
    else
        print_error "前端构建失败，请检查错误信息"
        exit 1
    fi
    
    # 5. 备份旧文件
    if [ -d "dist.backup" ]; then
        rm -rf dist.backup
    fi
    if [ -d "dist" ]; then
        print_info "备份旧的前端文件..."
        cp -r dist dist.backup
    fi
    
    # 6. 重启 Nginx（可选）
    print_info "重启 Nginx..."
    systemctl reload nginx
    print_success "Nginx 已重新加载配置"
}

# ========== 执行更新 ==========
case $choice in
    1)
        update_backend
        ;;
    2)
        update_frontend
        ;;
    3)
        update_backend
        echo ""
        update_frontend
        ;;
    *)
        print_error "无效的选项"
        exit 1
        ;;
esac

echo ""
echo "=========================================="
echo "  更新完成！"
echo "=========================================="
echo ""
print_success "项目已成功更新"
echo ""
echo "访问地址："
echo "  - 前端: http://$(curl -s ifconfig.me)/shangnantea/"
echo "  - 后端: http://$(curl -s ifconfig.me):8080"
echo ""
echo "常用命令："
echo "  - 查看后端日志: tail -f $BACKEND_DIR/logs/app.log"
echo "  - 重启后端: pkill -f $BACKEND_JAR && cd $BACKEND_DIR && nohup java -jar target/$BACKEND_JAR > logs/app.log 2>&1 &"
echo "  - 重启 Nginx: systemctl restart nginx"
echo "=========================================="
