# DBeaver本地连接远程MySQL方案

## 🎯 场景说明

- **您的情况**：在本地电脑使用DBeaver
- **远程服务器**：10.73.194.94
- **MySQL位置**：远程服务器的Docker容器中
- **MySQL容器IP**：172.21.0.3

---

## 📋 方案对比

| 方案 | 难度 | 是否需要修改 | 安全性 |
|------|------|--------------|--------|
| 方案1：暴露MySQL端口 | ⭐ 简单 | 需要修改docker-compose | ⚠️ 中 |
| 方案2：SSH本地端口转发 | ⭐⭐ 中等 | 不需要 | ✅ 高 |

---

## 🚀 方案1：暴露MySQL端口（推荐-最简单）

### 步骤1：修改docker-compose配置

```bash
# 编辑配置文件
nano /home/moshu/xiaozhi-esp32-server/main/xiaozhi-server/docker-compose_all.yml
```

找到`xiaozhi-esp32-server-db`部分（大约第57行），修改：

**修改前：**
```yaml
xiaozhi-esp32-server-db:
  image: mysql:latest
  container_name: xiaozhi-esp32-server-db
  # ...
  expose:
    - 3306
```

**修改后：**
```yaml
xiaozhi-esp32-server-db:
  image: mysql:latest
  container_name: xiaozhi-esp32-server-db
  # ...
  ports:
    - "3306:3306"
```

### 步骤2：重启MySQL容器

```bash
cd /home/moshu/xiaozhi-esp32-server/main/xiaozhi-server
sudo docker compose -f docker-compose_all.yml restart xiaozhi-esp32-server-db
```

### 步骤3：在DBeaver中配置（简单连接）

**不需要SSH隧道，直接连接：**

```
连接类型：MySQL
主机：10.73.194.94
端口：3306
数据库：xiaozhi_esp32_server
用户名：root
密码：123456
```

1. 打开DBeaver
2. 新建连接 → MySQL
3. 填写上面的信息
4. 测试连接
5. 完成

### ⚠️ 安全建议（重要！）

如果使用此方案，建议配置防火墙：

```bash
# 只允许您的IP访问3306端口
sudo ufw allow from 您的本地IP to any port 3306

# 或者只允许本地访问（如果服务器在本地网络）
sudo ufw allow from 10.0.0.0/8 to any port 3306
```

---

## 🔐 方案2：SSH本地端口转发（更安全）

### 工作原理

```
本地3307端口 → SSH隧道 → 远程服务器 → Docker MySQL(172.21.0.3:3306)
```

### 步骤1：在本地电脑建立SSH隧道

**Windows（使用PowerShell或CMD）：**
```bash
ssh -L 3307:172.21.0.3:3306 您的用户名@10.73.194.94
```

**Mac/Linux：**
```bash
ssh -L 3307:172.21.0.3:3306 您的用户名@10.73.194.94
```

参数说明：
- `-L 3307:172.21.0.3:3306`
  - `3307`：本地电脑的端口（可以改成其他未占用端口）
  - `172.21.0.3`：MySQL容器的IP
  - `3306`：MySQL的端口

执行后：
- 输入SSH密码
- **保持这个SSH连接不要关闭！**（隧道需要保持活跃）

### 步骤2：在DBeaver中配置

**直接连接本地端口：**

```
连接类型：MySQL
主机：localhost
端口：3307  （注意：是3307，不是3306）
数据库：xiaozhi_esp32_server
用户名：root
密码：123456
```

1. 打开DBeaver
2. 新建连接 → MySQL
3. 填写上面的信息
4. **不要勾选SSH隧道**（我们已经手动建立了）
5. 测试连接
6. 完成

### 优点

- ✅ 数据加密传输
- ✅ 不需要修改配置
- ✅ 数据库端口不暴露到公网

### 缺点

- ⚠️ 需要保持SSH连接
- ⚠️ 每次使用前需要先建立隧道

### 💡 自动化脚本（可选）

**Windows（创建bat文件）：**
```batch
@echo off
echo 正在建立数据库隧道...
ssh -L 3307:172.21.0.3:3306 您的用户名@10.73.194.94
```

**Mac/Linux（创建sh文件）：**
```bash
#!/bin/bash
echo "正在建立数据库隧道..."
ssh -L 3307:172.21.0.3:3306 您的用户名@10.73.194.94
```

---

## 🔍 方案3：在DBeaver中使用SSH隧道（修正版）

如果您坚持在DBeaver中配置SSH隧道，请这样配置：

### 【常规】标签

```
服务器主机：172.21.0.3  ← 改成容器IP
端口：3306
数据库：xiaozhi_esp32_server
用户名：root
密码：123456
```

### 【SSH】标签

```
☑️ 使用SSH隧道
主机/IP：10.73.194.94
端口：22
用户名：[您的SSH用户名]
密码：[您的SSH密码]
```

**不需要填写"远程主机"高级设置**，因为我们在常规标签中直接填了容器IP。

---

## 📊 三种方案对比

### 方案1：暴露端口
- **优点**：最简单，配置一次永久有效
- **缺点**：端口暴露到公网，需要配置防火墙
- **适合**：内网环境或配置了防火墙

### 方案2：SSH端口转发
- **优点**：最安全，不需要修改配置
- **缺点**：每次使用需要先建立隧道
- **适合**：安全要求高的场景

### 方案3：DBeaver SSH隧道
- **优点**：在DBeaver中一键连接
- **缺点**：配置稍复杂，需要容器IP
- **适合**：经常使用数据库工具的场景

---

## 💡 推荐方案

### 如果您在公网环境
→ **方案2（SSH端口转发）** - 最安全

### 如果您在内网或VPN环境
→ **方案1（暴露端口）** - 最简单方便

### 如果您经常使用DBeaver
→ **方案3（DBeaver SSH隧道）** - 配置好一次即可

---

## 🔧 快速实施（推荐方案2）

### 1. 在本地电脑打开终端/CMD

```bash
ssh -L 3307:172.21.0.3:3306 您的用户名@10.73.194.94
```

### 2. 保持SSH连接不要关闭

### 3. 打开DBeaver配置连接

```
主机：localhost
端口：3307
数据库：xiaozhi_esp32_server
用户名：root
密码：123456
```

### 4. 测试连接

点击"测试连接"，应该会成功！

---

## ⚠️ 常见错误

### 错误1：Connection refused

**原因**：SSH隧道没有建立或已断开
**解决**：检查SSH隧道是否还在运行

### 错误2：Access denied

**原因**：用户名或密码错误
**解决**：确认密码是`123456`

### 错误3：Unknown database

**原因**：数据库名错误
**解决**：确认数据库名是`xiaozhi_esp32_server`（带下划线）

---

## 📝 测试SQL

连接成功后，执行以下SQL测试：

```sql
-- 查看所有表
SHOW TABLES;

-- 查看数据库信息
SELECT DATABASE();

-- 查看LLM配置
SELECT id, model_code, model_name 
FROM ai_model_config 
WHERE model_type = 'LLM';
```

---

## 🎯 下一步

连接成功后，您可以：
1. 查看所有模型配置数据
2. 为"调整默认模型配置"做准备
3. 备份重要数据

需要我帮您执行哪个方案？





