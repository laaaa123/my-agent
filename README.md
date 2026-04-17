# AI 情感助手与超级智能体项目
**作者**：zmy
**项目定位**：AI 应用实战项目（情感助手 + 超级智能体）
**技术方向**：Spring AI / RAG / Tool Calling / MCP / Agent

--2026-4.16---

## 📖 项目介绍
本项目是一套面向真实业务场景的企业级 AI 应用工程，核心由两大模块组成，绝非简单 Demo，旨在沉淀可复用、可扩展的企业级 AI 应用架构，覆盖对话编排、知识检索策略、会话隔离、可观测性、前后端协同与工程规范全链路，为 AI 应用工程化落地提供完整实践方案。
<img width="1160" height="721" alt="总览" src="https://github.com/user-attachments/assets/02b69e2d-5898-43a7-ba22-8bfab1c8c549" />


### 1. AI 情感助手应用
支持多轮对话、会话记忆持久化、会话级知识库问答、工具调用与流式输出，为用户在情感沟通、关系决策、情绪疏导等场景提供专业、可信赖的 AI 辅助能力。
<img width="1914" height="725" alt="情感助手" src="https://github.com/user-attachments/assets/5ceddc7f-68d9-4d7b-a56d-ebeb7f8d053a" />



### 2. AI 超级智能体
具备任务拆解、自主规划、多工具协作执行能力，可灵活扩展至信息检索、网页分析、内容生成、结构化文档输出等复杂业务场景，实现从“对话式 AI”到“任务式智能体”的能力跃迁。
<img width="1910" height="730" alt="智能体" src="https://github.com/user-attachments/assets/bd9c6d66-001b-47e1-8c1b-195f5bd7404c" />
<img width="1066" height="530" alt="智能体2" src="https://github.com/user-attachments/assets/a42ccdbb-ff51-4b8e-984d-841cd2cd8f92" />


## ✨ 核心功能特性
### 1. AI 情感助手
- 支持同步对话与 SSE 流式对话，提升交互体验
- 支持多轮上下文记忆，保障对话连贯性
- 支持会话历史持久化（MySQL 存储），刷新不丢失
- 支持结构化能力扩展，可生成情感报告、沟通建议等

### 2. 会话管理
- 前端左侧会话栏，支持会话创建、切换、删除
- 会话 ID 持久化，页面刷新后可完整恢复会话状态
- 会话级隔离设计，每个会话独立消息历史，杜绝串扰

### 3. 知识库能力（RAG）
- 支持 `md` / `pdf` / `docx` 多格式文档上传
- 完整文档处理链路：解析 → 切分 → 文本清洗 → 向量化 → 入库
- 会话级知识隔离，文档与会话绑定，检索自动按会话过滤
- 公共知识 + 会话知识混合召回策略，兼顾通用性与场景化
- 上传后返回入库分片数，提供可检索性即时反馈

### 4. 工具与 MCP 扩展
- 原生支持 Tool Calling 能力，可灵活扩展业务工具
- 支持 MCP 协议客户端接入，兼容主流 AI 工具生态
- 为超级智能体提供可插拔的任务执行能力扩展

### 5. 前端交互体验
- 类 DeepSeek 左右布局，左侧会话管理、右侧对话交互
- SSE 流式输出实时渲染，模拟真实对话感
- 文档上传结果即时反馈，操作状态可视化
- 对话内容规范化展示，优化可读性与信息层级

---

## 🏗️ 技术架构与选型
### 后端技术栈
- 核心框架：Java 21 + Spring Boot 3 + Spring AI
- 数据存储：MySQL（会话、消息、记忆持久化）
- 向量检索：自研向量存储实现，可无缝扩展 PGVector / Milvus
- 通信能力：SSE 流式响应，支持实时对话输出

### 前端技术栈
- 核心框架：Vue 3 + Vite
- 网络通信：Axios + EventSource（SSE 流式通信）
- 交互设计：类主流 AI 产品的现代化交互体验

### AI 能力层
- Prompt 编排与意图路由
- RAG 检索与混合召回策略
- Tool Calling 工具调用框架
- MCP 协议标准化接入
- Agent 自主规划与任务执行

---

