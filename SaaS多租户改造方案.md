# 小智ESP32-Server SaaS多租户改造方案

## 📋 需求分析

### 核心目标
将系统改造为SaaS平台模式，让所有用户能够：
1. **独立管理自己的模型配置**（ASR、LLM、TTS等）
2. **维护自己的API密钥**（安全隔离）
3. **只能看到和使用自己的配置**（数据隔离）
4. **无需管理员权限**（自助服务）

### 业务场景
- 用户注册后，可以添加自己的MinimaxLLM、DoubaoASR等模型配置
- 用户创建智能体时，只能选择自己配置的模型
- 用户的API密钥完全隔离，互不可见
- 系统管理员可以设置默认模型模板供用户参考

---

## 🔍 现状分析

### 1. 数据库表结构（已具备多租户基础）

#### `ai_model_config` 表
```sql
- id (主键)
- model_type (模型类型: ASR/LLM/TTS等)
- model_code (模型代码: MinimaxLLM, DoubaoASR等)
- model_name (显示名称)
- is_default (是否默认)
- is_enabled (是否启用)
- config_json (配置JSON，包含API密钥)
- creator (创建者用户ID) ✅ 已有
- create_date
```

#### `ai_agent` 表
```sql
- id
- user_id (智能体所属用户) ✅ 已有
- asr_model_id (引用 ai_model_config.id)
- llm_model_id (引用 ai_model_config.id)
- tts_model_id (引用 ai_model_config.id)
- ...
```

#### `ai_device` 表
```sql
- id
- user_id (设备所属用户) ✅ 已有
- agent_id (关联的智能体)
- ...
```

### 2. 前端权限控制

#### HeaderBar.vue (第100-115行)
```vue
<!-- 模型配置菜单 - 当前仅超级管理员可见 -->
<div
  v-if="isSuperAdmin"
  class="equipment-management"
  @click="goModelConfig"
>
  <span class="nav-text">{{ $t("header.modelConfig") }}</span>
</div>
```

**问题**：普通用户看不到"模型配置"菜单

### 3. 后端服务

#### ModelConfigServiceImpl.java
- 已有完整的CRUD方法
- **缺少**：按用户ID过滤的查询逻辑
- **缺少**：数据权限校验

---

## 🎯 改造方案

### 方案一：完全SaaS模式（推荐）⭐

#### 核心思路
**彻底开放模型配置权限，所有用户都可以管理自己的模型配置**

#### 改造内容

### A. 数据库层面（已完善，无需改动）✅

```sql
-- 表结构已满足需求
-- creator 字段已存在，用于标识模型配置的所有者
```

### B. 后端改造（3个核心点）

#### B1. 修改模型配置查询逻辑

**文件**：`ModelConfigServiceImpl.java`

**改动点1**：查询模型列表时过滤用户数据

```java
// 原逻辑：查询所有模型配置
// 新逻辑：查询 (系统默认配置 OR 当前用户创建的配置)

public List<ModelConfigDTO> listByType(String modelType, Long userId) {
    QueryWrapper<ModelConfigEntity> wrapper = new QueryWrapper<>();
    wrapper.eq("model_type", modelType);
    wrapper.eq("is_enabled", 1);
    
    // 关键：数据权限过滤
    wrapper.and(w -> w
        .isNull("creator")                    // 系统默认配置（creator为NULL）
        .or()
        .eq("creator", userId)                // 或当前用户创建的配置
    );
    
    wrapper.orderByDesc("is_default");
    wrapper.orderByAsc("sort");
    
    List<ModelConfigEntity> list = baseDao.selectList(wrapper);
    return ConvertUtils.sourceToTarget(list, ModelConfigDTO.class);
}
```

**改动点2**：创建/更新/删除时添加权限校验

