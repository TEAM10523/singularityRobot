# 多电机驱动的前馈计算机制

## 概述

在真实的机器人系统中，一个机制通常由多个电机驱动。例如：

- **电梯机制**：可能有两个电机（左右两侧）
- **机械臂**：可能有多个关节电机
- **底盘**：通常有4个或6个驱动电机

本框架已经扩展支持多电机驱动，并实现了智能的前馈分配算法。

## 核心改进

### 1. 多电机支持

**Mechanism基类改进**：

```java
// 从单电机改为多电机列表
protected List<MotorIO> motorIOs = new ArrayList<>();
protected List<MotorConfig> motorConfigs = new ArrayList<>();
protected List<MotorInputs> motorInputs = new ArrayList<>();
```

**主要方法更新**：

- `registerMotor()`: 支持注册多个电机
- `updateMechanismState()`: 更新所有电机状态
- `executeControl()`: 向所有电机发送控制命令
- `isAtTarget()`: 基于所有电机的平均状态判断

### 2. 智能前馈分配

#### 基础分配算法

```java
protected List<Double> distributeFeedforwardAmongMotors(SimpleMatrix totalFeedforward) {
    // 默认：平均分配给所有电机
    double feedforwardPerMotor = totalMagnitude / motorIOs.size();
    return motorFeedforwards;
}
```

#### 线性机制的自定义分配

```java
// 考虑齿轮比和效率
for (int i = 0; i < getMotorCount(); i++) {
    MotorConfig config = motorConfigs.get(i);
    double motorRatio = config.gearRatio / totalGearRatio;
    double motorEfficiency = 0.85; // 假设85%效率
    
    // 前馈 = 总力 × 电机比例 / 电机效率
    double motorFeedforward = totalMagnitude * motorRatio / motorEfficiency;
    motorFeedforwards.add(motorFeedforward);
}
```

#### 旋转机制的自定义分配

```java
// 考虑齿轮比和效率（扭矩分配）
for (int i = 0; i < getMotorCount(); i++) {
    MotorConfig config = motorConfigs.get(i);
    double motorRatio = config.gearRatio / totalGearRatio;
    double motorEfficiency = 0.85;
    
    // 扭矩 = 总扭矩 × 电机比例 / 电机效率
    double motorTorque = totalMagnitude * motorRatio / motorEfficiency;
    motorFeedforwards.add(motorTorque);
}
```

## 物理原理

### 1. 力/扭矩分配

**线性机制**：

- 总前馈力 = 重力 + 惯性力 + 摩擦力
- 每个电机分担的力 = 总力 × (电机齿轮比 / 总齿轮比) / 电机效率

**旋转机制**：

- 总前馈扭矩 = 重力扭矩 + 惯性扭矩 + 科里奥利扭矩
- 每个电机分担的扭矩 = 总扭矩 × (电机齿轮比 / 总齿轮比) / 电机效率

### 2. 状态平均

**位置/角度**：

```java
double avgPosition = 0.0;
for (MotorInputs inputs : motorInputs) {
    avgPosition += inputs.position;
}
avgPosition /= motorInputs.size();
```

**速度**：

```java
double avgVelocity = 0.0;
for (MotorInputs inputs : motorInputs) {
    avgVelocity += inputs.velocity;
}
avgVelocity /= motorInputs.size();
```

## 使用示例

### 双电机电梯设置

```java
// 创建两个电机配置
MotorConfig elevatorConfig1 = new MotorConfig("Elevator1", 1, "rio");
MotorConfig elevatorConfig2 = new MotorConfig("Elevator2", 3, "rio");

// 配置参数
elevatorConfig1.gearRatio = 10.0;
elevatorConfig2.gearRatio = 10.0;

// 创建电机
KrakenIO elevatorMotor1 = new KrakenIO(elevatorConfig1);
KrakenIO elevatorMotor2 = new KrakenIO(elevatorConfig2);

// 注册到机制
elevator.registerMotor(elevatorMotor1, elevatorConfig1);
elevator.registerMotor(elevatorMotor2, elevatorConfig2);
```

### 多关节机械臂

```java
// 每个关节可以有不同的电机数量
arm.registerMotor(shoulderMotor, shoulderConfig);
arm.registerMotor(elbowMotor1, elbowConfig1);
arm.registerMotor(elbowMotor2, elbowConfig2); // 双电机肘关节
```

## 优势

### 1. 负载分担

- 多个电机分担负载，提高系统可靠性
- 减少单个电机的热负荷

### 2. 精确控制

- 考虑每个电机的特性（齿轮比、效率）
- 智能分配前馈，避免电机过载

### 3. 容错能力

- 单个电机故障时，其他电机仍可工作
- 系统状态基于平均值，提高稳定性

### 4. 扩展性

- 支持任意数量的电机
- 可自定义分配算法

## 高级特性

### 1. 自定义分配算法

子类可以重写 `distributeFeedforwardAmongMotors()` 方法：

```java
@Override
protected List<Double> distributeFeedforwardAmongMotors(SimpleMatrix totalFeedforward) {
    // 自定义分配逻辑
    // 例如：基于电机温度、电流限制等
}
```

### 2. 电机状态监控

```java
// 获取特定电机状态
MotorInputs motor1State = mechanism.getMotorInputs(0);
MotorInputs motor2State = mechanism.getMotorInputs(1);

// 获取所有电机状态
List<MotorInputs> allMotorStates = mechanism.getMotorInputs();
```

### 3. 电机数量查询

```java
int motorCount = mechanism.getMotorCount();
```

## 注意事项

1. **齿轮比一致性**：同一机制中的电机应具有相同的齿轮比
2. **效率估算**：当前使用85%的固定效率，实际应用中可能需要动态调整
3. **同步控制**：所有电机接收相同的目标位置/速度，但前馈不同
4. **故障处理**：建议添加电机故障检测和处理逻辑

## 总结

多电机驱动的前馈计算机制提供了：

- **物理准确性**：基于真实物理原理的力/扭矩分配
- **工程实用性**：考虑齿轮比、效率等实际因素
- **系统可靠性**：负载分担和容错能力
- **扩展灵活性**：支持自定义分配算法

这使得框架能够处理复杂的多电机机器人系统，提供精确和可靠的控制。
