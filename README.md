

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

