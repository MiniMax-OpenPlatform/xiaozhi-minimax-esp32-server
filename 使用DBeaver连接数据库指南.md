# 使用DBeaver连接数据库指南

## 📊 当前数据库信息

从docker-compose配置中获取的信息：
- **数据库类型**：MySQL (latest)
- **数据库名**：xiaozhi_esp32_server
- **用户名**：root
- **密码**：123456
- **内部端口**：3306（仅在Docker网络内可用）
- **容器名**：xiaozhi-esp32-server-db

## ⚠️ 当前问题

数据库端口**只在Docker内部网络暴露**，没有映射到主机，因此无法直接从外部连接。

---

## 🔧 解决方案（3种方法）

### 方案1：暴露数据库端口（推荐）⭐

修改docker-compose文件，将MySQL端口映射到主机。

#### 步骤1：修改docker-compose_all.yml

```bash
nano /home/moshu/xiaozhi-esp32-server/main/xiaozhi-server/docker-compose_all.yml
```

找到`xiaozhi-esp32-server-db`部分（大约第57行），将：

```yaml
expose:
  - 3306
```

**修改为**：

```yaml
ports:
  - "3306:3306"  # 映射到主机3306端口
```

或者如果主机3306端口被占用，可以映射到其他端口：

```yaml
ports:
  - "33060:3306"  # 映射到主机33060端口
```

#### 步骤2：重启数据库容器

```bash
cd /home/moshu/xiaozhi-esp32-server/main/xiaozhi-server
sudo docker compose -f docker-compose_all.yml restart xiaozhi-esp32-server-db
```

#### 步骤3：在DBeaver中配置连接

**连接信息：**
```
主机：10.73.194.94（或您的服务器IP）
端口：3306（或您映射的端口，如33060）
数据库：xiaozhi_esp32_server
用户名：root
密码：123456
```

**DBeaver配置步骤：**
1. 打开DBeaver
2. 点击"新建数据库连接"（或按Ctrl+N）
3. 选择"MySQL"
4. 填写以上连接信息
5. 点击"测试连接"
6. 如果成功，点击"完成"

---

### 方案2：使用SSH隧道（更安全）⭐⭐⭐⭐⭐

不修改配置，通过SSH隧道转发数据库连接。

**优点**：
- ✅ 安全（数据加密传输）
- ✅ 不需要暴露数据库端口到公网
- ✅ 不需要修改docker-compose配置

#### 在DBeaver中配置SSH隧道

**步骤1：新建连接**
1. 打开DBeaver
2. 新建连接 → 选择MySQL
3. 填写主连接信息：
   ```
   主机：localhost
   端口：3306
   数据库：xiaozhi_esp32_server
   用户名：root
   密码：123456
   ```

**步骤2：配置SSH隧道**
1. 切换到"SSH"标签
2. 勾选"使用SSH隧道"
3. 填写SSH信息：
   ```
   主机/IP：10.73.194.94
   端口：22
   用户名：您的SSH用户名
   认证方法：密码 或 公钥
   密码/密钥：您的SSH密码或私钥路径
   ```

**步骤3：高级设置（重要）**
1. 点击"高级"或"SSH设置"
2. 在"本地端口"保持默认（通常自动选择）
3. **远程主机**：填写 `xiaozhi-esp32-server-db`（容器名）
4. **远程端口**：3306

**步骤4：测试并连接**
1. 点击"测试连接"
2. 如果成功，点击"完成"

---

### 方案3：使用Docker命令连接（临时查看）

如果只是临时查看数据，可以直接进入容器：

```bash
# 进入MySQL容器
sudo docker exec -it xiaozhi-esp32-server-db mysql -u root -p123456 xiaozhi_esp32_server
```

进入后可以执行SQL命令：

```sql
-- 查看所有表
SHOW TABLES;

-- 查看模型配置
SELECT * FROM ai_model_config;

-- 查看LLM配置
SELECT id, model_code, model_name, is_default, is_enabled 
FROM ai_model_config 
WHERE model_type = 'LLM';

-- 查看TTS配置
SELECT id, model_code, model_name, is_default, is_enabled 
FROM ai_model_config 
WHERE model_type = 'TTS';

-- 退出
EXIT;
```

---

## 📋 完整的连接参数表

### 直接连接（需要方案1暴露端口）

| 参数 | 值 |
|------|-----|
| 连接类型 | MySQL |
| 主机 | 10.73.194.94 |
| 端口 | 3306 |
| 数据库 | xiaozhi_esp32_server |
| 用户名 | root |
| 密码 | 123456 |
| 时区 | Asia/Shanghai |

