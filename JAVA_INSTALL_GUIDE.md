# Java 17 安装指南 (Windows)

## 方法一：使用安装程序（推荐）

### 1. 下载安装程序版本
访问 https://adoptium.net/temurin/releases/?version=17

选择以下文件：
- **Windows x64 Installer** (例如: `OpenJDK17U-jdk_x64_windows_hotspot_17.0.9_9.msi`)

### 2. 安装步骤
1. 双击下载的 `.msi` 文件
2. 点击 "Next"
3. 选择安装路径（建议使用默认路径）
4. 勾选 "Add to PATH" 选项
5. 点击 "Install"
6. 等待安装完成

## 方法二：手动安装（如果你下载的是压缩包）

### 1. 解压文件
1. 下载 `OpenJDK17U-jdk_x64_windows_hotspot_17.0.9_9.zip`
2. 右键点击压缩包，选择"解压到当前文件夹"
3. 将解压后的文件夹重命名为 `jdk-17`
4. 将整个文件夹移动到 `C:\Program Files\Java\` 目录下

### 2. 设置环境变量
1. 按 `Win + R`，输入 `sysdm.cpl`，回车
2. 点击"高级"选项卡
3. 点击"环境变量"按钮
4. 在"系统变量"部分：
   - 点击"新建"
   - 变量名：`JAVA_HOME`
   - 变量值：`C:\Program Files\Java\jdk-17`
5. 编辑 `Path` 变量：
   - 选择 `Path`，点击"编辑"
   - 点击"新建"
   - 添加：`%JAVA_HOME%\bin`
6. 点击"确定"保存所有更改

### 3. 验证安装
1. 打开新的命令提示符或PowerShell
2. 输入：`java -version`
3. 应该看到类似输出：
   ```
   openjdk version "17.0.9" 2023-10-17
   OpenJDK Runtime Environment Temurin-17.0.9+9 (build 17.0.9+9)
   OpenJDK 64-Bit Server VM Temurin-17.0.9+9 (build 17.0.9+9, mixed mode, sharing)
   ```

## 方法三：使用Chocolatey（如果你有Chocolatey）

在管理员权限的PowerShell中运行：
```powershell
choco install temurin17
```

## 方法四：使用Winget（Windows 10/11）

在PowerShell中运行：
```powershell
winget install EclipseAdoptium.Temurin.17.JDK
```

## 验证安装

安装完成后，运行以下命令验证：

```cmd
java -version
javac -version
echo %JAVA_HOME%
```

## 常见问题

### 1. "java不是内部或外部命令"
- 检查环境变量是否正确设置
- 重启命令提示符或PowerShell
- 重启电脑

### 2. 多个Java版本冲突
- 检查 `Path` 变量中是否有多个Java路径
- 确保 `JAVA_HOME` 指向正确的版本

### 3. 权限问题
- 以管理员身份运行安装程序
- 确保有写入 `C:\Program Files\Java\` 的权限

## 安装完成后

1. 运行项目根目录下的 `setup_env.bat`
2. 在Cursor中打开项目
3. 安装推荐的扩展
4. 开始开发！

## 快速验证脚本

创建一个 `check_java.bat` 文件：

```batch
@echo off
echo 检查Java安装...
echo.

echo Java版本:
java -version
echo.

echo Java编译器版本:
javac -version
echo.

echo JAVA_HOME环境变量:
echo %JAVA_HOME%
echo.

echo Java可执行文件位置:
where java
echo.

pause
```

如果所有检查都通过，你就可以开始使用WPILib项目了！ 