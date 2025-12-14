<template>
  <!-- 主容器 -->
  <div class="home-page">
    <!-- 聊天容器 -->
    <div class="chat-container">
      <!-- 聊天历史区域，可滚动 -->
      <div class="chat-history" ref="chatHistory">
        <!-- 欢迎消息 - 只在没有消息时显示 -->
        <div v-if="messages.length === 0" class="welcome-message">
          <div class="welcome-icon">👋</div>
          <h3>知识库与大语言模型的智能对话系统</h3>
        </div>

        <!-- 聊天消息列表 -->
        <div
            v-for="(message, index) in messages"
            :key="index"
            :class="['message', message.type]"
        >
          <!-- 消息头像 -->
          <div class="message-avatar">
            <!-- 用户头像 -->
            <div v-if="message.type === 'user'" class="avatar user-avatar">👤</div>
            <!-- AI头像 -->
            <div v-else class="avatar ai-avatar">🤖</div>
          </div>

          <!-- 消息内容包装器 -->
          <div class="message-content-wrapper">
            <!-- 消息内容区域 -->
            <div class="message-content">
              <!-- 消息文本 -->
              <div class="message-text">{{ message.content }}</div>

              <!-- 打字动画指示器 - 只在AI消息且正在加载时显示 -->
              <div v-if="message.type === 'ai' && isLoading && index === messages.length - 1"
                   class="typing-indicator">
                <div class="typing-dot"></div>
                <div class="typing-dot"></div>
                <div class="typing-dot"></div>
              </div>
            </div>

            <!-- 消息时间 -->
            <div class="message-time">{{ message.time }}</div>
          </div>
        </div>
      </div>

      <!-- 聊天输入区域 -->
      <div class="chat-input-container">
        <div class="input-wrapper">
          <!-- Element Plus 文本输入框 -->
          <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="2"
              :maxrows="4"
              placeholder="请输入您的问题..."
              :disabled="isLoading"
              @keydown.enter.exact.prevent="sendMessage"
              class="custom-input"
          ></el-input>

          <!-- 输入框操作区域 -->
          <div class="input-actions">
            <!-- 字符计数 -->
            <div class="char-count">{{ inputMessage.length }}/500</div>

            <!-- 发送按钮 -->
            <el-button
                type="primary"
                @click="sendMessage"
                :disabled="isLoading || !inputMessage.trim()"
                class="send-button"
                :class="{ loading: isLoading }"
            >
              <!-- 正常状态显示文本 -->
              <span v-if="!isLoading">发送</span>
              <!-- 加载状态显示旋转动画 -->
              <div v-else class="loading-spinner"></div>
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// Vue 组合式 API 导入
import {ref, nextTick, onBeforeUnmount} from 'vue'
// API 函数导入
import {sseChat} from '@/api/ai/streamChat'

// 模板引用 - 聊天历史区域的DOM元素
const chatHistory = ref(null)
// 响应式数据 - 输入框内容
const inputMessage = ref('')
// 响应式数据 - 消息列表
const messages = ref([])
// 响应式数据 - 加载状态
const isLoading = ref(false)
// 响应式数据 - 流式传输的文本内容
const streamingText = ref('')
// 响应式数据 - EventSource 实例引用
/** @type {import('vue').Ref<EventSource | null>} */
const eventSource = ref(null)

/**
 * 发送消息函数
 * 处理用户消息发送和AI响应接收
 */