### SSH隧道连接（推荐）

**主连接：**
| 参数 | 值 |
|------|-----|
| 主机 | localhost |
| 端口 | 3306 |
| 数据库 | xiaozhi_esp32_server |
| 用户名 | root |
| 密码 | 123456 |

**SSH设置：**
| 参数 | 值 |
|------|-----|
| SSH主机 | 10.73.194.94 |
| SSH端口 | 22 |
| SSH用户 | 您的用户名 |
| 远程主机 | xiaozhi-esp32-server-db |
| 远程端口 | 3306 |

---

## 🗺️ 数据库表结构

连接成功后，您可以看到以下主要表：

```
xiaozhi_esp32_server
├── ai_model_config          # 模型配置表（重点）
├── ai_model_provider        # 模型供应商表
├── ai_tts_voice            # TTS音色表
├── ai_agent                # 智能体配置表
├── ai_agent_template       # 智能体模板表
├── ai_device               # 设备信息表
├── ai_voiceprint          # 声纹识别表
├── ai_voice_clone         # 声音克隆表
├── ai_chat_history        # 对话历史表
├── ai_chat_message        # 对话消息表
└── sys_*                  # 系统管理表
```

---

## 🔍 常用查询示例

### 查看所有LLM配置

```sql
SELECT 
    id,
    model_code,
    model_name,
    is_default,
    is_enabled,
    config_json,
    sort
FROM ai_model_config
WHERE model_type = 'LLM'
ORDER BY sort;
```

### 查看所有TTS配置

```sql
SELECT 
    id,
    model_code,
    model_name,
    is_default,
    is_enabled,
    config_json,
    sort
FROM ai_model_config
WHERE model_type = 'TTS'
ORDER BY sort;
```

### 查看启用的模型

```sql
SELECT 
    model_type,
    model_code,
    model_name,
    is_default
FROM ai_model_config
WHERE is_enabled = 1
ORDER BY model_type, sort;
```

### 统计每种类型的模型数量

```sql
SELECT 
    model_type,
    COUNT(*) as total_count,
    SUM(is_enabled) as enabled_count
FROM ai_model_config
GROUP BY model_type;
```

---

## ⚠️ 安全建议

### 如果使用方案1（暴露端口）

1. **修改默认密码**
   ```sql
   ALTER USER 'root'@'%' IDENTIFIED BY '新的强密码';
   FLUSH PRIVILEGES;
   ```

2. **限制访问IP**
   修改docker-compose，只允许特定IP访问：
   ```yaml
   ports:
     - "127.0.0.1:3306:3306"  # 只允许本地访问
   ```

3. **使用防火墙**
   ```bash
   # 只允许特定IP访问3306端口
   sudo ufw allow from 您的IP地址 to any port 3306
   ```

### 推荐：使用SSH隧道（方案2）

方案2最安全，因为：
- ✅ 数据库端口不暴露到公网
- ✅ 所有流量通过SSH加密
- ✅ 利用SSH的认证机制

---

## 🎯 推荐操作流程

### 如果您在本地（可以访问服务器）
→ 使用**方案2（SSH隧道）** - 最安全

### 如果您在服务器上
→ 使用**方案3（Docker命令）** - 最简单

### 如果需要远程GUI工具且不介意暴露端口
→ 使用**方案1（暴露端口）** + 修改密码 + 配置防火墙

---

## 💡 小贴士

1. **备份数据库**
   ```bash
   sudo docker exec xiaozhi-esp32-server-db \
     mysqldump -u root -p123456 xiaozhi_esp32_server \
     > /tmp/xiaozhi_backup_$(date +%Y%m%d).sql
   ```

2. **恢复数据库**
   ```bash
   sudo docker exec -i xiaozhi-esp32-server-db \
     mysql -u root -p123456 xiaozhi_esp32_server \
     < /tmp/xiaozhi_backup_20251016.sql
   ```

3. **查看数据库大小**
   ```sql
   SELECT 
       table_schema as '数据库',
       ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) as '大小(MB)'
   FROM information_schema.tables 
   WHERE table_schema = 'xiaozhi_esp32_server'
   GROUP BY table_schema;
   ```

---

## 📞 需要帮助？

如果遇到问题：
1. 检查Docker容器是否运行：`sudo docker ps | grep xiaozhi`
2. 查看数据库日志：`sudo docker logs xiaozhi-esp32-server-db`
3. 测试网络连接：`telnet 10.73.194.94 3306`

请告诉我您选择哪个方案，我可以帮您执行具体步骤！





