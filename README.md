# code-review-guru

基于 **Spring Boot 3.5 + spring-ai + 内嵌 H2** 的轻量代码评审平台。

## 技术栈
- 后端：Spring Boot 3.5.16、MyBatis 3.0.4、内嵌 H2（文件型，默认）/ MySQL（生产可选）
- AI：spring-ai 1.1.8（`ChatClient`，OpenAI 兼容端点）
- 前端：**服务端渲染（Freemarker）+ Tailwind CSS（提交进仓库的产物）+ 原生 JS（零依赖）**
  - 严格遵循三条硬约束：资源同源本地加载（不引 CDN）；不写内联 `onclick/onload/<style>`（交互走 `data-*` + `app.js` 的 `addEventListener`）；不引入依赖 `eval/new Function` 的库（CSP `script-src 'self'` 零豁免）

## 运行
```bash
# 1) 编译并启动（默认 H2 文件库，评审记录落本地 ./reviewdb.mv.db）
mvn -B spring-boot:run

# 2) 打开 http://localhost:8080
#    H2 控制台（可选）：http://localhost:8080/h2-console  JDBC URL: jdbc:h2:file:./reviewdb
```

## AI 评审（可选）
默认不配置真实 key 时，平台走**本地规则评审**（离线可用，做基础静态检查）。
要启用大模型评审：
```bash
export OPENAI_API_KEY=sk-xxx          # 配了真实 key 即自动启用 AI（auto 模式）
export OPENAI_BASE_URL=https://api.openai.com/v1   # 自托管 / 兼容端点可改这里
export APP_REVIEW_AI_MODEL=gpt-4o-mini
# export APP_REVIEW_AI_ENABLED=false  # 想强制只用本地规则时可显式关闭
```
> 说明：`spring.ai.openai.api-key` 在 `application.yml` 里有占位默认值，仅为满足 spring-ai
> 各模型 Bean 的启动期校验；服务层只在检测到**非占位**的真实 key 时才真正调用 AI。
> AI 调用失败会自动回退本地规则评审，并在摘要里标注。

## 构建 / 测试
```bash
mvn -B test          # 单元 + 集成测试（@SpringBootTest + 内存 H2，走本地规则评审）
mvn -B package       # 打可执行 jar
```

## 重新生成前端样式（改动模板 class 后）
```bash
npx tailwindcss@3.4.17 -i tools/tailwind.src.css -o src/main/resources/static/css/tailwind.css --minify
```

## 目录
```
src/main/java/com/codeguru/review/   后端（config / domain / mapper / service / web）
src/main/resources/templates/         Freemarker 模板（layout + 三个页面）
src/main/resources/static/            同源静态资源（css/tailwind.css、css/app.css、js/app.js）
src/main/resources/db/schema.sql      建表
```