const sendMessage = async () => {
  // 验证输入：空内容或正在加载时直接返回
  if (!inputMessage.value.trim() || isLoading.value) return

  // 创建用户消息对象
  const userMessage = {
    type: 'user',                                   // 消息类型：用户
    content: inputMessage.value,                    // 消息内容
    time: new Date().toLocaleTimeString()           // 当前时间
  }
  // 添加到消息列表
  messages.value.push(userMessage)

  // 保存要发送的消息并清空输入框
  const messageToSend = inputMessage.value
  inputMessage.value = ''

  // 立即滚动到底部（用户消息）
  setTimeout(() => {
    scrollToBottom()
  }, 0)

  try {
    // 开始加载状态
    isLoading.value = true
    streamingText.value = ''

    // 创建AI消息占位符
    const aiMessage = {
      type: 'ai',                                 // 消息类型：AI
      content: '',                                // 初始为空内容
      time: new Date().toLocaleTimeString()       // 当前时间
    }
    messages.value.push(aiMessage)

    // 生成简单的记忆ID（用于对话上下文）
    const memoryId = Date.now() % 1000000

    // 调用流式对话API
    sseChat(memoryId, messageToSend).then(es => {
      // 保存EventSource实例
      eventSource.value = es

      /**
       * 处理服务器发送的消息事件
       * @param {MessageEvent} event - 消息事件对象
       */
      eventSource.value.onmessage = (event) => {
        // 累加流式数据
        const chunk = event.data
        streamingText.value += chunk

        // 更新最后一条AI消息的内容
        const lastMessage = messages.value[messages.value.length - 1]
        if (lastMessage && lastMessage.type === 'ai') {
          lastMessage.content = streamingText.value
        }

        // 滚动到底部（实时更新内容）
        setTimeout(() => {
          scrollToBottom()
        }, 0)
      }

      /**
       * 处理SSE连接错误
       * @param {Event} error - 错误事件对象
       */
      eventSource.value.onerror = (error) => {
        console.error('SSE连接出错:', error)
        // 关闭连接
        if (eventSource.value) {
          eventSource.value.close()
        }
        // 结束加载状态
        isLoading.value = false

        // 更新最后一条消息内容（错误处理）
        const lastMessage = messages.value[messages.value.length - 1]
        if (lastMessage && lastMessage.type === 'ai') {
          lastMessage.content = streamingText.value || '抱歉，回复似乎出现了问题，请稍后重试。'
        }

        // 强制滚动到底部
        forceScrollToBottom()
        // 清空流式文本
        streamingText.value = ''
      }
    })

  } catch (error) {
    // 捕获并处理异常
    console.error('对话出错:', error)
    // 创建错误消息
    const errorMessage = {
      type: 'ai',
      content: '抱歉，我在处理您的问题时遇到了错误，请稍后再试。',
      time: new Date().toLocaleTimeString()
    }
    messages.value.push(errorMessage)
    // 重置状态
    isLoading.value = false
    streamingText.value = ''
  }
}

/**
 * 平滑滚动到底部
 * 使用nextTick确保DOM更新后执行
 */
const scrollToBottom = () => {
  nextTick(() => {
    if (chatHistory.value) {
      chatHistory.value.scrollTop = chatHistory.value.scrollHeight
    }
  })
}

/**
 * 强制滚动到底部
 * 不使用nextTick，立即执行
 */
const forceScrollToBottom = () => {
  if (chatHistory.value) {
    chatHistory.value.scrollTop = chatHistory.value.scrollHeight
  }
}

/**
 * 组件卸载前的生命周期钩子
 * 清理EventSource连接
 */
onBeforeUnmount(() => {
  if (eventSource.value) {
    eventSource.value.close()
  }
})
</script>

<style scoped>
/*
 * 主页面样式
 * 使用flex布局创建全屏渐变背景
 */
.home-page {
  display: flex;
  flex-direction: column;
  height: 92vh; /* 视口高度的92% */
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8f0 100%); /* 渐变背景 */
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; /* 字体栈 */
}

/*
 * 聊天容器
 * 居中布局，最大宽度限制
 */
.chat-container {
  display: flex;
  flex-direction: column;
  flex: 1; /* 填充剩余空间 */
  max-width: 1200px; /* 最大宽度 */
  margin: 0 auto; /* 水平居中 */
  width: 100%; /* 宽度100% */
  padding: 20px; /* 内边距 */
  box-sizing: border-box; /* 盒模型计算 */
}

/*
 * 聊天历史区域
 * 可滚动区域，白色卡片样式
 */
.chat-history {
  flex: 1; /* 填充剩余空间 */
  overflow-y: auto; /* 垂直滚动 */
  padding: 20px; /* 内边距 */
  border-radius: 16px; /* 圆角 */
  background-color: white; /* 白色背景 */
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05); /* 阴影效果 */
  margin-bottom: 20px; /* 底部外边距 */
  scroll-behavior: smooth; /* 平滑滚动 */
  max-height: calc(100vh - 300px); /* 最大高度计算 */
}

