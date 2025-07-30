@echo off
echo ========================================
echo    自动安装Java 17脚本
echo ========================================
echo.

echo 检查是否已安装Java...
java -version >nul 2>&1
if %errorlevel% equ 0 (
    echo [信息] 检测到Java已安装
    java -version
    echo.
    echo 是否继续安装Java 17？(y/n)
    set /p choice=
    if /i not "%choice%"=="y" (
        echo 安装已取消
        pause
        exit /b 0
    )
)

echo 检查winget是否可用...
winget --version >nul 2>&1
if %errorlevel% equ 0 (
    echo [✓] winget可用，使用winget安装
    echo 正在安装Java 17...
    winget install EclipseAdoptium.Temurin.17.JDK --accept-source-agreements --accept-package-agreements
    if %errorlevel% equ 0 (
        echo [✓] Java 17安装成功！
        echo.
        echo 请重启命令提示符或PowerShell以使环境变量生效
        echo 然后运行 check_java.bat 验证安装
    ) else (
        echo [✗] 安装失败，请尝试手动安装
        echo 参考 JAVA_INSTALL_GUIDE.md
    )
) else (
    echo [✗] winget不可用，请手动安装
    echo.
    echo 请按照以下步骤手动安装：
    echo 1. 访问 https://adoptium.net/temurin/releases/?version=17
    echo 2. 下载 Windows x64 Installer (.msi文件)
    echo 3. 运行安装程序
    echo 4. 勾选"Add to PATH"选项
    echo 5. 完成安装后运行 check_java.bat 验证
    echo.
    echo 详细步骤请参考 JAVA_INSTALL_GUIDE.md
)

echo.
pause 