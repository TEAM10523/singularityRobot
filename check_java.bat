@echo off
echo ========================================
echo    Java环境检查脚本
echo ========================================
echo.

echo 检查Java运行时...
java -version >nul 2>&1
if %errorlevel% equ 0 (
    echo [✓] Java运行时已安装
    echo Java版本信息:
    java -version
) else (
    echo [✗] Java运行时未安装或未配置
    echo 请参考 JAVA_INSTALL_GUIDE.md 进行安装
)
echo.

echo 检查Java编译器...
javac -version >nul 2>&1
if %errorlevel% equ 0 (
    echo [✓] Java编译器已安装
    echo Java编译器版本:
    javac -version
) else (
    echo [✗] Java编译器未安装或未配置
)
echo.

echo 检查JAVA_HOME环境变量...
if defined JAVA_HOME (
    echo [✓] JAVA_HOME已设置
    echo JAVA_HOME = %JAVA_HOME%
) else (
    echo [✗] JAVA_HOME未设置
    echo 请设置JAVA_HOME环境变量
)
echo.

echo 检查Java可执行文件位置...
where java >nul 2>&1
if %errorlevel% equ 0 (
    echo [✓] Java在PATH中找到
    echo Java位置:
    where java
) else (
    echo [✗] Java不在PATH中
    echo 请检查环境变量配置
)
echo.

echo ========================================
echo 检查完成！
echo ========================================
echo.

if defined JAVA_HOME (
    echo 如果所有检查都通过，你可以：
    echo 1. 运行 setup_env.bat 配置项目环境
    echo 2. 运行 quick_start.bat 快速启动项目
) else (
    echo 请先完成Java安装和环境配置
    echo 参考 JAVA_INSTALL_GUIDE.md
)
echo.
pause 