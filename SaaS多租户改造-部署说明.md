# SaaS多租户改造 - 部署说明

## ✅ 代码改造已完成

### 改造内容总结

#### 后端改造（3个文件）

**1. ModelConfigServiceImpl.java**
- ✅ `getModelCodeList()` - 添加用户数据过滤（creator IS NULL OR creator = userId）
- ✅ `getLlmModelCodeList()` - 添加用户数据过滤
- ✅ `getPageList()` - 添加用户数据过滤（超级管理员看所有，普通用户只看系统+自己的）
- ✅ `add()` - 自动设置 creator 为当前用户ID
- ✅ `edit()` - 添加权限校验（只能编辑自己的配置）
- ✅ `delete()` - 添加权限校验（不能删除系统配置，只能删除自己的）
- ✅ `validateEditPermission()` - 新增编辑权限验证方法
- ✅ `validateDeletePermission()` - 新增删除权限验证方法

**2. ModelController.java**
- ✅ `getModelConfigList()` - 权限从 `superAdmin` 改为 `normal`
- ✅ `addModelConfig()` - 权限从 `superAdmin` 改为 `normal`
- ✅ `editModelConfig()` - 权限从 `superAdmin` 改为 `normal`
- ✅ `deleteModelConfig()` - 权限从 `superAdmin` 改为 `normal`
- ✅ `getModelConfig()` - 权限从 `superAdmin` 改为 `normal`
- ✅ `enableModelConfig()` - 权限从 `superAdmin` 改为 `normal`

**3. ConfigServiceImpl.java**
- ✅ 经分析确认：**无需修改**
- 原因：多层权限控制（设备层、智能体层、模型选择层）已确保安全性

#### 前端改造（2个文件）

**1. HeaderBar.vue**
- ✅ 移除 `v-if="isSuperAdmin"` - "模型配置"菜单对所有用户可见

**2. ModelConfig.vue**
- ✅ 模型名称列添加标签：
  - 系统配置显示 `<el-tag type="info">系统</el-tag>`
  - 用户配置显示 `<el-tag type="success">我的</el-tag>`
- ✅ 添加 `currentUserId` computed 属性 - 获取当前用户ID
- ✅ 添加 `isSuperAdmin` computed 属性 - 判断是否超级管理员
- ✅ 添加 `canEdit()` 方法 - 判断是否可编辑
- ✅ 添加 `canDelete()` 方法 - 判断是否可删除
- ✅ 添加 `viewModel()` 方法 - 只读查看模式
- ✅ 操作按钮优化：
  - 编辑按钮：只对可编辑的配置显示
  - 查看按钮：系统配置且非管理员时显示
  - 复制按钮：系统配置显示"复制为我的"，用户配置显示"复制"
  - 删除按钮：只对可删除的配置显示

---

## 🚀 部署方案

### ⚠️ 重要说明

当前部署使用的是 **远程预构建镜像**：
```
ghcr.nju.edu.cn/xinnan-tech/xiaozhi-esp32-server:web_latest
```

这意味着：
- ✅ 代码已在源码中修改完成
- ❌ 修改后的代码尚未应用到运行中的容器
- 需要重新构建 Docker 镜像才能使改动生效

### 方案选择

#### 方案A：容器内热修复（快速验证）⚡

**优点**：
- 快速，几分钟内完成
- 适合快速验证功能

**缺点**：
- 容器重启后改动会丢失
- 不适合生产环境

