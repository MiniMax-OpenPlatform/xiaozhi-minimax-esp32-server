package xiaozhi.modules.sys.service.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xiaozhi.modules.model.dao.ModelConfigDao;
import xiaozhi.modules.model.entity.ModelConfigEntity;
import xiaozhi.modules.sys.dao.SysUserDao;
import xiaozhi.modules.sys.entity.SysUserEntity;
import xiaozhi.modules.sys.service.UserInitService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 用户初始化服务实现
 *
 * @author system
 */
@Service
@AllArgsConstructor
@Slf4j
public class UserInitServiceImpl implements UserInitService {

    private final ModelConfigDao modelConfigDao;
    private final SysUserDao sysUserDao;
    
    // 需要清空的敏感字段列表
    private static final List<String> SENSITIVE_FIELDS = Arrays.asList(
        "api_key", "apiKey", "apikey", 
        "access_token", "accessToken", "access_key", "accessKey",
        "secret_key", "secretKey", "secret",
        "password", "passwd", "pwd",
        "token", "appid", "app_id", "group_id", "groupId",
        "url", "base_url", "baseUrl", "endpoint",  // 添加URL类字段，可能包含敏感信息
        "account", "username", "user",  // 添加账号类字段
        "host", "server"  // 添加服务器地址类字段
    );
    
    // 新用户默认初始化的模型配置列表（model_code）
    private static final List<String> DEFAULT_ASR_MODELS = Arrays.asList(
        "FunASR",           // FunASR语音识别
        "FunASRServer",     // FunASR服务器
        "TencentASR"        // 腾讯语音识别
    );
    
    private static final List<String> DEFAULT_LLM_MODELS = Arrays.asList(
        "MinimaxLLM"        // MinimaxLLM
    );
    
    private static final List<String> DEFAULT_TTS_MODELS = Arrays.asList(
        "MinimaxStreamTTS"  // Minimax流式语音合成
    );

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initUserModelConfigs(Long userId) {
        log.info("开始为用户 {} 初始化默认模型配置", userId);
        
        // 查找第一个超级管理员用户作为模板来源
        QueryWrapper<SysUserEntity> userWrapper = new QueryWrapper<>();
        userWrapper.eq("super_admin", 1);
        userWrapper.orderByAsc("id");
        userWrapper.last("LIMIT 1");
        SysUserEntity adminUser = sysUserDao.selectOne(userWrapper);
        
        if (adminUser == null) {
            log.error("未找到超级管理员用户，无法初始化配置");
            return;
        }
        
        log.info("从超级管理员 {} (ID:{}) 复制配置", adminUser.getUsername(), adminUser.getId());
        
        // 查找超级管理员的所有配置作为模板
        QueryWrapper<ModelConfigEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("creator", adminUser.getId());
        wrapper.orderByAsc("sort");
        
        List<ModelConfigEntity> templateConfigs = modelConfigDao.selectList(wrapper);
        
        if (templateConfigs == null || templateConfigs.isEmpty()) {
            log.warn("超级管理员 {} 没有配置，跳过用户 {} 的配置初始化", adminUser.getUsername(), userId);
            return;
        }
        
        log.info("找到 {} 个模板配置，开始过滤并复制", templateConfigs.size());
        
        List<ModelConfigEntity> newConfigs = new ArrayList<>();
        int filteredCount = 0;
        
        for (ModelConfigEntity template : templateConfigs) {
            // 过滤：只复制指定的模型配置
            if (!shouldCopyConfig(template)) {
                filteredCount++;
                continue;
            }
            
            ModelConfigEntity newConfig = new ModelConfigEntity();
            
            // 复制基本信息
            newConfig.setModelType(template.getModelType());
            newConfig.setModelCode(template.getModelCode());
            newConfig.setModelName(template.getModelName());
            newConfig.setIsEnabled(template.getIsEnabled());
            newConfig.setSort(template.getSort());
            
            // 复制配置JSON并清空敏感信息
            JSONObject cleanedConfigJson = clearSensitiveFields(template.getConfigJson());
            newConfig.setConfigJson(cleanedConfigJson);
            
            // 设置创建者为新用户
            newConfig.setCreator(userId);
            
            newConfigs.add(newConfig);
        }
        
        log.info("过滤了 {} 个配置，将复制 {} 个配置", filteredCount, newConfigs.size());
        
        // 批量插入
        for (ModelConfigEntity config : newConfigs) {
            modelConfigDao.insert(config);
        }
        
        log.info("成功为用户 {} 复制了 {} 个配置", userId, newConfigs.size());
    }
    
    /**
     * 判断是否应该复制该配置
     * 只复制指定的ASR、LLM、TTS模型，以及所有其他类型的模型（Memory、VAD、Intent等）
     */
    private boolean shouldCopyConfig(ModelConfigEntity config) {
        String modelType = config.getModelType();
        String modelCode = config.getModelCode();
        
        if (modelType == null || modelCode == null) {
            return false;
        }
        
        // 统一转换为小写进行比较
        String type = modelType.toLowerCase();
        
        switch (type) {
            case "asr":
                // ASR：只复制指定的模型
                return DEFAULT_ASR_MODELS.contains(modelCode);
                
            case "llm":
                // LLM：只复制指定的模型
                return DEFAULT_LLM_MODELS.contains(modelCode);
                
            case "tts":
                // TTS：只复制指定的模型
                return DEFAULT_TTS_MODELS.contains(modelCode);
                
            case "memory":
            case "vad":
            case "intent":
            case "vllm":
            case "voiceprint":
                // Memory、VAD、Intent、VLLM、Voiceprint等其他类型全部复制
                return true;
                
            default:
                // 其他未知类型也复制
                log.warn("遇到未知模型类型: {}, modelCode: {}", modelType, modelCode);
                return true;
        }
    }
    
    /**
     * 清空配置JSON中的敏感字段
     */
    private JSONObject clearSensitiveFields(JSONObject configJson) {
        if (configJson == null || configJson.isEmpty()) {
            return new JSONObject();
        }
        
        try {
            // 创建副本
            JSONObject cleaned = new JSONObject(configJson);
            
            // 遍历并清空敏感字段
            for (String field : SENSITIVE_FIELDS) {
                if (cleaned.containsKey(field)) {
                    cleaned.set(field, "");
                    log.debug("清空敏感字段: {}", field);
                }
            }
            
            return cleaned;
        } catch (Exception e) {
            log.error("清空敏感字段失败，返回空JSON", e);
            return new JSONObject();
        }
    }
}

