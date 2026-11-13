# code-review-guru 项目要点（长期）

- 技术栈：Spring Boot 3.5.16 + spring-ai 1.1.8 + MyBatis 3.0.4 + 内嵌 H2（默认文件型 `./reviewdb`，`MODE=MySQL`；测试内存）+ Freemarker + Tailwind（构建产物提交进仓库）+ 原生 `app.js`。包名 `com.codeguru.review`，主类 `CodeReviewGuruApplication`（`@MapperScan("com.codeguru.review.mapper")`）。
- 前端三条硬约束（对齐 vela-web，改前端前必读）：CSP 零豁免（`script-src 'self'` / `style-src 'self'`，无 CDN、无 unsafe-inline/eval）；禁内联 `onclick/onload/<style>`；交互一律 `data-*` + `app.js` 的 `addEventListener`。见 `config/SecurityHeadersFilter`。
- spring-ai 启用方式：`application.yml` 里 `spring.ai.openai.api-key=${OPENAI_API_KEY:sk-local-placeholder}`（占位 key 只为过 spring-ai 启动期校验）+ `spring.autoconfigure.exclude` 排除 5 个非 chat 的 OpenAI 模型自动配置；`CodeReviewService` 仅在检测到真实（非占位）key 时走 `AiReviewer`，否则/异常回退 `RuleBasedReviewer`（离线可用）。`app.review.ai-enabled` 可强制开关。
- 改模板 class 后重新生成样式：`npx tailwindcss@3.4.17 -i tools/tailwind.src.css -o src/main/resources/static/css/tailwind.css --minify`（content 扫 templates + static/js/app.js）。
- 本机构建/测试（Bash 不传 env、缺 Git usr/bin，用 plexus-classworlds Launcher）：
  ```bash
  export PATH="/d/Program Files/Git/usr/bin:$PATH"
  "D:/Program Files/Java/jdk-25.0.3/bin/java.exe" --enable-native-access=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED \
    -classpath "D:/MyDevelop/JDevelop/maven/apache-maven-3.9.15/boot/plexus-classworlds-2.9.0.jar" \
    "-Dclassworlds.conf=D:/MyDevelop/JDevelop/maven/apache-maven-3.9.15/bin/m2.conf" \
    "-Dmaven.home=D:/MyDevelop/JDevelop/maven/apache-maven-3.9.15" \
    "-Dlibrary.jansi.path=D:/MyDevelop/JDevelop/maven/apache-maven-3.9.15/lib/jansi-native" \
    "-Dmaven.multiModuleProjectDirectory=D:/MyDevelop/JDevelop/workspace/code-review-guru" \
    org.codehaus.plexus.classworlds.launcher.Launcher -B clean test
  ```
- 大坑（务必记住）：① `src/test/resources/application.yml` 会整份遮蔽主配置，且 `target/test-classes` 里的陈旧副本在 `mvn test`（非 clean）下持续生效——测试要覆盖配置一律用 `@DynamicPropertySource`，别放 application.yml；改主配置后必要时先 `mvn clean`。② spring-ai 缺 key 会让 OpenAI 模型 Bean 启动期抛错（见上）。
- 数据表：`review_request`（title/language/source_code/source_size/reviewer_type/model/summary/score/status/created_at）、`review_finding`（request_id/severity/category/line/title/detail/suggestion）。severity：ERROR/WARNING/INFO；reviewer_type：AI/RULE。建表 `db/schema.sql`（MySQL 方言，H2 MODE=MySQL 同跑）。
