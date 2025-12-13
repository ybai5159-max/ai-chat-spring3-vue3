package com.ai.ai.controller;

import com.ai.ai.Service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * 对话接口
     * @param memoryId 对话记忆ID, 用于标识和维持对话的上下文记忆
     * @param message 用户输入的消息内容
     * @return 返回一个数据流
     */
    @GetMapping("/sse")
    public Flux<ServerSentEvent<String>> sseChat(int memoryId, String message){
        // 调用对话服务的流式聊天方法，获取AI的回复数据流
        return chatService.sseChat(memoryId,message)
                // 将每个回复片段包装成ServerSentEvent,方便前端接收
                .map(chunk -> ServerSentEvent.<String>builder()
                .data(chunk)
                .build());
    }
}
