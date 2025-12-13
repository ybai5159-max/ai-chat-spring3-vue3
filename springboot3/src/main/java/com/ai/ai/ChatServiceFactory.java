package com.ai.ai;
import com.ai.ai.Service.ChatService;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
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
     * 内容检索器
     */
    @Autowired
    private ContentRetriever contentRetriever;

    /**
     * 流式聊天模型
     */
    @Autowired
    private StreamingChatModel streamingChatModel;

    /**
     * 创建对话服务Bean的方法
     * @return 配置好的对话服务实例, 可以直接用来进行AI对话
     */
    @Bean
    public ChatService chatService() {
        // 创建基于消息窗口的聊天记忆，最多保留10条消息
        // 这种记忆机制可以保持对话的连贯性，同时防止了内存无限增长
//        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        // 使用AIservices创建对话服务实例
        ChatService codeService = AiServices.builder(ChatService.class)
                .chatModel(chatModel) // 设置AI聊天模型
                .streamingChatModel(streamingChatModel) // 实现流式输出
//                .chatMemory(chatMemory) // 设置聊天记忆，维持对话上下文
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10)) // 设置聊天记忆提供者，为每个对话创建独立的对话窗口
                .contentRetriever(contentRetriever) // 设置内容检索器，启用RAG功能，并且从知识库检索相关的信息增强AI回答
                .build(); // 构建完整的对话服务实例

        // 返回对话服务实例
        return codeService;
    }

}