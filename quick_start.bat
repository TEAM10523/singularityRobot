@echo off
echo ========================================
echo    WPILib项目快速启动脚本
echo ========================================
echo.

REM 检查Java环境
echo 检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未找到Java环境
    echo 请先安装Java 17 JDK
    echo 下载地址: https://adoptium.net/temurin/releases/?version=17
    echo.
    pause
    exit /b 1
)

echo [成功] Java环境已就绪
echo.

REM 显示Java版本
echo Java版本信息:
java -version
echo.

REM 构建项目
echo 开始构建项目...
.\gradlew.bat build
if %errorlevel% neq 0 (
    echo [错误] 项目构建失败
    pause
    exit /b 1
)

echo.
echo [成功] 项目构建完成！
echo.
echo 下一步操作：
echo 1. 在Cursor中打开项目文件夹
echo 2. 安装推荐的扩展
echo 3. 等待Java语言服务器启动
echo 4. 开始编写机器人代码
echo.
echo 常用命令：
echo - 构建: .\gradlew.bat build
echo - 清理: .\gradlew.bat clean
echo - 部署: .\gradlew.bat deploy
echo - 测试: .\gradlew.bat test
echo - 模拟: .\gradlew.bat simulateJava
echo.
pause 