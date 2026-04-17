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
```text
yu-ai-agent-master/
├─ src/
│  ├─ main/
│  │  ├─ java/
│  │  │  └─ com/yupi/yuaiagent/
│  │  │     ├─ YuAiAgentApplication.java
│  │  │     │  作用：Spring Boot 启动入口。
│  │  │     │
│  │  │     ├─ advisor/
│  │  │     │  作用：挂在 Spring AI ChatClient 上的对话增强器。
│  │  │     │  ├─ MyLoggerAdvisor.java
│  │  │     │  │  作用：记录模型请求/响应过程日志，方便排查调用链。
│  │  │     │  └─ ReReadingAdvisor.java
│  │  │     │     作用：对模型对话过程做额外控制的 advisor。
│  │  │     │
│  │  │     ├─ agent/
│  │  │     │  作用：通用 Agent 抽象与自治执行能力。
│  │  │     │  ├─ BaseAgent.java
│  │  │     │  │  作用：Agent 基类，定义多步执行的基础能力。
│  │  │     │  ├─ ReActAgent.java
│  │  │     │  │  作用：基于 ReAct 思路的 Agent 实现。
│  │  │     │  ├─ ToolCallAgent.java
│  │  │     │  │  作用：以工具调用为核心的 Agent 基础实现。
│  │  │     │  ├─ YuManus.java
│  │  │     │  │  作用：项目里的自治任务型智能体，支持多步推理、工具调用和记忆回写。
│  │  │     │  └─ model/
│  │  │     │     作用：Agent 执行过程中的状态模型。
│  │  │     │
│  │  │     ├─ app/
│  │  │     │  作用：应用编排层，负责把意图识别、知识检索、工具调用、提示词和输出格式串起来。
│  │  │     │  ├─ LoveApp.java
│  │  │     │  │  作用：情感助手主编排入口；先做意图路由，再分流到普通对话、知识问答、工具调用或澄清分支。
│  │  │     │  ├─ formatter/
│  │  │     │  │  作用：回答后处理与格式清洗。
│  │  │     │  ├─ knowledge/
│  │  │     │  │  作用：应用层知识召回门面，不负责入库，负责按会话范围从向量库检索、过滤、去重并拼接上下文。
│  │  │     │  ├─ manus/
│  │  │     │  │  作用：超级智能体应用层封装，负责把普通聊天链路和 YuManus 自治链路接起来。
│  │  │     │  │  ├─ factory/
│  │  │     │  │  │  作用：创建带会话上下文的 YuManus 实例。
│  │  │     │  │  └─ service/
│  │  │     │  │     作用：对外提供 Manus 聊天服务；简单意图走 LoveApp，复杂任务走 YuManus。
│  │  │     │  ├─ prompt/
│  │  │     │  │  作用：集中管理情感助手、知识问答、工具调用、澄清等系统提示词。
│  │  │     │  └─ router/
│  │  │     │     作用：意图识别与路由决策。
│  │  │     │     ├─ HybridIntentRouter.java
│  │  │     │     │  作用：规则优先、LLM 兜底的混合路由器。
│  │  │     │     ├─ IntentType.java
│  │  │     │     │  作用：定义 CHITCHAT、TOOL、KNOWLEDGE、CLARIFICATION 等意图类型。
│  │  │     │     ├─ classifier/
│  │  │     │     │  作用：意图分类器实现。
│  │  │     │     │  ├─ RuleBasedIntentClassifier.java
│  │  │     │     │  │  作用：基于关键词/规则快速判断意图。
│  │  │     │     │  └─ LlmIntentClassifier.java
│  │  │     │     │     作用：在规则无法命中时，调用大模型补充意图判断。
│  │  │     │     ├─ model/
│  │  │     │     │  作用：意图识别结果模型。
│  │  │     │     └─ service/
│  │  │     │        作用：对话历史辅助服务，供意图识别阶段参考上下文。
│  │  │     │
│  │  │     ├─ chatmemory/
│  │  │     │  作用：会话记忆与历史消息持久化。
│  │  │     │  ├─ FileBasedChatMemory.java
│  │  │     │  │  作用：文件型聊天记忆实现。
│  │  │     │  ├─ jdbc/
│  │  │     │  │  作用：基于数据库的聊天记忆实现，核心是“摘要 + 最近消息”的混合记忆。
│  │  │     │  │  ├─ HybridJdbcChatMemory.java
│  │  │     │  │  │  作用：Spring AI ChatMemory 的 JDBC 实现，负责消息存储、摘要压缩、历史恢复。
│  │  │     │  │  ├─ ChatMemorySchemaInitializer.java
│  │  │     │  │  │  作用：初始化聊天记忆相关表结构。
│  │  │     │  │  ├─ MemorySummaryService.java
│  │  │     │  │  │  作用：对历史消息做摘要压缩。
│  │  │     │  │  ├─ model/
│  │  │     │  │  │  作用：聊天消息、会话、摘要等数据库实体。
│  │  │     │  │  └─ repository/
│  │  │     │  │     作用：聊天记忆 JDBC 数据访问层。
│  │  │     │  └─ service/
│  │  │     │     作用：聊天会话管理服务，负责创建、查询、删除会话及读取消息历史。
│  │  │     │
│  │  │     ├─ config/
│  │  │     │  作用：Spring 配置类。
│  │  │     │  ├─ ChatMemoryConfig.java
│  │  │     │  │  作用：装配聊天记忆相关 Bean。
│  │  │     │  ├─ CorsConfig.java
│  │  │     │  │  作用：配置跨域。
│  │  │     │  └─ KnowledgeDocumentProperties.java
│  │  │     │     作用：配置知识文档加载、切块、上传目录等参数。
│  │  │     │
│  │  │     ├─ constant/
│  │  │     │  作用：通用常量定义。
│  │  │     │
│  │  │     ├─ controller/
│  │  │     │  作用：HTTP 接口层。
│  │  │     │  ├─ AiController.java
│  │  │     │  │  作用：情感助手与超级智能体的聊天入口，提供同步和多种 SSE 输出方式。
│  │  │     │  ├─ ChatSessionController.java
│  │  │     │  │  作用：聊天会话管理接口。
│  │  │     │  ├─ FileController.java
│  │  │     │  │  作用：文件下载/访问接口，主要服务 PDF 等产物访问。
│  │  │     │  ├─ HealthController.java
│  │  │     │  │  作用：健康检查接口。
│  │  │     │  ├─ KnowledgeBaseController.java
│  │  │     │  │  作用：知识库状态查询、重载、上传入库接口。
│  │  │     │  └─ model/
│  │  │     │     作用：接口层请求/响应对象。
│  │  │     │
│  │  │     ├─ demo/
│  │  │     │  作用：示例代码，不属于主业务链路。
│  │  │     │  ├─ invoke/
│  │  │     │  │  作用：不同 AI 调用方式的演示。
│  │  │     │  └─ rag/
│  │  │     │     作用：RAG 能力演示。
│  │  │     │
│  │  │     ├─ rag/
│  │  │     │  作用：知识库/RAG 基础设施层，负责文档加载、解析、切分、向量入库、检索增强相关配置。
│  │  │     │  ├─ LoveAppDocumentLoader.java
│  │  │     │  │  作用：统一加载并解析知识文档，再交给切分器切块。
│  │  │     │  ├─ LoveAppVectorStoreConfig.java
│  │  │     │  │  作用：向量库与嵌入检索相关配置。
│  │  │     │  ├─ PgVectorVectorStoreConfig.java
│  │  │     │  │  作用：PGVector 存储配置。
│  │  │     │  ├─ LoveAppRagCustomAdvisorFactory.java
│  │  │     │  │  作用：构造项目自定义的 RAG advisor。
│  │  │     │  ├─ LoveAppRagCloudAdvisorConfig.java
│  │  │     │  │  作用：RAG advisor 云端配置相关支持。
│  │  │     │  ├─ LoveAppContextualQueryAugmenterFactory.java
│  │  │     │  │  作用：构造上下文化查询增强相关组件。
│  │  │     │  ├─ QueryRewriter.java
│  │  │     │  │  作用：查询改写，提升召回效果。
│  │  │     │  ├─ MyKeywordEnricher.java
│  │  │     │  │  作用：补充关键词，增强检索召回。
│  │  │     │  ├─ parser/
│  │  │     │  │  作用：不同文档格式解析器工厂与实现。
│  │  │     │  ├─ splitter/
│  │  │     │  │  作用：文档切块策略实现。
│  │  │     │  ├─ service/
│  │  │     │  │  作用：知识库入库与已加载文档注册管理。
│  │  │     │  │  ├─ KnowledgeBaseIngestionService.java
│  │  │     │  │  │  作用：负责 classpath 文档和上传文档的解析、归一化、去重、写入向量库。
│  │  │     │  │  └─ KnowledgeDocumentRegistry.java
│  │  │     │  │     作用：记录已入库文档和分片，避免重复导入，并提供状态统计。
│  │  │     │  └─ model/
│  │  │     │     作用：知识库上传、状态、解析、入库结果等模型。
│  │  │     │
│  │  │     └─ tools/
│  │  │        作用：可被模型/Agent 调用的工具集合。
│  │  │        ├─ WebSearchTool.java
│  │  │        │  作用：网页搜索工具。
│  │  │        ├─ WebScrapingTool.java
│  │  │        │  作用：网页抓取工具。
│  │  │        ├─ FileOperationTool.java
│  │  │        │  作用：文件读写工具。
│  │  │        ├─ ResourceDownloadTool.java
│  │  │        │  作用：资源下载工具。
│  │  │        ├─ TerminalOperationTool.java
│  │  │        │  作用：终端命令执行工具。
│  │  │        ├─ PDFGenerationTool.java
│  │  │        │  作用：生成 PDF 文件的工具。
│  │  │        ├─ KnowledgeSearchTool.java
│  │  │        │  作用：面向 Agent 的知识检索工具，内部调用应用层知识召回服务。
│  │  │        ├─ TerminateTool.java
│  │  │        │  作用：结束 Agent 任务的终止工具。
│  │  │        ├─ ToolRegistration.java
│  │  │        │  作用：工具注册入口。
│  │  │        ├─ pdf/
│  │  │        │  作用：PDF 模板数据与渲染支持。
│  │  │        └─ routing/
│  │  │           作用：工具二级路由中心，不直接执行工具，负责把本地工具和 MCP 工具统一编目并按能力筛选候选工具。
│  │  │           ├─ UnifiedToolRegistry.java
│  │  │           │  作用：统一管理本地工具和 MCP 工具，并基于用户消息匹配能力标签。
│  │  │           ├─ ToolCapability.java
│  │  │           │  作用：定义地图、图片搜索、网页搜索、抓取、文件、PDF 等工具能力分类。
│  │  │           ├─ ToolRoutingDecision.java
│  │  │           │  作用：封装本次工具路由结果，包括候选工具、能力和系统提示词。
│  │  │           └─ RegisteredTool.java
│  │  │              作用：统一描述已注册工具的元信息。
│  │  │
│  │  └─ resources/
│  │     ├─ application.yml
│  │     │  作用：主配置文件，包含端口、数据源、模型、MCP、知识文档、上传目录等配置。
│  │     ├─ application-prod.yml
│  │     │  作用：生产环境配置。
│  │     ├─ mcp-servers.json
│  │     │  作用：MCP 客户端连接配置。
│  │     └─ document/
│  │        作用：项目内置知识库原始文档目录，会被批量加载入知识库。
│  │
│  └─ test/
│     └─ java/com/yupi/yuaiagent/
│        作用：后端测试代码。
│        ├─ advisor/
│        │  作用：advisor 测试。
│        ├─ agent/
│        │  作用：Agent / YuManus 测试。
│        ├─ app/
│        │  作用：LoveApp 等应用编排层测试。
│        ├─ app/router/
│        │  作用：意图分类与路由测试。
│        ├─ rag/
│        │  作用：RAG、文档加载、向量库配置测试。
│        ├─ tools/
│        │  作用：工具类测试。
│        └─ demo/
│           作用：示例模块测试。
│
├─ yu-ai-agent-frontend/
│  作用：前端项目，Vue 3 + Vite；提供情感助手页和超级智能体页。
│  ├─ src/
│  │  ├─ api/
│  │  │  作用：封装前端请求与 SSE 连接，负责对接聊天、会话、知识库上传等后端接口。
│  │  ├─ assets/
│  │  │  作用：静态资源。
│  │  ├─ components/
│  │  │  作用：通用组件。
│  │  │  ├─ ChatRoom.vue
│  │  │  │  作用：聊天窗口核心组件；负责消息展示、输入框、文件上传、PDF 预览、推理过程展示。
│  │  │  ├─ PdfPreview.vue
│  │  │  │  作用：PDF 预览组件。
│  │  │  ├─ AppFooter.vue
│  │  │  │  作用：页脚组件。
│  │  │  └─ AiAvatarFallback.vue
│  │  │     作用：不同 AI 类型的头像兜底显示。
│  │  ├─ router/
│  │  │  作用：前端路由管理。
│  │  ├─ views/
│  │  │  作用：页面级组件。
│  │  │  ├─ Home.vue
│  │  │  │  作用：首页/入口页。
│  │  │  ├─ LoveMaster.vue
│  │  │  │  作用：情感助手页面；对接 LoveApp，会话管理和知识文档上传也在这里完成。
│  │  │  └─ SuperAgent.vue
│  │  │     作用：超级智能体页面；对接 Manus SSE，并展示推理过程和工具执行痕迹。
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
│  └─ Dockerfile
│     作用：前端镜像构建文件。
│
├─ yu-image-search-mcp-server/
│  作用：独立的 MCP 图片搜索服务，为主项目提供图片搜索能力。
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
│  │  作用：MCP 子项目 Maven 配置。
│  ├─ mvnw / mvnw.cmd
│  │  作用：Maven Wrapper。
│  └─ .mvn/
│     作用：Maven Wrapper 配套目录。
│
├─ .mvn/
│  作用：主项目 Maven Wrapper 配置。
│
├─ tmp/
│  作用：临时目录；当前知识库上传文件会按会话写入该目录下。
│
├─ target/
│  作用：主后端构建输出目录。
│
├─ Dockerfile
│  作用：主后端 Docker 镜像构建文件。
│
├─ pom.xml
│  作用：主后端 Maven 配置，定义 Spring Boot、Spring AI、MCP、数据库、向量库、工具等依赖。
│
├─ README.md
│  作用：项目说明文档。
│
└─ ${maven.multiModuleProjectDirectory}/
   作用：这个目录确实存在于仓库根目录中，看起来像构建/配置异常生成的占位目录，建议后续确认是否可以清理。

```text
