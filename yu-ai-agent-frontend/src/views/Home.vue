<template>
  <div class="home-page">
    <div class="home-inner">
      <div class="top-tip">
        <span class="tip-dot"></span>
        <span>多轮对话、知识检索与智能体执行一体化体验</span>
      </div>

      <section class="hero-section">
        <div class="brand">companion</div>
        <h1 class="headline">更温柔地陪你说话，也更认真地帮你解决问题</h1>
        <p class="summary">
          面向情感陪伴场景打造的 AI 助手平台，支持连续对话、知识问答、文档上传、工具调用与多步骤任务执行。
        </p>
      </section>

      <section class="card-grid">
        <article class="feature-card feature-card-primary" @click="navigateTo('/love-master')">
          <div class="feature-icon">♥</div>
          <div class="feature-title">情感陪伴助手</div>
          <div class="feature-desc">
            适合倾诉、关系分析、情绪疏导与温和建议，整体体验更贴近陪伴式对话。
          </div>
          <div class="feature-link">开始对话</div>
        </article>

        <article class="feature-card" @click="navigateTo('/super-agent')">
          <div class="feature-icon feature-icon-calm">✦</div>
          <div class="feature-title">超级智能体</div>
          <div class="feature-desc">
            适合多步骤任务处理，可联动外部工具完成搜索、分析、整理与内容生成。
          </div>
          <div class="feature-link">立即体验</div>
        </article>
      </section>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'

useHead({
  title: '情感对话智能体平台',
  meta: [
    {
      name: 'description',
      content: '面向情感陪伴与智能任务执行场景的 AI 应用平台，支持多轮对话、知识问答、工具调用与智能体执行。'
    },
    {
      name: 'keywords',
      content: '情感助手,AI 陪伴,智能体,RAG,Tool Calling,MCP'
    }
  ]
})

const router = useRouter()

const navigateTo = (path) => {
  router.push(path)
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  padding: 36px 20px 56px;
  background:
    radial-gradient(circle at top left, rgba(252, 217, 204, 0.58), transparent 24%),
    radial-gradient(circle at 92% 18%, rgba(204, 221, 242, 0.5), transparent 24%),
    linear-gradient(180deg, #fffdfb 0%, #fff8f3 55%, #fff4ee 100%);
}

.home-inner {
  max-width: 1040px;
  margin: 0 auto;
}

.top-tip {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(224, 201, 189, 0.85);
  color: #7c6760;
  font-size: 14px;
  box-shadow: 0 10px 30px rgba(204, 179, 167, 0.18);
}

.tip-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e6917e, #d46c5c);
  box-shadow: 0 0 0 5px rgba(228, 145, 126, 0.15);
}

.hero-section {
  padding: 60px 0 44px;
  text-align: center;
}

.brand {
  font-size: clamp(40px, 7vw, 72px);
  line-height: 0.98;
  font-weight: 700;
  letter-spacing: -0.06em;
  color: #dd7a66;
}

.headline {
  max-width: 980px;
  margin: 16px auto 0;
  font-size: clamp(26px, 4.2vw, 46px);
  line-height: 1.24;
  letter-spacing: -0.04em;
  color: #3d312c;
  text-wrap: balance;
}

.summary {
  max-width: 760px;
  margin: 18px auto 0;
  font-size: 17px;
  line-height: 1.8;
  color: #7d6a63;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 26px;
}

.feature-card {
  min-height: 240px;
  padding: 30px;
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(230, 214, 206, 0.95);
  box-shadow: 0 20px 50px rgba(199, 167, 153, 0.16);
  cursor: pointer;
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
}

.feature-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 28px 64px rgba(196, 158, 142, 0.2);
}

.feature-card-primary:hover {
  border-color: rgba(223, 133, 111, 0.6);
}

.feature-card:hover:not(.feature-card-primary) {
  border-color: rgba(133, 168, 196, 0.6);
}

.feature-icon {
  width: 62px;
  height: 62px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18px;
  background: linear-gradient(135deg, #ef9c88, #da6e5c);
  color: #fff;
  font-size: 30px;
  box-shadow: 0 16px 34px rgba(221, 114, 93, 0.24);
}

.feature-icon-calm {
  background: linear-gradient(135deg, #99b9d5, #7e9cb7);
  box-shadow: 0 16px 34px rgba(126, 156, 183, 0.22);
}

.feature-title {
  margin-top: 26px;
  font-size: 32px;
  font-weight: 700;
  letter-spacing: -0.04em;
  color: #3c312d;
}

.feature-desc {
  margin-top: 14px;
  font-size: 16px;
  line-height: 1.85;
  color: #7e6d66;
}

.feature-link {
  margin-top: 28px;
  font-size: 17px;
  font-weight: 600;
  color: #4b3d38;
}

@media (max-width: 900px) {
  .hero-section {
    padding: 44px 0 34px;
  }

  .card-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .home-page {
    padding: 20px 14px 36px;
  }

  .top-tip {
    width: 100%;
    justify-content: center;
    text-align: center;
    line-height: 1.6;
  }

  .hero-section {
    padding: 34px 0 24px;
  }

  .summary {
    font-size: 15px;
    line-height: 1.8;
  }

  .feature-card {
    min-height: 220px;
    padding: 24px;
  }

  .feature-title {
    font-size: 26px;
  }
}
</style>