/*
 * 欢迎消息样式
 * 居中显示，较大内边距
 */
.welcome-message {
  text-align: center; /* 文本居中 */
  padding: 40px 20px; /* 内边距 */
  color: #495057; /* 文字颜色 */
}

/*
 * 欢迎图标样式
 */
.welcome-icon {
  font-size: 48px; /* 图标大小 */
  margin-bottom: 16px; /* 底部外边距 */
}

/*
 * 欢迎标题样式
 */
.welcome-message h3 {
  margin: 0 0 12px; /* 外边距 */
  font-size: 22px; /* 字体大小 */
  font-weight: 600; /* 字体粗细 */
}

/*
 * 消息容器
 * 使用flex布局，添加动画效果
 */
.message {
  display: flex; /* flex布局 */
  margin-bottom: 24px; /* 底部外边距 */
  animation: fadeIn 0.3s ease; /* 淡入动画 */
}

/*
 * 淡入动画定义
 * 从下方淡入效果
 */
@keyframes fadeIn {
  from {
    opacity: 0; /* 完全透明 */
    transform: translateY(10px); /* 向下偏移 */
  }
  to {
    opacity: 1; /* 完全不透明 */
    transform: translateY(0); /* 恢复位置 */
  }
}

/*
 * 用户消息特殊样式
 * 反向排列（头像在右边）
 */
.message.user {
  flex-direction: row-reverse; /* 反向flex排列 */
}

/*
 * 消息头像容器
 */
.message-avatar {
  margin: 0 12px; /* 水平外边距 */
}

/*
 * 头像通用样式
 * 圆形设计，阴影效果
 */
.avatar {
  width: 40px; /* 固定宽度 */
  height: 40px; /* 固定高度 */
  border-radius: 50%; /* 圆形 */
  display: flex; /* flex布局 */
  align-items: center; /* 垂直居中 */
  justify-content: center; /* 水平居中 */
  font-size: 18px; /* 图标大小 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); /* 阴影效果 */
}

/*
 * 用户头像样式
 * 蓝色渐变背景
 */
