package com.ai.ai.Service;

import com.ai.ai.domain.Report;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

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

    /**
     * 获取报告
     * @param message 用户输入的消息内容
     * @return 格式化之后的报告内容
     */
    @SystemMessage(fromResource = "prompt.txt")
    Report getReport(String message);


    /**
     * 使用RAG与AI进行对话
     * @param message 用户输入的消息内容
     * @return 包含RAG处理结果的封装对象
     *         - Ai生成的回答内容
     *         - 相关的检索来源信息
     */
    @SystemMessage(fromResource = "prompt.txt")
    Result<String> getChatRag(String message);

    /**
     * 基于sse的流逝对话接口
     * @param memoryId 对话记忆ID，用于标识和维持对话的上下问记忆
     * @param message 用户输入的消息内容
     * @return 返回一个Flux，包含Ai实时回复的片段
     */
    @SystemMessage(fromResource = "prompt.txt")
    Flux<String> sseChat(@MemoryId int memoryId,@UserMessage String message);

}
