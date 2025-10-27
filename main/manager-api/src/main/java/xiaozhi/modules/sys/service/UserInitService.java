package xiaozhi.modules.sys.service;

/**
 * 用户初始化服务
 * 负责在新用户注册时初始化默认配置
 *
 * @author system
 */
public interface UserInitService {

    /**
     * 初始化用户的默认模型配置
     * 从admin用户复制所有配置并清空敏感信息
     *
     * @param userId 新用户ID
     */
    void initUserModelConfigs(Long userId);
}