```java
@Override
public void save(ModelConfigDTO dto) {
    // 自动设置创建者
    Long currentUserId = SecurityUser.getUserId();
    dto.setCreator(currentUserId);
    
    ModelConfigEntity entity = ConvertUtils.sourceToTarget(dto, ModelConfigEntity.class);
    insert(entity);
}

@Override
public void update(ModelConfigDTO dto) {
    // 权限校验：只能修改自己创建的配置
    ModelConfigEntity existingEntity = baseDao.selectById(dto.getId());
    if (existingEntity == null) {
        throw new RenException("模型配置不存在");
    }
    
    Long currentUserId = SecurityUser.getUserId();
    boolean isSuperAdmin = SecurityUser.getUser().getSuperAdmin() == 1;
    
    // 非超级管理员只能修改自己的配置
    if (!isSuperAdmin && !currentUserId.equals(existingEntity.getCreator())) {
        throw new RenException("无权修改此配置");
    }
    
    ModelConfigEntity entity = ConvertUtils.sourceToTarget(dto, ModelConfigEntity.class);
    updateById(entity);
}

@Override
public void delete(String[] ids) {
    Long currentUserId = SecurityUser.getUserId();
    boolean isSuperAdmin = SecurityUser.getUser().getSuperAdmin() == 1;
    
    // 权限校验
    for (String id : ids) {
        ModelConfigEntity entity = baseDao.selectById(id);
        if (entity != null) {
            // 不能删除系统默认配置（creator为NULL）
            if (entity.getCreator() == null) {
                throw new RenException("不能删除系统默认配置");
            }
            // 非超级管理员只能删除自己的配置
            if (!isSuperAdmin && !currentUserId.equals(entity.getCreator())) {
                throw new RenException("无权删除此配置");
            }
        }
    }
    
    deleteBatchIds(Arrays.asList(ids));
}
```

**改动点3**：API接口添加用户上下文

```java
// ModelConfigController.java

@GetMapping("/list")
@Operation(summary = "模型配置列表")
public Result<List<ModelConfigVO>> list(@RequestParam String modelType) {
    // 获取当前登录用户ID
    Long userId = SecurityUser.getUserId();
    
    // 调用带用户过滤的查询方法
    List<ModelConfigDTO> list = modelConfigService.listByType(modelType, userId);
    
    return new Result<>().ok(ConvertUtils.sourceToTarget(list, ModelConfigVO.class));
}
```

#### B2. 修改智能体配置下发逻辑

**文件**：`ConfigServiceImpl.java` 的 `buildModuleConfig` 方法

**改动点**：确保只下发用户有权访问的模型配置

```java
private Map<String, Object> buildModuleConfig(Long userId, String[] modelIds) {
    Map<String, Object> moduleConfig = new HashMap<>();
    
    for (int i = 0; i < modelIds.length; i++) {
        if (StringUtils.isBlank(modelIds[i])) {
            continue;
        }
        
        // 获取模型配置
        ModelConfigDTO config = modelConfigService.getModelByIdFromCache(modelIds[i]);
        if (config == null) {
            continue;
        }
        
        // 权限校验：确保用户有权使用此模型配置
        if (config.getCreator() != null && !config.getCreator().equals(userId)) {
            throw new RenException("无权使用此模型配置");
        }
        
        // ... 后续处理逻辑
    }
    
    return moduleConfig;
}
```

### C. 前端改造（2个核心点）

#### C1. 开放"模型配置"菜单给所有用户

**文件**：`HeaderBar.vue`

**修改前**：
```vue
<div
  v-if="isSuperAdmin"
  class="equipment-management"
  @click="goModelConfig"
>
```

**修改后**：
```vue
<div
  class="equipment-management"
  :class="{ 'active-tab': $route.path === '/model-config' }"
  @click="goModelConfig"
>
```

**说明**：移除 `v-if="isSuperAdmin"` 条件，让所有用户都能看到

#### C2. 模型配置页面UI优化

**文件**：`ModelConfig.vue`