.user-avatar {
  background: linear-gradient(135deg, #3498db, #2980b9); /* 蓝色渐变 */
  color: white; /* 白色图标 */
}

/*
 * AI头像样式
 * 紫色渐变背景
 */
.ai-avatar {
  background: linear-gradient(135deg, #9b59b6, #8e44ad); /* 紫色渐变 */
  color: white; /* 白色图标 */
}

/*
 * 消息内容包装器
 * 限制最大宽度
 */
.message-content-wrapper {
  max-width: 70%; /* 最大宽度70% */
}

/*
 * 消息内容区域
 * 气泡样式设计
 */
.message-content {
  padding: 12px 18px; /* 内边距 */
  border-radius: 18px; /* 圆角 */
  position: relative; /* 相对定位 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08); /* 阴影效果 */
}

/*
 * 用户消息内容样式
 * 蓝色气泡，右下角小三角
 */
.message.user .message-content {
  background: linear-gradient(135deg, #3498db, #2980b9); /* 蓝色渐变 */
  color: white; /* 白色文字 */
  border-bottom-right-radius: 4px; /* 右下角小圆角 */
}

/*
 * AI消息内容样式
 * 浅灰色气泡，左下角小三角
 */
.message.ai .message-content {
  background: #f8f9fa; /* 浅灰色背景 */
  color: #495057; /* 深灰色文字 */
  border-bottom-left-radius: 4px; /* 左下角小圆角 */
  border: 1px solid #eef0f5; /* 边框 */
}

/*
 * 消息文本样式
 * 处理换行和长文本
 */
.message-text {
  line-height: 1.5; /* 行高 */
  white-space: pre-wrap; /* 保留空格和换行 */
  word-wrap: break-word; /* 长单词换行 */
}

/*
 * 消息时间样式
 * 小字体，浅色
 */
.message-time {
  font-size: 12px; /* 小字体 */
  color: #adb5bd; /* 浅灰色 */
  margin-top: 6px; /* 顶部外边距 */
  padding: 0 4px; /* 水平内边距 */
}

/*
 * 打字指示器容器
 */
.typing-indicator {
  display: flex; /* flex布局 */
  align-items: center; /* 垂直居中 */
  margin-top: 8px; /* 顶部外边距 */
}

/*
 * 打字点样式
 * 圆点动画效果
 */
.typing-dot {
  width: 8px; /* 点宽度 */
  height: 8px; /* 点高度 */
  border-radius: 50%; /* 圆形 */
  background-color: #adb5bd; /* 浅灰色 */
  margin: 0 2px; /* 水平间距 */
  animation: typing 1.4s infinite ease-in-out; /* 打字动画 */
}

/*
 * 打字点动画延迟
 * 创建波浪效果
 */
.typing-dot:nth-child(1) {
  animation-delay: -0.32s; /* 第一个点延迟 */
}

.typing-dot:nth-child(2) {
  animation-delay: -0.16s; /* 第二个点延迟 */
}

/*
 * 聊天输入容器
 * 白色卡片样式
 */
.chat-input-container {
  background: white; /* 白色背景 */
  border-radius: 16px; /* 圆角 */
  padding: 20px; /* 内边距 */
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05); /* 阴影效果 */
}

/*
 * 输入包装器
 * 相对定位容器
 */
.input-wrapper {
  position: relative; /* 相对定位 */
}

/*
 * 自定义输入框深度样式
 * 使用:deep选择器修改Element Plus组件样式
 */
.custom-input :deep(.el-textarea__inner) {
  border-radius: 12px; /* 圆角 */
  border: 1px solid #eef0f5; /* 边框 */
  padding: 16px; /* 内边距 */
  font-size: 16px; /* 字体大小 */
  resize: none; /* 禁止调整大小 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05); /* 阴影效果 */
  transition: all 0.3s ease; /* 过渡动画 */
}

/*
 * 输入框聚焦状态
 */
.custom-input :deep(.el-textarea__inner:focus) {
  border-color: #3498db; /* 蓝色边框 */
  box-shadow: 0 2px 12px rgba(52, 152, 219, 0.2); /* 蓝色阴影 */
}

/*
 * 输入操作区域
 * 底部操作按钮和计数
 */
.input-actions {
  display: flex; /* flex布局 */
  justify-content: space-between; /* 两端对齐 */
  align-items: center; /* 垂直居中 */
  margin-top: 12px; /* 顶部外边距 */
}

/*
 * 字符计数样式
 */
.char-count {
  font-size: 14px; /* 字体大小 */
  color: #adb5bd; /* 浅灰色 */
}

/*
 * 发送按钮样式
 */
.send-button {
  border-radius: 12px; /* 圆角 */
  padding: 10px 24px; /* 内边距 */
  font-weight: 600; /* 字体粗细 */
  transition: all 0.3s ease; /* 过渡动画 */
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3); /* 阴影效果 */
  border: none; /* 无边框 */
}

/*
 * 发送按钮悬停效果（非禁用状态）
 */
.send-button:hover:not(.is-disabled) {
  transform: translateY(-2px); /* 向上移动 */
  box-shadow: 0 6px 16px rgba(52, 152, 219, 0.4); /* 增强阴影 */
}

/*
 * 加载状态下的发送按钮
 */
.send-button.loading {
  background-color: #bdc3c7; /* 灰色背景 */
  pointer-events: none; /* 禁用鼠标事件 */
}

/*
 * 加载旋转动画
 */
.loading-spinner {
  width: 18px; /* 宽度 */
  height: 18px; /* 高度 */
  border: 2px solid transparent; /* 透明边框 */
  border-top: 2px solid white; /* 顶部白色边框 */
  border-radius: 50%; /* 圆形 */
  animation: spin 1s linear infinite; /* 旋转动画 */
}

/*
 * 旋转动画定义
 */
@keyframes spin {
  0% {
    transform: rotate(0deg); /* 起始角度 */
  }
  100% {
    transform: rotate(360deg); /* 结束角度 */
  }
}

/*
 * 打字动画定义
 * 点的缩放效果
 */
@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0.8); /* 缩小 */
    opacity: 0.5; /* 半透明 */
  }
  40% {
    transform: scale(1); /* 正常大小 */
    opacity: 1; /* 不透明 */
  }
}
</style>
