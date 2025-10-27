# xiaozhi-minimax-esp32-server

<p align="center">
  <img src="docs/images/banner1.png" alt="Banner" />
</p>

<h2 align="center">Xiaozhi ESP32 Server - Minimax AI Deep Integration</h2>

<p align="center">
  <a href="./README.md">中文</a> · 
  <a href="#deployment-guide">Deployment</a> · 
  <a href="#features">Features</a> · 
  <a href="#acknowledgments">Acknowledgments</a>
</p>

---

## 📖 About This Project

This project is a customized version based on [xinnan-tech/xiaozhi-esp32-server](https://github.com/xinnan-tech/xiaozhi-esp32-server), with deep integration and optimization for **Minimax AI** platform.

### Tribute to Original Project 🙏

We sincerely thank Professor Siyuan Liu's team at South China University of Technology for their outstanding contribution. Their innovative Xiaozhi intelligent terminal system based on human-machine symbiosis theory provides a solid foundation for our customization.

- **Original Project**: [https://github.com/xinnan-tech/xiaozhi-esp32-server](https://github.com/xinnan-tech/xiaozhi-esp32-server)
- **Documentation**: Please refer to the original project for detailed deployment and usage documentation

---

## ✨ Features

This customized version includes the following core enhancements:

### 🎯 Minimax AI Deep Integration

#### 1. **Minimax LLM Support**
- ✅ Integrated Minimax abab6.5s-chat model
- ✅ Complete conversation context management
- ✅ Streaming response support
- ✅ OpenAI API compatible

#### 2. **Minimax TTS Support**
- ✅ Integrated Minimax speech-01-turbo model
- ✅ Streaming speech synthesis with low latency
- 🎤 **Featured Voice**: Perfect replication of Xiaozhi's "Wanwan" voice
  - Voice ID: `vc_wanwan_0303_02-wanwan_0303_01_0303_1047`
  - Professionally tuned to match Xiaozhi hardware's voice characteristics
  - Natural and fluent voice interaction experience

#### 3. **Enhanced ASR**
- ✅ DoubaoASR support (ByteDance Volcano Engine)
- ✅ Keep original FunASR, Tencent ASR and more options

### 🔐 SaaS Multi-Tenancy Architecture

Complete multi-tenant isolation architecture for SaaS deployment:

- **User-level Configuration Isolation**: Each user manages their own model configurations and API keys
- **Flexible Permission Control**: Support multi-user registration with independent configuration spaces
- **Secure Data Isolation**: Sensitive information (API Keys, URLs) completely isolated between users
- **Auto Initialization**: New users automatically get default configuration templates (with empty sensitive fields)
- **Simplified Configuration**: New users only see recommended core model configurations

### 🌐 Web Interaction Interface

- **Web Dialogue Page**: Interact with Xiaozhi through browser without hardware
- **Real-time Voice Conversation**: Browser microphone recording and real-time speech synthesis
- **Upgraded Control Panel**: All users can access model configuration management

---

## 🚀 Quick Start

### Prerequisites

- Docker & Docker Compose
- Minimax API account and key ([Register here](https://platform.minimax.com/))
- Server or local environment (Ubuntu/Debian recommended)

### Deployment Steps

Please refer to the original project documentation for detailed deployment process:
- [Original Project Deployment Guide](https://github.com/xinnan-tech/xiaozhi-esp32-server#%E9%83%A8%E7%BD%B2%E6%96%87%E6%A1%A3)

#### Configuration Differences

This customized version requires Minimax parameters. Configure them in the "Model Configuration" panel:

**Minimax LLM Configuration Example**:
```yaml
type: openai
model_name: abab6.5s-chat
url: https://api.minimaxi.com/v1
api_key: YOUR_MINIMAX_API_KEY
```

**Minimax TTS Configuration Example**:
```yaml
type: minimax
model: speech-01-turbo
group_id: YOUR_GROUP_ID
api_key: YOUR_MINIMAX_API_KEY
voice_id: vc_wanwan_0303_02-wanwan_0303_01_0303_1047  # Wanwan voice
```

**DoubaoASR Configuration Example**:
```yaml
type: doubao
appid: YOUR_APPID
access_token: YOUR_ACCESS_TOKEN
cluster: volcengine_input_common
```

---

## 📂 Project Structure

```
xiaozhi-minimax-esp32-server/
├── main/
│   ├── xiaozhi-server/       # Python core service (WebSocket, protocol, AI integration)
│   ├── manager-api/           # Java Spring Boot backend API
│   └── manager-web/           # Vue.js frontend management interface
├── docker-compose.yml         # Docker compose configuration
└── README.md                  # This document
```

---

## 🎤 About "Wanwan" Voice

"Wanwan" is the iconic voice of Xiaozhi project. This customized version perfectly replicates it through Minimax TTS platform:

- **Voice ID**: `vc_wanwan_0303_02-wanwan_0303_01_0303_1047`
- **Characteristics**: 
  - Warm and friendly female voice
  - Clear and natural pronunciation
  - Suitable for intelligent assistant scenarios
  - Highly consistent with Xiaozhi hardware image

Using this voice provides the same voice experience as the original Xiaozhi hardware.

---

## 🛠️ Tech Stack

- **Backend**: Python (FastAPI/WebSocket), Java (Spring Boot)
- **Frontend**: Vue.js, Element UI
- **AI Services**: 
  - Minimax LLM (abab6.5s-chat)
  - Minimax TTS (speech-01-turbo)
  - DoubaoASR / FunASR / Tencent ASR
- **Infrastructure**: Docker, MySQL, Redis, Nginx
- **Protocols**: WebSocket, MQTT, UDP, MCP

---

## 📝 Changelog

### v1.0.0 (2025-10)
- ✅ Integrated Minimax LLM (abab6.5s-chat)
- ✅ Integrated Minimax TTS (speech-01-turbo) + Wanwan voice
- ✅ Integrated DoubaoASR speech recognition
- ✅ Implemented SaaS multi-tenancy architecture
- ✅ User-level configuration isolation and permission control
- ✅ Auto initialization with simplified configuration for new users
- ✅ Optimized web interaction interface

---

## 🤝 Acknowledgments

### Special Thanks

- **Professor Siyuan Liu's Team (SCUT)**: Thank you for developing the excellent xiaozhi-esp32-server project, which provides a solid technical foundation
- **Minimax AI Platform**: Providing high-quality LLM and TTS services
- **Open Source Community**: All developers and users who contributed to the Xiaozhi project

### Original Project Links

- **Original Repository**: [xinnan-tech/xiaozhi-esp32-server](https://github.com/xinnan-tech/xiaozhi-esp32-server)
- **Hardware Project**: [xiaozhi-esp32](https://github.com/78/xiaozhi-esp32)
- **Protocol Documentation**: [Xiaozhi Communication Protocol](https://ccnphfhqs21z.feishu.cn/wiki/M0XiwldO9iJwHikpXD5cEx71nKh)

---

## 📄 License

This project follows the MIT License of the original project.

---

## 📧 Contact

For questions or suggestions, please submit via Issues.

---

<p align="center">
  <b>Customized based on the original project, tribute to open source spirit 🚀</b>
</p>