**操作步骤**：
```bash
# 1. 复制修改后的Java文件到容器
sudo docker cp /home/moshu/xiaozhi-esp32-server/main/manager-api/src/main/java/xiaozhi/modules/model/service/impl/ModelConfigServiceImpl.java \
  xiaozhi-esp32-server-web:/opt/app/WEB-INF/classes/xiaozhi/modules/model/service/impl/

sudo docker cp /home/moshu/xiaozhi-esp32-server/main/manager-api/src/main/java/xiaozhi/modules/model/controller/ModelController.java \
  xiaozhi-esp32-server-web:/opt/app/WEB-INF/classes/xiaozhi/modules/model/controller/

# 2. 复制修改后的Vue文件到容器
sudo docker cp /home/moshu/xiaozhi-esp32-server/main/manager-web/src/components/HeaderBar.vue \
  xiaozhi-esp32-server-web:/opt/app/static/vue/

sudo docker cp /home/moshu/xiaozhi-esp32-server/main/manager-web/src/views/ModelConfig.vue \
  xiaozhi-esp32-server-web:/opt/app/static/vue/

# 3. 重启容器
cd /home/moshu/xiaozhi-esp32-server/main/xiaozhi-server
sudo docker compose -f docker-compose_all.yml restart xiaozhi-esp32-server-web
```

**注意**：
- Java文件需要编译成.class文件才能工作（容器内没有编译工具）
- Vue文件也需要编译成.js文件
- 这个方案**不可行**，因为容器内没有编译环境

#### 方案B：本地构建镜像（推荐）🌟

**优点**：
- 完整、可靠
- 改动永久生效
- 适合生产环境

**缺点**：
- 需要本地构建环境（Maven、Node.js）
- 耗时较长（10-30分钟）

**准备工作**：
```bash
# 1. 安装Maven
sudo apt update
sudo apt install -y maven openjdk-17-jdk

# 2. 安装Node.js和npm
sudo apt install -y nodejs npm
```

**构建步骤**：

**Step 1: 编译后端（manager-api）**
```bash
cd /home/moshu/xiaozhi-esp32-server/main/manager-api
mvn clean package -DskipTests

# 生成的jar包位置：
# target/manager-api-0.8.4.jar
```

**Step 2: 编译前端（manager-web）**
```bash
cd /home/moshu/xiaozhi-esp32-server/main/manager-web
npm install
npm run build

# 生成的文件位置：
# dist/ 目录
```

**Step 3: 构建Docker镜像**

创建本地Dockerfile（`/home/moshu/xiaozhi-esp32-server/main/Dockerfile.web`）：
```dockerfile
FROM ghcr.nju.edu.cn/xinnan-tech/xiaozhi-esp32-server:web_base

# 复制编译好的后端
COPY manager-api/target/manager-api-*.jar /opt/app.jar

# 复制编译好的前端
COPY manager-web/dist/ /opt/app/static/

EXPOSE 8002

CMD ["java", "-jar", "/opt/app.jar"]
```

**Step 4: 修改 docker-compose_all.yml**

将：
```yaml
xiaozhi-esp32-server-web:
  image: ghcr.nju.edu.cn/xinnan-tech/xiaozhi-esp32-server:web_latest
```

改为：
```yaml
xiaozhi-esp32-server-web:
  build:
    context: ../
    dockerfile: Dockerfile.web
  image: xiaozhi-esp32-server-web:local
```

**Step 5: 重新构建并启动**
```bash
cd /home/moshu/xiaozhi-esp32-server/main/xiaozhi-server
sudo docker compose -f docker-compose_all.yml up -d --build xiaozhi-esp32-server-web
```

#### 方案C：使用已修改的源码创建新镜像（简化版）

如果原镜像的Dockerfile可用，可以：
1. 将修改后的源码提交到Git仓库
2. 使用原始的构建脚本重新构建镜像
3. 更新docker-compose.yml指向新镜像

---

## 🎯 当前状态

### ✅ 已完成
- [x] 后端代码改造（ModelConfigServiceImpl.java, ModelController.java）
- [x] 前端代码改造（HeaderBar.vue, ModelConfig.vue）
- [x] 权限逻辑设计和实现
- [x] 数据隔离实现

### ⏳ 待完成
- [ ] 选择部署方案并执行
- [ ] 验证功能是否正常
- [ ] 测试多用户场景

---

## 📊 验证清单

部署完成后，请按以下步骤验证：

### 1. 普通用户测试

