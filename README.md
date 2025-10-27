# xiaozhi-minimax-esp32-server

<p align="center">
  <img src="docs/images/banner1.png" alt="Banner" />
</p>

<h2 align="center">小智后端服务 - Minimax AI 深度集成版</h2>

<p align="center">
  <a href="./README_en.md">English</a> · 
  <a href="#部署指南">部署指南</a> · 
  <a href="#功能特性">功能特性</a> · 
  <a href="#致谢">致谢</a>
</p>

---

## 📖 项目说明

本项目基于 [xinnan-tech/xiaozhi-esp32-server](https://github.com/xinnan-tech/xiaozhi-esp32-server) 开源项目进行二次开发，特别针对 **Minimax AI** 平台进行了深度集成和优化。

### 致敬原项目 🙏

我们衷心感谢华南理工大学刘思源教授团队的卓越贡献，他们基于人机共生智能理论和技术研发的小智智能终端软硬件体系为本项目奠定了坚实的基础。原项目的创新性架构和完善的文档使得我们能够快速实现定制化需求。

- **原项目地址**: [https://github.com/xinnan-tech/xiaozhi-esp32-server](https://github.com/xinnan-tech/xiaozhi-esp32-server)
- **原项目文档**: 请参考原项目的详细部署和使用文档

---

## ✨ 功能特性

本定制版本在原项目基础上进行了以下核心改动：

### 🎯 Minimax AI 深度集成

#### 1. **Minimax LLM（大语言模型）支持**
- ✅ 集成 Minimax abab6.5s-chat 模型
- ✅ 完整的对话上下文管理
- ✅ 支持流式响应
- ✅ 兼容 OpenAI API 调用规范

#### 2. **Minimax TTS（语音合成）支持**
- ✅ 集成 Minimax speech-01-turbo 模型
- ✅ 流式语音合成，低延迟响应
- 🎤 **特色音色**: 完美复刻小智"湾湾"音色
  - 音色ID: `vc_wanwan_0303_02-wanwan_0303_01_0303_1047`
  - 该音色经过专业调优，完美匹配小智硬件的声音特质
  - 提供自然、流畅的语音交互体验

#### 3. **ASR（语音识别）增强**
- ✅ 支持 DoubaoASR（字节跳动火山引擎）
- ✅ 保留原有 FunASR、腾讯ASR 等多种选择

### 🔐 SaaS 多租户架构

本版本实现了完整的多租户隔离架构，使项目可作为 SaaS 平台使用：

- **用户级配置隔离**: 每个用户独立管理自己的模型配置和 API 密钥
- **灵活的权限控制**: 支持多用户注册，每个用户拥有独立的配置空间
- **安全的数据隔离**: 敏感信息（API Key、URL等）完全隔离，用户间互不可见
- **自动初始化**: 新用户注册时自动创建默认配置模板（敏感信息为空）
- **精简配置体验**: 新用户只看到推荐的核心模型配置，降低使用门槛

### 🌐 Web 交互界面

- **Web 对话页面**: 无需硬件即可通过浏览器与小智交互
- **实时语音对话**: 支持浏览器麦克风录音和实时语音合成播放
- **智控台升级**: 所有用户可访问模型配置管理界面

---

## 🚀 快速开始

### 前置要求

- Docker & Docker Compose
- Minimax API 账号和密钥（[注册地址](https://platform.minimax.com/)）
- 服务器或本地环境（推荐 Ubuntu/Debian）

### 部署步骤

详细的部署流程请参考原项目文档：
- [原项目部署指南](https://github.com/xinnan-tech/xiaozhi-esp32-server#%E9%83%A8%E7%BD%B2%E6%96%87%E6%A1%A3)

#### 核心配置差异

本定制版本需要配置 Minimax 相关参数，在智控台的"模型配置"中设置：

**Minimax LLM 配置示例**:
```yaml
type: openai
model_name: abab6.5s-chat
url: https://api.minimaxi.com/v1
api_key: 你的_MINIMAX_API_KEY
```

**Minimax TTS 配置示例**:
```yaml
type: minimax
model: speech-01-turbo
group_id: 你的_GROUP_ID
api_key: 你的_MINIMAX_API_KEY
voice_id: vc_wanwan_0303_02-wanwan_0303_01_0303_1047  # 湾湾音色
```

**DoubaoASR 配置示例**:
```yaml
type: doubao
appid: 你的_APPID
access_token: 你的_ACCESS_TOKEN
cluster: volcengine_input_common
```

---

## 📂 项目结构

```
xiaozhi-minimax-esp32-server/
├── main/
│   ├── xiaozhi-server/       # Python 核心服务（WebSocket、协议处理、AI集成）
│   ├── manager-api/           # Java Spring Boot 后端 API
│   └── manager-web/           # Vue.js 前端管理界面
├── docker-compose.yml         # Docker 编排配置
└── README.md                  # 本文档
```

---

## 🎤 关于"湾湾"音色

"湾湾"是小智项目的标志性音色，本定制版通过 Minimax TTS 平台实现了该音色的完美复刻：

- **音色ID**: `vc_wanwan_0303_02-wanwan_0303_01_0303_1047`
- **特点**: 
  - 温暖亲切的女声
  - 清晰自然的发音
  - 适合智能助手场景
  - 与小智硬件形象高度契合

使用该音色可以获得与原版小智硬件一致的语音体验。

---

## 🛠️ 技术栈

- **后端**: Python (FastAPI/WebSocket), Java (Spring Boot)
- **前端**: Vue.js, Element UI
- **AI 服务**: 
  - Minimax LLM (abab6.5s-chat)
  - Minimax TTS (speech-01-turbo)
  - DoubaoASR / FunASR / 腾讯ASR
- **基础设施**: Docker, MySQL, Redis, Nginx
- **协议**: WebSocket, MQTT, UDP, MCP

---

## 📝 更新日志

### v1.0.0 (2025-10)
- ✅ 集成 Minimax LLM (abab6.5s-chat)
- ✅ 集成 Minimax TTS (speech-01-turbo) + 湾湾音色
- ✅ 集成 DoubaoASR 语音识别
- ✅ 实现 SaaS 多租户架构
- ✅ 用户级配置隔离和权限控制
- ✅ 新用户自动初始化精简配置
- ✅ Web 交互界面优化

---

## 🤝 致谢

### 特别感谢

- **华南理工大学刘思源教授团队**: 感谢他们开发的出色的 xiaozhi-esp32-server 项目，为本项目提供了坚实的技术基础
- **Minimax AI 平台**: 提供高质量的 LLM 和 TTS 服务
- **开源社区**: 所有为小智项目贡献的开发者和用户

### 原项目相关链接

- **原项目仓库**: [xinnan-tech/xiaozhi-esp32-server](https://github.com/xinnan-tech/xiaozhi-esp32-server)
- **硬件项目**: [xiaozhi-esp32](https://github.com/78/xiaozhi-esp32)
- **通信协议文档**: [小智通信协议](https://ccnphfhqs21z.feishu.cn/wiki/M0XiwldO9iJwHikpXD5cEx71nKh)

---

## 📄 开源协议

本项目遵循原项目的 MIT 协议。

---

## 📧 联系方式

如有问题或建议，欢迎通过 Issues 反馈。

---

<p align="center">
  <b>基于原项目二次开发，致敬开源精神 🚀</b>
</p>
