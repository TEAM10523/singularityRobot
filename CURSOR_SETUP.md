# Cursor中配置WPILib项目指南

## 前置要求

1. **安装Java 17 JDK**
   - 下载并安装Java 17 JDK (推荐使用Eclipse Temurin)
   - 下载地址: <https://adoptium.net/temurin/releases/?version=17>
   - 设置JAVA_HOME环境变量
   - 运行项目根目录下的 `setup_env.bat` 来自动配置环境

2. **安装必要的扩展**
   在Cursor中安装以下扩展：
   - Extension Pack for Java (vscjava.vscode-java-pack)
   - Gradle for Java (vscjava.vscode-gradle)
   - WPILib (wpilibsuite.vscode-wpilib)

## 配置步骤

### 1. 安装扩展

打开Cursor，按 `Ctrl+Shift+X` 打开扩展面板，搜索并安装：

- `Extension Pack for Java`
- `Gradle for Java`
- `WPILib`

### 2. 配置Java环境

确保Cursor能够找到Java 17：

- 打开命令面板 (`Ctrl+Shift+P`)
- 输入 "Java: Configure Java Runtime"
- 选择Java 17作为项目JDK

### 3. 项目初始化

首次打开项目时：

1. 等待Java语言服务器启动
2. 等待Gradle项目同步完成
3. 如果提示信任工作区，选择"是"

## 常用命令

### 通过命令面板运行

按 `Ctrl+Shift+P` 打开命令面板，可以运行：

- `Tasks: Run Task` - 运行Gradle任务
- `Java: Run` - 运行Java程序
- `Java: Debug` - 调试Java程序

### 通过终端运行

在Cursor的集成终端中运行：

**Windows (PowerShell/CMD):**

```cmd
# 构建项目
.\gradlew.bat build

# 清理项目
.\gradlew.bat clean

# 部署到RoboRIO
.\gradlew.bat deploy

# 运行测试
.\gradlew.bat test

# 模拟机器人
.\gradlew.bat simulateJava
```

**Linux/macOS:**

```bash
# 构建项目
./gradlew build

# 清理项目
./gradlew clean

# 部署到RoboRIO
./gradlew deploy

# 运行测试
./gradlew test

# 模拟机器人
./gradlew simulateJava
```

### 通过任务菜单运行

按 `Ctrl+Shift+P` 然后输入 "Tasks: Run Task"，选择：

- Build Project
- Clean Project
- Deploy to RoboRIO
- Run Tests
- Simulate Robot

## 调试配置

项目已配置了两种调试模式：

1. **WPILib Desktop Debug** - 在桌面环境调试
2. **WPILib roboRIO Debug** - 在RoboRIO上调试

按 `F5` 或使用调试面板选择调试配置。

## 代码格式化

项目配置了Google Java代码风格：

- 保存时自动格式化
- 自动整理导入语句
- 自动清理代码

## 常见问题

### 1. Java语言服务器未启动

- 检查Java 17是否正确安装
- 重启Cursor
- 检查项目JDK设置

### 2. Gradle同步失败

- 检查网络连接
- 清理Gradle缓存：`./gradlew clean`
- 删除 `.gradle` 文件夹后重新同步

### 3. WPILib扩展问题

- 确保安装了最新版本的WPILib扩展
- 检查项目是否在正确的目录结构下

## 项目结构说明

```
singularityRobot/
├── src/main/java/frc/robot/     # 机器人代码
├── src/main/deploy/             # 部署文件
├── vendordeps/                  # 第三方库依赖
├── build.gradle                 # Gradle构建配置
└── .vscode/                     # Cursor配置
    ├── settings.json            # 工作区设置
    ├── launch.json              # 调试配置
    ├── tasks.json               # 任务配置
    └── extensions.json          # 扩展推荐
```

## 团队协作

确保团队成员都安装了相同的扩展和配置，以保持开发环境的一致性。
