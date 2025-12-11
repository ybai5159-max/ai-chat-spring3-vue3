package com.ai.ai.Service.impl;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
/**
 * 对话服务实现类
 */
public class ChatServiceImpl {
    //自动注入AI聊天模型
    @Autowired
    private ChatModel chatModel;

    //定义提示词
    private static final String prompt = """
    你是一个专业的AI助手，专注于帮助开发者解决技术问题。

    核心能力：
    1. 解答技术问题
    2. 提供代码建议和最佳实践
    3. 协助分析和解决编程难题

    回答准则：
    - 使用清晰易懂的技术语言
    - 提供具体可执行的代码示例
    - 保持专业且友好的交流风格
    - 遇到不确定情况时诚实说明
    """;


    /**
     * 与AI进行对话的主要方法
     * @param userMessage 用户输入的消息对象包含用户的问题或者对话内容
     * @return AI生成的回答文本
     */
    public String chat(UserMessage userMessage){
        // 创建系统消息：从预定义的提示词模版生成系统级指令
        //相当于为AI设定了角色和对话规则，比如：“你是一个专注于帮助开发者解决技术问题的助手”
        SystemMessage systemMessage = SystemMessage.from(prompt);

        // 第一步：把用户消息发送给AI，等待AI思考并且生成回复
        ChatResponse res = chatModel.chat(systemMessage,userMessage);
        System.out.println(res);

        // 第二步：从AI的完整回复中，专门提取出文字内容部分
        AiMessage aiMessage = res.aiMessage();

        // 第三步，返回纯文本的AI回答给调用者
        return aiMessage.text();
    }
}