**优化点1**：区分系统默认配置和用户配置

```vue
<template>
  <!-- 模型配置列表 -->
  <el-table :data="modelList">
    <!-- 模型名称列 -->
    <el-table-column label="模型名称">
      <template slot-scope="scope">
        {{ scope.row.modelName }}
        <!-- 标识系统配置 -->
        <el-tag v-if="!scope.row.creator" size="mini" type="info">系统</el-tag>
        <!-- 标识我的配置 -->
        <el-tag v-else-if="scope.row.creator === currentUserId" size="mini" type="success">我的</el-tag>
      </template>
    </el-table-column>
    
    <!-- 操作列 -->
    <el-table-column label="操作">
      <template slot-scope="scope">
        <!-- 只能操作自己创建的配置 -->
        <el-button 
          v-if="scope.row.creator === currentUserId || isSuperAdmin"
          @click="handleEdit(scope.row)"
          size="mini"
        >
          编辑
        </el-button>
        <el-button 
          v-if="scope.row.creator === currentUserId || isSuperAdmin"
          @click="handleDelete(scope.row)"
          size="mini" 
          type="danger"
        >
          删除
        </el-button>
        <!-- 系统配置只能查看 -->
        <el-button 
          v-if="!scope.row.creator && !isSuperAdmin"
          @click="handleView(scope.row)"
          size="mini"
        >
          查看
        </el-button>
        <!-- 复制系统配置为我的配置 -->
        <el-button 
          v-if="!scope.row.creator"
          @click="handleCopy(scope.row)"
          size="mini"
          type="primary"
        >
          复制为我的
        </el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script>
export default {
  computed: {
    currentUserId() {
      const tokenStr = localStorage.getItem('token');
      if (tokenStr) {
        const token = JSON.parse(tokenStr);
        return token.id;
      }
      return null;
    },
    isSuperAdmin() {
      const tokenStr = localStorage.getItem('token');
      if (tokenStr) {
        const token = JSON.parse(tokenStr);
        return token.superAdmin === 1;
      }
      return false;
    }
  },
  methods: {
    // 复制系统配置为用户配置
    handleCopy(row) {
      this.$confirm('将复制此配置为您的个人配置，您可以修改API密钥等参数', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        // 创建副本
        const newConfig = {
          ...row,
          id: undefined,           // 清除ID，作为新记录插入
          modelName: row.modelName + ' (我的副本)',
          creator: this.currentUserId,
          isDefault: false         // 副本不作为默认
        };
        
        Api.model.saveModelConfig(newConfig, () => {
          this.$message.success('复制成功');
          this.fetchModelList();
        });
      });
    }
  }
}
</script>
```

**优化点2**：智能体配置页面只显示可用模型

**文件**：`roleConfig.vue`

```vue
<!-- 在选择ASR/LLM/TTS模型时，自动过滤只显示用户有权访问的模型 -->
<el-select v-model="agent.llmModelId">
  <el-option
    v-for="model in userAccessibleLLMModels"
    :key="model.id"
    :label="model.modelName"
    :value="model.id"
  />
</el-select>
```

### D. 路由配置（无需改动）

**文件**：`router/index.js`

```javascript
// 模型配置路由已存在
{
  path: '/model-config',
  name: 'ModelConfig',
  component: () => import('../views/ModelConfig.vue')
}

// 路由守卫已保护此路由需要登录
const protectedRoutes = ['home', 'RoleConfig', 'DeviceManagement', 'UserManagement', 'ModelConfig']
```

**说明**：路由已配置正确，只需确保在 `protectedRoutes` 中包含 `'ModelConfig'`

---

## 📊 数据权限规则总结

### 查询规则
```
用户可见的模型配置 = 系统默认配置（creator IS NULL）+ 自己创建的配置（creator = userId）
```

