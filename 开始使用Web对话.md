# 🎉 服务配置完成！开始使用Web对话

## ✅ 当前状态

所有服务已成功启动：

| 服务 | 状态 | 端口 | 说明 |
|------|------|------|------|
| 🌐 智控台 | ✅ 运行中 | 8002 | http://10.73.194.94:8002 |
| 🤖 对话服务 | ✅ 运行中 | 8000 | WebSocket服务 |
| 💬 Web对话页面 | ✅ 运行中 | 8006 | http://10.73.194.94:8006/test_page.html |
| 🗄️ 数据库 | ✅ 运行中 | - | MySQL |
| 📦 缓存 | ✅ 运行中 | - | Redis |

**Secret已配置**: `95294d8c-f7b3-44ac-a336-6cf6c4b488a1` ✅

---

## 🚀 快速开始（3步）

### 第1步：添加测试设备

1. **打开智控台**：http://10.73.194.94:8002

2. **进入设备管理**：
   - 左侧菜单 → 📱 设备管理
   - 点击【新增设备】按钮

3. **填写设备信息**：
   ```
   设备名称：web测试设备
   设备类型：选择一个合适的类型
   设备ID：web-test-001
   备注：Web对话测试
   ```

4. **保存设备**
   - 记住设备ID：`web-test-001`

### 第2步：打开Web对话页面

在浏览器新标签页打开：
```
http://10.73.194.94:8006/test_page.html
```

### 第3步：配置并连接

1. **填写设备信息区域**：
   ```
   WebSocket地址：ws://10.73.194.94:8000/xiaozhi/v1/
   设备ID：web-test-001
   设备IP：127.0.0.1
   ```

2. **点击【连接WebSocket】按钮**

3. **看到"WebSocket已连接"** ✅

4. **开始对话**：
   - 在下方输入框输入消息，比如："你好"
   - 点击【发送文本】
   - 等待AI回复

---

## 💡 使用提示

### 文本对话
```
输入框：你好，请介绍一下你自己
点击：发送文本按钮
等待：AI语音和文字回复
```

### 语音对话（需要浏览器麦克风权限）
```
1. 点击：开始录音
2. 说话：对着麦克风说话
3. 点击：停止录音
4. 等待：自动识别并回复
```

### 连接信息说明

| 字段 | 值 | 说明 |
|------|-----|------|
| WebSocket地址 | `ws://10.73.194.94:8000/xiaozhi/v1/` | 对话服务地址 |
| 设备ID | `web-test-001` | 您在智控台创建的设备ID |
| 设备IP | `127.0.0.1` | 测试用，保持默认即可 |

---

## 🎯 完整对话流程示例

### 示例1：简单问答

```
您：你好
AI：你好！我是智能助手，很高兴为您服务。有什么可以帮助您的吗？

您：今天天气怎么样？
AI：抱歉，我目前无法获取实时天气信息。您可以...
```

### 示例2：功能测试

```
您：讲个笑话
AI：好的，给您讲一个... [AI会生成笑话并语音播放]

您：帮我写一首诗
AI：好的，我来为您创作一首诗... [AI生成并朗读诗歌]
```

---

## 🔧 配置的AI模型

根据您的配置，当前使用的模型：

| 功能 | 模型 | 说明 |
|------|------|------|
| 🎤 语音识别(ASR) | DoubaoASR | 豆包语音识别 |
| 🧠 大语言模型(LLM) | MinimaxLLM (abab6.5s-chat) | MiniMax对话模型 |
| 🔊 语音合成(TTS) | MinimaxTTS (speech-01-turbo) | MiniMax语音合成 |

---

## 🐛 常见问题

### Q1: WebSocket连接失败

**症状**：点击"连接WebSocket"后显示连接失败

**解决**：
1. 检查WebSocket地址是否正确：`ws://10.73.194.94:8000/xiaozhi/v1/`
2. 检查设备ID是否已在智控台创建
3. 检查Python服务是否运行：
   ```bash
   cd /home/moshu/xiaozhi-esp32-server/main/xiaozhi-server
   sudo docker compose -f docker-compose_all.yml ps
   ```

### Q2: 发送消息没有回复

**症状**：消息发送成功，但AI没有回复

**解决**：
1. 查看Python服务日志：
   ```bash
   cd /home/moshu/xiaozhi-esp32-server/main/xiaozhi-server
   sudo docker compose -f docker-compose_all.yml logs --tail=50 xiaozhi-esp32-server
   ```
2. 检查AI API密钥是否有效
3. 检查网络连接

### Q3: 语音无法播放

**症状**：收到文字回复，但没有语音

**解决**：
1. 检查浏览器音量是否打开
2. 检查TTS配置是否正确
3. 查看浏览器控制台是否有错误（F12）

### Q4: 麦克风权限被拒绝

**症状**：点击"开始录音"后提示权限错误

**解决**：
1. 浏览器地址栏 → 点击🔒 → 权限 → 允许麦克风
2. 或在浏览器设置中允许该网站使用麦克风
3. 刷新页面后重试

---

## 📊 查看日志

### 查看实时对话日志

```bash
cd /home/moshu/xiaozhi-esp32-server/main/xiaozhi-server
sudo docker compose -f docker-compose_all.yml logs -f xiaozhi-esp32-server
```

**按 Ctrl+C 停止查看**

### 查看最近50条日志

```bash
cd /home/moshu/xiaozhi-esp32-server/main/xiaozhi-server
sudo docker compose -f docker-compose_all.yml logs --tail=50 xiaozhi-esp32-server
```

---

## 🎨 高级配置

### 在智控台配置AI模型参数

1. **访问智控台**：http://10.73.194.94:8002

2. **进入模型配置**：
   - 左侧菜单 → 🤖 模型配置

3. **修改模型参数**：
   - 可以修改API Key
   - 可以调整模型参数（温度、最大tokens等）
   - 可以切换不同的模型

4. **保存后自动生效**
   - 无需重启服务
   - 配置会实时同步到对话服务

### 修改系统提示词

在智控台中：
1. 参数管理 → 找到系统提示词相关参数
2. 编辑提示词内容
3. 保存后立即生效

---

## 📱 设备类型说明

在智控台添加设备时，可以选择不同的设备类型：

- **Web测试设备**：用于Web页面测试
- **ESP32设备**：实际的小智硬件设备
- **移动端设备**：移动App接入
- **其他设备**：自定义设备类型

对于Web测试，选择"Web测试设备"或任意类型都可以。

---

## 🔗 快捷链接

- 🌐 **智控台**：http://10.73.194.94:8002
- 💬 **Web对话页面**：http://10.73.194.94:8006/test_page.html
- 📖 **项目GitHub**：https://github.com/xinnan-tech/xiaozhi-esp32-server

---

## 📝 配置文件位置

| 文件 | 路径 | 说明 |
|------|------|------|
| 主配置 | `/home/moshu/xiaozhi-esp32-server/main/xiaozhi-server/config.yaml` | 默认配置 |
| 覆盖配置 | `/home/moshu/xiaozhi-esp32-server/main/xiaozhi-server/data/.config.yaml` | Secret配置 |
| Docker配置 | `/home/moshu/xiaozhi-esp32-server/main/xiaozhi-server/docker-compose_all.yml` | 容器配置 |

---

## 🎉 开始体验吧！

所有准备工作已完成，现在可以：

1. ✅ **添加测试设备** → 智控台设备管理
2. ✅ **打开对话页面** → http://10.73.194.94:8006/test_page.html
3. ✅ **开始对话** → 输入消息测试

如有问题，随时查看日志或询问我！祝使用愉快！😊