**1.1 登录普通用户账号**
- 用户名：`test_user`
- 密码：（您设置的密码）

**1.2 验证"模型配置"菜单可见**
- ✅ 顶部导航栏应该能看到"模型配置"菜单

**1.3 验证模型列表**
- ✅ 应该能看到系统默认配置（标签：`系统`）
- ✅ 应该能看到自己创建的配置（标签：`我的`）
- ❌ 不应该看到其他用户创建的配置

**1.4 验证操作权限**
- ✅ 系统配置：只能"查看"和"复制为我的"
- ✅ 自己的配置：可以"编辑"、"复制"、"删除"
- ✅ 点击"复制为我的"后，应该创建一个新的配置，标签为"我的"

**1.5 验证创建配置**
- ✅ 点击"添加"按钮，创建一个新的MinimaxLLM配置
- ✅ 填入自己的API密钥
- ✅ 保存后，列表中应该出现新配置，标签为"我的"

**1.6 验证智能体使用**
- ✅ 创建或编辑智能体时，选择LLM模型
- ✅ 下拉列表应该只显示：系统配置 + 自己创建的配置
- ❌ 不应该显示其他用户的配置

### 2. 超级管理员测试

**2.1 登录超级管理员账号**

**2.2 验证权限**
- ✅ 可以看到所有用户创建的配置
- ✅ 可以编辑系统配置
- ✅ 可以编辑/删除用户创建的配置
- ❌ 不能删除系统配置

### 3. 安全性测试

**3.1 API越权测试**
- 尝试通过API访问其他用户的模型配置ID
- ✅ 应该返回权限错误或404

**3.2 数据隔离测试**
- 用户A创建配置，用户B登录
- ✅ 用户B不应该看到用户A的配置

---

## 🔧 故障排查

### 问题1：改动没有生效

**症状**：重启容器后，界面和功能没有变化

**原因**：使用的是远程预构建镜像，没有包含本地改动

**解决**：选择方案B，本地构建镜像

### 问题2：编译失败

**症状**：`mvn clean package` 失败

**可能原因**：
1. Maven配置问题
2. JDK版本不匹配
3. 依赖下载失败

**解决**：
```bash
# 检查Java版本
java -version  # 应该是JDK 17

# 检查Maven版本
mvn -version

# 清除Maven缓存重试
rm -rf ~/.m2/repository
mvn clean package -DskipTests
```

### 问题3：前端编译失败

**症状**：`npm run build` 失败

**解决**：
```bash
# 清除node_modules重新安装
rm -rf node_modules package-lock.json
npm install
npm run build
```

### 问题4：权限校验失败

**症状**：编辑/删除时提示"无权操作"

**排查**：
1. 检查用户是否登录
2. 检查配置的creator字段是否正确
3. 查看后端日志：
```bash
sudo docker compose -f docker-compose_all.yml logs xiaozhi-esp32-server-web
```

---

## 📝 下一步建议

1. **选择部署方案**
   - 如果是测试环境，建议方案B（本地构建）
   - 如果是生产环境，建议联系原项目维护者，提交PR合并改动

2. **数据库索引优化**
   ```sql
   -- 为了提升查询性能，建议添加索引
   CREATE INDEX idx_model_type_creator ON ai_model_config(model_type, creator);
   CREATE INDEX idx_model_enabled ON ai_model_config(is_enabled);
   ```

3. **监控和日志**
   - 关注权限校验失败的日志
   - 监控用户创建配置的数量
   - 定期备份数据库

4. **文档和培训**
   - 为用户提供使用指南
   - 说明系统配置和个人配置的区别
   - 指导用户如何复制系统配置并填入自己的API密钥

---

## 💡 联系方式

如有问题或需要进一步的协助，请：
1. 查看完整的改造方案文档：`/home/moshu/xiaozhi-esp32-server/SaaS多租户改造方案.md`
2. 检查代码改动的具体位置和逻辑
3. 参考上述的验证清单和故障排查指南