### 操作权限
| 操作 | 系统配置 | 自己的配置 | 他人的配置 |
|------|---------|----------|-----------|
| 查看 | ✅ 所有用户 | ✅ 所有用户 | ❌ 不可见 |
| 编辑 | ✅ 仅管理员 | ✅ 所有用户 | ❌ 禁止 |
| 删除 | ❌ 禁止 | ✅ 所有用户 | ❌ 禁止 |
| 复制 | ✅ 所有用户 | ✅ 所有用户 | ❌ 不可见 |
| 使用 | ✅ 所有用户 | ✅ 所有用户 | ❌ 禁止 |

---

## 🗂️ 文件清单

### 需要修改的文件

#### 后端（Java）
1. `ModelConfigServiceImpl.java` - 核心业务逻辑（3个方法）
   - [ ] `listByType()` - 添加用户过滤
   - [ ] `save()` - 自动设置creator
   - [ ] `update()` - 添加权限校验
   - [ ] `delete()` - 添加权限校验

2. `ModelConfigController.java` - API接口
   - [ ] `list()` - 传递当前用户ID

3. `ConfigServiceImpl.java` - 配置下发逻辑
   - [ ] `buildModuleConfig()` - 添加权限校验

#### 前端（Vue.js）
4. `HeaderBar.vue` - 顶部导航栏
   - [ ] 移除模型配置菜单的 `v-if="isSuperAdmin"` 限制

5. `ModelConfig.vue` - 模型配置页面
   - [ ] 添加"系统"/"我的"标签
   - [ ] 添加"复制为我的"按钮
   - [ ] 优化编辑/删除按钮权限控制

6. `roleConfig.vue` - 智能体配置页面（可选优化）
   - [ ] 模型选择器过滤逻辑优化

---

## 🔄 实施步骤

### 第一阶段：后端改造（核心）

#### Step 1: ModelConfigServiceImpl 改造
```bash
文件：/home/moshu/xiaozhi-esp32-server/main/manager-api/src/main/java/xiaozhi/modules/model/service/impl/ModelConfigServiceImpl.java

改动：
1. 修改查询方法，添加用户过滤
2. 修改保存方法，自动设置creator
3. 修改更新/删除方法，添加权限校验
```

#### Step 2: ModelConfigController 改造
```bash
文件：/home/moshu/xiaozhi-esp32-server/main/manager-api/src/main/java/xiaozhi/modules/model/controller/ModelConfigController.java

改动：
1. API方法中获取当前用户ID
2. 传递给Service层
```

#### Step 3: ConfigServiceImpl 改造
```bash
文件：/home/moshu/xiaozhi-esp32-server/main/manager-api/src/main/java/xiaozhi/modules/config/service/impl/ConfigServiceImpl.java

改动：
1. buildModuleConfig方法添加权限校验
```

### 第二阶段：前端改造（UI）

#### Step 4: HeaderBar 改造
```bash
文件：/home/moshu/xiaozhi-esp32-server/main/manager-web/src/components/HeaderBar.vue

改动：
1. 移除第100行的 v-if="isSuperAdmin"
2. 让所有用户都能看到"模型配置"菜单
```

#### Step 5: ModelConfig 页面优化
```bash
文件：/home/moshu/xiaozhi-esp32-server/main/manager-web/src/views/ModelConfig.vue

改动：
1. 添加配置所有权标签（系统/我的）
2. 添加"复制为我的"功能
3. 优化操作按钮权限控制
```

### 第三阶段：测试验证

#### 测试场景
1. **普通用户测试**
   - [ ] 能看到"模型配置"菜单
   - [ ] 能看到系统默认配置和自己的配置
   - [ ] 不能看到其他用户的配置
   - [ ] 能编辑/删除自己的配置
   - [ ] 不能编辑/删除系统配置
   - [ ] 能复制系统配置为自己的配置
   - [ ] 创建智能体时只能选择自己有权访问的模型

