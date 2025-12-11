package com.ai.ai;
import com.ai.ai.Service.ChatService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 对话服务工厂类
 */
@Configuration
public class ChatServiceFactory {
    /**
     * 聊天模型
     */
    @Autowired
    private ChatModel chatModel;

    /**
     * 创建对话服务Bean的方法
     * @return 配置号的对话服务实例, 可以直接用来进行AI对话
     */
    @Bean
    public ChatService chatService() {
        //1. 创建了一个ChatService接口的实现
        //2. 把chatModel(聊天模型)和这个服务连接起来
        //3. 返回了一个可以直接使用的对话服务对象
        return AiServices.create(ChatService.class, chatModel);
    }

}