## 🧩 企业级模块划分
### 1. 应用编排层
- 职责：意图路由、对话分发、结果统一输出，是系统的调度中枢
- 典型模块：`LoveApp`（已完成编排职责重构，解耦业务逻辑）

### 2. 知识检索层
- 职责：召回策略控制、会话隔离校验、回退机制、命中日志埋点
- 典型模块：`KnowledgeRetrievalService`

### 3. 提示词治理层
- 职责：统一管理系统提示词与业务提示模板，避免散落在业务代码中
- 典型模块：`EmotionalAssistantPrompts`

### 4. 入库处理层
- 职责：文档解析、切分、文本清洗、向量入库、分片统计
- 典型模块：`KnowledgeBaseIngestionService`

### 5. 会话与记忆层
- 职责：会话创建、消息落库、记忆策略（摘要 + 最近消息）
- 典型模块：`Chat Session` / `Chat Memory` 相关服务

---

## 🔄 核心业务流程
### 流程一：对话请求链路
`用户输入 → 意图路由 → 普通对话 / 知识问答 / 工具调用 → SSE 流式返回前端`

### 流程二：知识库问答链路
`上传文档 → 文档解析与入库 → 混合召回（会话+公共知识） → 上下文组装 → 生成回答`

### 流程三：会话隔离保障链路
`会话创建 → 文档绑定会话 → 检索按会话过滤 → 杜绝跨会话文档串读`

---

## 🎯 项目核心亮点
✅ 会话级 RAG 隔离机制：从架构层面解决多用户、多会话知识串读问题，保障数据安全
✅ 混合召回 + 回退检索策略：大幅提升新文档首轮可命中率，优化 RAG 效果
✅ 分层解耦架构设计：完成编排层与检索层解耦重构，系统可维护性、扩展性显著提升
✅ 完整工程化闭环：支持 SSE 流式对话、会话历史持久化、文档上传反馈全链路
✅ 企业级扩展能力：原生支持 Tool Calling、MCP 协议、Agent 自主规划，适配复杂业务场景

---

## 🚀 后续迭代计划（Roadmap）
1.  向量存储性能升级：接入 PGVector / Milvus，支撑大规模向量检索场景
2.  检索能力量化体系：增加命中率、召回率、延迟等检索评估指标
3.  自动化测试体系：补充检索策略回归、会话隔离回归等自动化用例
4.  知识库运营能力：搭建知识库管理台，支持文档审核、版本控制
5.  多租户权限体系：完善租户隔离、会话级权限控制，支撑 SaaS 化部署

---

## 📌 项目价值
本项目完整覆盖了企业级 AI 应用从 0 到 1 的工程化落地全流程，可直接作为简历核心亮点项目，沉淀的架构设计、问题解决方案（如会话隔离、RAG 召回策略）可直接复用在各类 AI Agent、RAG 应用开发中，为求职与工程落地提供双重支撑。

