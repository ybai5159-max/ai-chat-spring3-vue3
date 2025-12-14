<template>
  <div class="dashboard">
    <div class="welcome-section">
      <div class="welcome-content">
        <h1 class="welcome-title">欢迎使用知识库与大语言模型的智能对话系统</h1>
      </div>
    </div>
  </div>
</template>

<script setup >

</script>

<style scoped lang="scss">
.dashboard {
  padding: 20px;
  background-color: #f5f7fa;
}

.welcome-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 60px 40px;
  margin-bottom: 30px;
  color: #fff;

  .welcome-content {
    flex: 1;

    .welcome-title {
      font-size: 36px;
      font-weight: bold;
      margin-bottom: 20px;
    }
  }
}
</style>
