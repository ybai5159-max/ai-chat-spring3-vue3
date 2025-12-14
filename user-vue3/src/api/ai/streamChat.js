// 流式对话接口
export function sseChat(memoryId, message) {
    return new Promise((resolve) => {
        // 获取基础api地址
        const baseUrl = import.meta.env.VITE_APP_BASE_API;

        const eventSource = new EventSource(
            `${baseUrl}/ai/chat/sse?memoryId=${memoryId}&message=${message}`
        )

        resolve(eventSource)
    })
}