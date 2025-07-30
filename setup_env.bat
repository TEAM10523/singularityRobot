@echo off
echo 正在设置WPILib项目环境...

REM 检查Java是否已安装
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Java。请先安装Java 17 JDK。
    echo 下载地址: https://adoptium.net/temurin/releases/?version=17
    pause
    exit /b 1
)

REM 设置JAVA_HOME环境变量
for /f "tokens=*" %%i in ('where java') do set JAVA_PATH=%%i
set JAVA_HOME=%JAVA_PATH:~0,-10%
echo JAVA_HOME设置为: %JAVA_HOME%

REM 验证Gradle wrapper
echo 验证Gradle wrapper...
.\gradlew.bat --version
if %errorlevel% neq 0 (
    echo 错误: Gradle wrapper验证失败
    pause
    exit /b 1
)

echo.
echo 环境设置完成！
echo 现在你可以在Cursor中打开项目了。
echo.
echo 下一步：
echo 1. 在Cursor中安装必要的扩展
echo 2. 打开项目文件夹
echo 3. 等待Java语言服务器启动
echo 4. 运行 gradlew build 来构建项目
echo.
pause 