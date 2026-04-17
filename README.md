# AI 情感助手与超级智能体项目
**作者**：zmy
**项目定位**：AI 应用实战项目（情感助手 + 超级智能体）
**技术方向**：Spring AI / RAG / Tool Calling / MCP / Agent

--2026-4.16---

## ✨ 核心功能特性

### 1. AI 情感助手
- 面向情感陪伴、关系沟通、情绪疏导等场景，提供更偏咨询式、陪伴式的对话能力。
- 支持同步对话与 SSE 流式对话两种交互模式，兼顾标准接口调用和实时聊天体验。
- 支持多轮上下文记忆，能够结合历史会话内容保持回复连贯性。
- 支持意图识别与对话分流，可根据用户问题自动进入普通闲聊、知识问答、工具调用或澄清提问链路。
- 支持知识库增强问答，可结合情感领域知识文档生成更有依据的回答。
- 支持结构化能力扩展，可进一步生成情感分析、沟通建议、关系修复建议等结果。

### 2. 超级智能体
- 面向复杂任务执行场景，支持比普通聊天更强的任务拆解、工具调用和多步执行能力。
- 支持根据用户请求自动选择执行链路，简单对话复用情感助手能力，复杂任务切换到智能体自治执行模式。
- 支持多步推理与任务推进，可结合当前上下文、工具结果和历史消息持续完成任务。
- 支持工具协同调用，能够接入网页搜索、网页抓取、文件操作、终端执行、PDF 生成、知识检索等多类工具。
- 支持任务完成终止机制，能够在获得足够信息后结束执行，避免无效调用。
- 前端支持展示智能体推理过程、工具执行状态和最终答案，增强任务执行过程的可解释性。

### 3. 会话管理与记忆机制
- 支持会话创建、切换、删除，形成完整的会话管理闭环。
- 支持会话 ID 持久化，页面刷新后可自动恢复最近会话状态。
- 支持会话级消息隔离，每个会话独立保存消息历史，避免上下文串扰。
- 支持基于 MySQL 的聊天消息持久化存储，保障历史对话可追溯。
- 支持“摘要 + 最近消息”的混合记忆机制，在控制上下文长度的同时尽量保留关键信息。

### 4. 知识库问答能力（RAG）
- 支持 `md / pdf / docx` 多格式知识文档上传与解析。
- 支持完整知识入库链路：文档解析 → 文本切分 → 文本清洗 → 向量化 → 入库。
- 支持公共知识与会话私有知识并存，兼顾通用知识复用和当前会话场景适配。
- 支持会话级知识隔离，上传文档自动绑定当前会话，检索时按会话范围过滤，避免跨会话知识串读。
- 支持混合召回与回退检索策略，在首轮召回不足时自动扩大召回范围，提高知识命中率和回答稳定性。
- 支持上传后返回入库分片数，便于快速确认文档是否进入可检索状态。

### 5. 工具与 MCP 扩展能力
- 支持原生 Tool Calling，可灵活扩展业务工具能力。
- 支持统一工具注册与能力路由，可根据用户请求自动筛选更匹配的工具集合。
- 支持 MCP 协议客户端接入，兼容外部工具生态，便于后续接入地图、图片搜索等扩展能力。
- 为超级智能体提供可插拔的任务执行能力底座，方便后续持续扩展复杂业务场景。

### 6. 前端交互体验
- 采用类主流 AI 产品的左右布局，左侧管理会话，右侧承载聊天交互。
- 支持 SSE 流式输出实时渲染，提升对话自然度与响应感知。
- 支持知识文档上传与结果即时反馈，操作状态清晰可见。
- 支持普通聊天展示与智能体推理过程展示两种交互形态，兼顾易用性与可解释性。


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
