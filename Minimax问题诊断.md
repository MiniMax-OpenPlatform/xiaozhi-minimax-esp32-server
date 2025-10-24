# 🔍 Minimax LLM 问题诊断

## 🐛 问题现象

从服务器日志可以看到：
```
大模型收到用户消息: 你好
发送第一段语音: None
发送音频消息: SentenceType.LAST, None
```

LLM没有生成任何回复，直接返回None。

## 🔬 根本原因

通过测试Minimax API，发现错误：
```json
{
  "status_code": 1004,
  "status_msg": "login fail: Please carry the API secret key in the 'Authorization' field of the request header"
}
```

**Minimax的OpenAI Compatible API认证失败！**

## 💡 可能的原因

### 1. API Key格式问题
Minimax提供的API Key是JWT格式的token，可能不完全兼容OpenAI的认证方式。

### 2. API Endpoint不正确
当前配置使用：`https://api.minimaxi.com/v1`

但Minimax的OpenAI compatible endpoint可能需要不同的路径。

### 3. 需要额外的认证参数
Minimax可能需要同时提供：
- `api_key` 
- `group_id`（在URL参数或header中）

## 🔧 解决方案

### 方案1：使用Minimax的原生API（推荐）

**问题**：当前xiaozhi-server项目可能不支持Minimax的原生API格式。

**状态**：需要开发专门的MinimaxLLM适配器。

### 方案2：使用其他兼容OpenAI的LLM（临时方案）

可以先使用其他LLM测试系统功能，比如：

#### 选项A：DeepSeek（国内，免费额度）
```yaml
DeepSeekLLM:
  type: openai
  model_name: deepseek-chat
  base_url: https://api.deepseek.com/v1
  api_key: 你的DeepSeek API Key
```

#### 选项B：通义千问（阿里云，兼容OpenAI）
```yaml
QwenLLM:
  type: openai  
  model_name: qwen-turbo
  base_url: https://dashscope.aliyuncs.com/compatible-mode/v1
  api_key: 你的通义千问 API Key
```

#### 选项C：Groq（国外，免费，快速）
```yaml
GroqLLM:
  type: openai
  model_name: llama-3.1-70b-versatile
  base_url: https://api.groq.com/openai/v1
  api_key: 你的Groq API Key
```

### 方案3：修正Minimax配置

可能需要尝试：

#### 尝试1：使用不同的base_url
```yaml
MinimaxLLM:
  type: openai
  model_name: abab6.5s-chat
  base_url: https://api.minimaxi.com/v1/chat/completions
  api_key: 你的API Key
```

#### 尝试2：添加group_id参数
可能需要在代码中添加对group_id的支持。

## 🎯 推荐操作步骤

### 步骤1：在智控台添加一个测试LLM

1. **访问智控台**：http://10.73.194.94:8002

2. **进入模型配置**（左侧菜单）

3. **新增模型配置**：
   ```
   配置名称：DeepSeekTest
   模型类型：LLM
   配置类型：openai
   
   配置JSON：
   {
     "type": "openai",
     "model_name": "deepseek-chat",
     "base_url": "https://api.deepseek.com/v1",
     "api_key": "sk-your-deepseek-key",
     "temperature": 0.7,
     "max_tokens": 2000
   }
   ```

### 步骤2：修改设备配置使用新LLM

1. **进入设备管理**

2. **找到您的设备**：`Web测试设备`

3. **编辑设备**

4. **LLM配置选择**：`DeepSeekTest`

5. **保存**

### 步骤3：测试对话

重新在Web对话页面发送消息，应该就能收到AI回复了。

### 步骤4：解决Minimax问题（稍后）

系统正常工作后，再研究Minimax LLM的正确配置方式。

## 📊 当前配置状态

从日志可以看到，系统从API加载的配置：

```json
{
  "LLM": {
    "2f9cd4dc98758e4562e5076cff8862c4": {
      "type": "openai",
      "top_k": "",
      "top_p": "",
      "api_key": "***",
      "base_url": "https://api.minimaxi.com/v1",
      "max_tokens": "***",
      "model_name": "abab6.5s-chat",
      "temperature": "",
      "frequency_penalty": ""
    }
  }
}
```

**问题**：很多字段是空字符串（`""`），这可能也导致API调用失败。

## 🔍 需要检查的地方

1. **智控台 → 模型配置**
   - 找到Minimax LLM的配置
   - 检查所有字段是否填写完整

2. **智控台 → 设备管理 → Web测试设备**
   - 检查分配的LLM配置

3. **尝试其他LLM**
   - 验证系统本身是否正常

## 💬 Minimax的正确使用方式

根据Minimax官方文档，他们的API有两种方式：

### 方式1：OpenAI Compatible API（推荐）
```
POST https://api.minimaxi.com/v1/chat/completions
Authorization: Bearer YOUR_API_KEY
```

### 方式2：原生API
```
POST https://api.minimaxi.com/v1/text/chatcompletion_v2?GroupId=YOUR_GROUP_ID
Authorization: Bearer YOUR_API_KEY
```

需要确认xiaozhi-server是否支持这两种方式。

## 🆘 下一步行动

请告诉我您想：

**选项A**：暂时使用其他LLM（DeepSeek/通义千问等）测试系统
   → 我可以帮您配置

**选项B**：继续调试Minimax LLM
   → 需要查看您在智控台中的详细配置

**选项C**：开发Minimax LLM专用适配器
   → 需要一些开发工作

## 📝 临时变通方案

如果急需测试，可以：

1. **使用免费的OpenAI Compatible LLM**
   - DeepSeek：https://platform.deepseek.com
   - Groq：https://console.groq.com  
   - 通义千问：https://dashscope.aliyun.com

2. **配置到智控台**

3. **分配给设备**

4. **立即测试**

---

**快捷链接**：
- 🌐 智控台：http://10.73.194.94:8002
- 💬 Web对话：http://10.73.194.94:8006/test_page.html

**当前状态**：
- ✅ WebSocket连接正常
- ✅ 设备绑定成功
- ✅ TTS (MinimaxTTS) 工作正常
- ❌ LLM (MinimaxLLM) API认证失败

需要更换或修复LLM配置才能正常对话。





