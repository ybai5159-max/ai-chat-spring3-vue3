package com.ai.ai.Service;

import dev.langchain4j.service.SystemMessage;

/**
 * 对话服务接口
 * 这个服务提供了与AI对话的能力，可以理解为是一个智能聊天机器人的核心接口
 */
public interface ChatService {
    /**
     * 与AI进行对话
     * @param message 用户输入的消息内容
     * @return AI根据用户消息生成的回复内容
     *
     * @SystemMessage 注解说明
     *  - fromResource = "prompt.txt" 表示从项目资源目录下的prompt.txt文件中读取系统提示词
     *  - 系统提示词就像是给AI的工作说明书，告诉他应该扮演什么角色
     */
    @SystemMessage(fromResource = "prompt.txt")
    String chat(String message);

}