## 📌 项目代码介绍
yu-ai-agent-master/
├─ src/
│  ├─ main/
│  │  ├─ java/
│  │  │  └─ com/yupi/yuaiagent/
│  │  │     ├─ YuAiAgentApplication.java
│  │  │     │  作用：Spring Boot 启动入口。
│  │  │     │
│  │  │     ├─ advisor/
│  │  │     │  作用：AI 调用过程中的增强器 / 拦截器。
│  │  │     │  ├─ MyLoggerAdvisor.java
│  │  │     │  │  作用：记录大模型调用日志，方便调试。
│  │  │     │  └─ ReReadingAdvisor.java
│  │  │     │     作用：对模型对话过程做补充控制。
│  │  │     │
│  │  │     ├─ agent/
│  │  │     │  作用：通用 Agent 抽象与实现。
│  │  │     │  ├─ BaseAgent.java
│  │  │     │  │  作用：Agent 基类。
│  │  │     │  ├─ ReActAgent.java
│  │  │     │  │  作用：ReAct 风格 Agent。
│  │  │     │  ├─ ToolCallAgent.java
│  │  │     │  │  作用：偏工具调用型 Agent。
│  │  │     │  ├─ YuManus.java
│  │  │     │  │  作用：项目里的 Manus 风格智能体实现。
│  │  │     │  └─ model/
│  │  │     │     作用：Agent 运行状态模型。
│  │  │     │
│  │  │     ├─ app/
│  │  │     │  作用：应用编排层，是核心业务层。
│  │  │     │  ├─ LoveApp.java
│  │  │     │  │  作用：情感助手总入口，负责意图识别、工具调用、知识库问答、流式输出。
│  │  │     │  ├─ formatter/
│  │  │     │  │  作用：回答内容格式化。
│  │  │     │  ├─ knowledge/
│  │  │     │  │  作用：知识检索服务，负责从向量库召回上下文。
│  │  │     │  ├─ manus/
│  │  │     │  │  作用：Manus 智能体应用层封装。
│  │  │     │  │  ├─ factory/
│  │  │     │  │  │  作用：创建 Manus 相关对象。
│  │  │     │  │  └─ service/
│  │  │     │  │     作用：对外提供 Manus 聊天服务。
│  │  │     │  ├─ prompt/
│  │  │     │  │  作用：集中管理系统提示词。
│  │  │     │  └─ router/
│  │  │     │     作用：意图路由。
│  │  │     │     ├─ HybridIntentRouter.java
│  │  │     │     │  作用：规则优先、LLM 兜底的混合意图识别器。
│  │  │     │     ├─ IntentType.java
│  │  │     │     │  作用：定义 CHITCHAT、TOOL、KNOWLEDGE 等意图枚举。
│  │  │     │     ├─ classifier/
│  │  │     │     │  作用：分类器实现。
│  │  │     │     │  ├─ RuleBasedIntentClassifier.java
│  │  │     │     │  │  作用：基于规则的意图识别。
│  │  │     │     │  └─ LlmIntentClassifier.java
│  │  │     │     │     作用：基于大模型的意图识别。
│  │  │     │     ├─ model/
│  │  │     │     │  作用：意图识别结果模型。
│  │  │     │     └─ service/
│  │  │     │        作用：对话历史辅助服务。
│  │  │     │
│  │  │     ├─ chatmemory/
│  │  │     │  作用：会话记忆存储。
│  │  │     │  ├─ FileBasedChatMemory.java
│  │  │     │  │  作用：文件型聊天记忆。
│  │  │     │  ├─ jdbc/
│  │  │     │  │  作用：基于数据库的聊天记忆实现。
│  │  │     │  │  ├─ model/
│  │  │     │  │  │  作用：聊天消息、会话、摘要等实体。
│  │  │     │  │  └─ repository/
│  │  │     │  │     作用：JDBC 数据访问层。
│  │  │     │  └─ service/
│  │  │     │     作用：聊天会话服务。
│  │  │     │
│  │  │     ├─ config/
│  │  │     │  作用：Spring 配置类。
│  │  │     │  ├─ ChatMemoryConfig.java
│  │  │     │  │  作用：配置聊天记忆 Bean。
│  │  │     │  ├─ CorsConfig.java
│  │  │     │  │  作用：配置跨域。
│  │  │     │  └─ KnowledgeDocumentProperties.java
│  │  │     │     作用：配置知识文档加载参数。
│  │  │     │
│  │  │     ├─ constant/
│  │  │     │  作用：常量定义。
│  │  │     │
│  │  │     ├─ controller/
│  │  │     │  作用：HTTP 接口层。
│  │  │     │  ├─ AiController.java
│  │  │     │  │  作用：情感助手 / 流式聊天接口入口。
│  │  │     │  ├─ ChatSessionController.java
│  │  │     │  │  作用：聊天会话管理接口。
│  │  │     │  ├─ FileController.java
│  │  │     │  │  作用：文件上传、下载相关接口。
│  │  │     │  ├─ HealthController.java
│  │  │     │  │  作用：健康检查接口。
│  │  │     │  ├─ KnowledgeBaseController.java
│  │  │     │  │  作用：知识库上传、解析、入库相关接口。
│  │  │     │  └─ model/
│  │  │     │     作用：接口层请求/响应对象。
│  │  │     │
│  │  │     ├─ demo/
│  │  │     │  作用：示例代码，不是主业务链路。
│  │  │     │  ├─ invoke/
│  │  │     │  │  作用：不同 AI 调用方式的演示。
│  │  │     │  └─ rag/
│  │  │     │     作用：RAG 示例。
│  │  │     │
│  │  │     ├─ rag/
│  │  │     │  作用：检索增强生成模块，是知识库能力核心。
│  │  │     │  ├─ LoveAppDocumentLoader.java
│  │  │     │  │  作用：加载知识文档。
│  │  │     │  ├─ LoveAppVectorStoreConfig.java
│  │  │     │  │  作用：向量库配置。
│  │  │     │  ├─ PgVectorVectorStoreConfig.java
│  │  │     │  │  作用：PGVector 相关配置。
│  │  │     │  ├─ QueryRewriter.java
│  │  │     │  │  作用：查询改写。
│  │  │     │  ├─ MyKeywordEnricher.java
│  │  │     │  │  作用：关键词增强。
│  │  │     │  ├─ parser/
│  │  │     │  │  作用：解析不同格式文档。
│  │  │     │  │  ├─ MarkdownKnowledgeDocumentParser.java
│  │  │     │  │  ├─ TikaDocxDocumentParser.java
│  │  │     │  │  └─ TikaPdfDocumentParser.java
│  │  │     │  ├─ splitter/
│  │  │     │  │  作用：文档切块。
│  │  │     │  ├─ service/
│  │  │     │  │  作用：知识库入库、注册等服务。
│  │  │     │  └─ model/
│  │  │     │     作用：知识库解析/上传/状态模型。
│  │  │     │
│  │  │     └─ tools/
│  │  │        作用：工具调用体系。
│  │  │        ├─ WebSearchTool.java
│  │  │        │  作用：网页搜索工具。
│  │  │        ├─ WebScrapingTool.java
│  │  │        │  作用：网页抓取工具。
│  │  │        ├─ FileOperationTool.java
│  │  │        │  作用：文件读写操作。
│  │  │        ├─ ResourceDownloadTool.java
│  │  │        │  作用：资源下载。
│  │  │        ├─ TerminalOperationTool.java
│  │  │        │  作用：终端命令执行。
│  │  │        ├─ PDFGenerationTool.java
│  │  │        │  作用：生成 PDF。
│  │  │        ├─ KnowledgeSearchTool.java
│  │  │        │  作用：知识检索工具。
│  │  │        ├─ TerminateTool.java
│  │  │        │  作用：终止任务。
│  │  │        ├─ ToolRegistration.java
│  │  │        │  作用：工具注册入口。
│  │  │        ├─ pdf/
│  │  │        │  作用：PDF 模板渲染相关类。
│  │  │        └─ routing/
│  │  │           作用：工具路由中心。
│  │  │           ├─ UnifiedToolRegistry.java
│  │  │           │  作用：统一管理本地工具和 MCP 工具，并按能力筛选候选工具。
│  │  │           ├─ ToolCapability.java
│  │  │           │  作用：定义工具能力分类。
│  │  │           ├─ ToolRoutingDecision.java
│  │  │           │  作用：封装本次工具路由结果。
│  │  │           └─ RegisteredTool.java
│  │  │              作用：统一描述一个已注册工具。
│  │  │
│  │  └─ resources/
│  │     ├─ application.yml
│  │     │  作用：主配置文件，包含端口、数据库、AI 模型、MCP、知识文档等配置。
│  │     ├─ application-prod.yml
│  │     │  作用：生产环境配置。
│  │     ├─ mcp-servers.json
│  │     │  作用：配置 MCP 服务端连接信息。
│  │     └─ document/
│  │        作用：知识库原始文档目录。
│  │        ├─ 情感常见问题和回答 - 亲密关系修复篇.md
│  │        ├─ 情感常见问题和回答 - 人际关系篇.md
│  │        ├─ 情感常见问题和回答 - 情绪自愈篇.md
│  │        ├─ 情感常见问题和回答（全新版）.pdf
│  │        └─ 情感常见问题和回答（完整版）.docx
│  │           作用：情感知识库数据源。
│  │
│  └─ test/
│     └─ java/com/yupi/yuaiagent/
│        作用：后端测试代码。
│        ├─ advisor/
│        │  作用：advisor 测试。
│        ├─ agent/
│        │  作用：Agent 测试。
│        ├─ app/
│        │  作用：LoveApp 等应用层测试。
│        ├─ rag/
│        │  作用：RAG 相关测试。
│        ├─ tools/
│        │  作用：工具类测试。
│        └─ demo/
│           作用：示例模块测试。
│
├─ yu-ai-agent-frontend/
│  作用：前端项目，Vue 3 + Vite。
│  ├─ src/
│  │  ├─ api/
│  │  │  作用：封装前端请求接口。
│  │  ├─ assets/
│  │  │  作用：静态资源。
│  │  ├─ components/
│  │  │  作用：通用组件。
│  │  │  ├─ ChatRoom.vue
│  │  │  │  作用：聊天窗口核心组件。
│  │  │  ├─ PdfPreview.vue
│  │  │  │  作用：PDF 预览。
│  │  │  ├─ AppFooter.vue
│  │  │  │  作用：页脚组件。
│  │  │  └─ AiAvatarFallback.vue
│  │  │     作用：AI 头像兜底显示。
│  │  ├─ router/
│  │  │  作用：前端路由管理。
│  │  ├─ views/
│  │  │  作用：页面级组件。
│  │  │  ├─ Home.vue
│  │  │  │  作用：首页。
│  │  │  ├─ LoveMaster.vue
│  │  │  │  作用：情感助手页面。
│  │  │  └─ SuperAgent.vue
│  │  │     作用：超级智能体页面。
│  │  ├─ App.vue
│  │  │  作用：应用根组件。
│  │  ├─ main.js
│  │  │  作用：前端启动入口。
│  │  └─ style.css
│  │     作用：全局样式。
│  ├─ public/
│  │  作用：公开静态资源。
│  ├─ package.json
│  │  作用：前端依赖和脚本配置。
│  ├─ vite.config.js
│  │  作用：Vite 构建配置。
│  ├─ nginx.conf
│  │  作用：前端部署时的 Nginx 配置。
│  ├─ Dockerfile
│  │  作用：前端镜像构建。
│  └─ node_modules/
│     作用：前端依赖目录，一般不看业务逻辑。
│
├─ yu-image-search-mcp-server/
│  作用：独立的 MCP 图片搜索服务。
│  ├─ src/
│  │  ├─ main/
│  │  │  ├─ java/com/yupi/yuimagesearchmcpserver/
│  │  │  │  ├─ YuImageSearchMcpServerApplication.java
│  │  │  │  │  作用：MCP 服务启动入口。
│  │  │  │  └─ tools/
│  │  │  │     └─ ImageSearchTool.java
│  │  │  │        作用：图片搜索 MCP 工具实现。
│  │  │  └─ resources/
│  │  │     ├─ application.yml
│  │  │     ├─ application-stdio.yml
│  │  │     └─ application-sse.yml
│  │  │        作用：MCP 服务不同启动模式配置。
│  │  └─ test/
│  │     作用：MCP 服务测试代码。
│  ├─ pom.xml
│  │  作用：MCP 服务 Maven 配置。
│  ├─ mvnw / mvnw.cmd
│  │  作用：Maven Wrapper。
│  ├─ .mvn/
│  │  作用：Maven Wrapper 配套目录。
│  └─ target/
│     作用：构建产物目录。
│
├─ .mvn/
│  作用：主项目 Maven Wrapper 配置。
│
├─ .idea/
│  作用：IntelliJ IDEA 工程配置。
│
├─ .vscode/
│  作用：VS Code 工程配置。
│
├─ target/
│  作用：主后端构建输出目录。
│
├─ tmp/
│  作用：临时目录，当前也被知识库上传配置用作上传目录。
│
├─ .m2home/
│  作用：本地 Maven 相关目录，通常是开发环境辅助目录。
│
├─ Dockerfile
│  作用：主后端 Docker 镜像构建文件。
│
├─ pom.xml
│  作用：主后端 Maven 配置，定义 Spring Boot、Spring AI、数据库、向量库、工具等依赖。
│
├─ README.md
│  作用：项目说明文档。
│
├─ mvnw / mvnw.cmd
│  作用：主项目 Maven Wrapper。
│
└─ ${maven.multiModuleProjectDirectory}/
   作用：这个目录看起来不太正常，像是某次构建或配置错误生成的占位目录，建议后续确认是否为误生成。