2. **超级管理员测试**
   - [ ] 能看到所有配置
   - [ ] 能编辑系统配置
   - [ ] 能管理所有用户的配置

3. **安全性测试**
   - [ ] 用户A无法通过API访问用户B的配置
   - [ ] API密钥隔离正确
   - [ ] 配置下发权限正确

### 第四阶段：部署上线

#### 部署步骤
```bash
# 1. 编译后端
cd /home/moshu/xiaozhi-esp32-server/main/manager-api
mvn clean package -DskipTests

# 2. 编译前端
cd /home/moshu/xiaozhi-esp32-server/main/manager-web
npm run build

# 3. 重启服务
cd /home/moshu/xiaozhi-esp32-server/main/xiaozhi-server
sudo docker compose -f docker-compose_all.yml restart xiaozhi-esp32-server-web
```

---

## 🎁 附加功能（可选）

### 1. 模型配置模板市场
- 系统提供常用模型配置模板（MinimaxLLM、DoubaoASR等）
- 用户一键复制模板，填入自己的API密钥即可使用

### 2. 配置分享功能
- 用户可以将自己的配置（隐藏API密钥）分享给其他用户参考
- 其他用户可以导入配置框架，填入自己的密钥

### 3. 配置使用统计
- 显示每个配置的使用次数
- 帮助用户了解哪些模型最常用

### 4. API密钥管理
- 单独的"API密钥管理"页面
- 集中管理各个服务商的密钥
- 模型配置时可以引用密钥，而不是直接填写

---

## ⚠️ 注意事项

### 1. 数据迁移
现有的模型配置中，`creator` 字段可能为 `NULL`：
```sql
-- 查看需要迁移的数据
SELECT id, model_code, model_name, creator 
FROM ai_model_config 
WHERE creator IS NULL;

-- 策略：creator为NULL的配置保留为系统默认配置
-- 无需迁移，直接作为所有用户可见的模板
```

### 2. 向后兼容
- 保留系统默认配置（creator为NULL）
- 超级管理员保留所有权限
- 逐步引导用户创建自己的配置

### 3. 性能优化
- 模型配置查询添加索引：
```sql
CREATE INDEX idx_model_type_creator ON ai_model_config(model_type, creator);
CREATE INDEX idx_model_enabled ON ai_model_config(is_enabled);
```

### 4. 安全性
- API密钥在前端显示时脱敏（`****1234`）
- 后端返回配置时，敏感字段加密或脱敏
- 审计日志记录配置的创建/修改/删除操作

---

## 🎯 预期效果

### 用户体验
1. **自助服务**：用户无需管理员帮助，自行配置模型
2. **数据隔离**：每个用户的API密钥完全隔离
3. **快速上手**：复制系统模板，填入密钥即可使用
4. **灵活配置**：可以配置多个相同类型的模型（如多个LLM）

### 系统管理
1. **降低运维成本**：管理员无需为每个用户配置模型
2. **提升安全性**：用户数据完全隔离
3. **可扩展性**：支持大量用户并发使用
4. **商业化基础**：具备SaaS平台的多租户架构

---

## 📝 总结

### 核心改动
1. **后端**：3个Service类，6个方法
2. **前端**：2个Vue组件，4个改动点
3. **数据库**：无需改动（已具备基础）

### 改造难度
- **难度**：⭐⭐⭐ (中等)
- **工作量**：约4-6小时
- **风险**：低（向后兼容）

### 关键优势
✅ 数据库已有 `creator` 字段，无需迁移
✅ 前端已有完整的模型配置UI，只需调整权限
✅ 后端Service层代码结构清晰，易于扩展
✅ 向后兼容，不影响现有用户

---

## 🚀 下一步行动

请您确认：
1. ✅ 是否认可此方案？
2. ✅ 是否需要我开始实施改造？
3. ✅ 是否有其他需求需要补充？

我会按照上述步骤，逐个文件进行改造，并在每一步完成后请您测试验证。





