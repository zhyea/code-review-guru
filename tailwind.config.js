/** Tailwind CLI 构建配置（替代 Play CDN，产物提交进仓库）。
 *
 *  用法（在项目根目录）：
 *    npx tailwindcss@3.4.17 -i tools/tailwind.src.css -o src/main/resources/static/css/tailwind.css --minify
 *
 *  content 指向 FreeMarker 模板与原生 JS 脚本：Tailwind 扫描器按文本提取候选 class。
 *  模板里所有 class 都是字面量（无动态拼接），JIT 能全覆盖；app.js 用 classList
 *  切换的 'hidden' 也在扫描范围内。
 *
 *  生成产物提交进仓库（static/css/tailwind.css），构建无需在 Maven/CI 跑 Node；
 *  改动模板 class 后需手动重跑上面命令并提交新产物。
 */
module.exports = {
  content: [
    "./src/main/resources/templates/**/*.ftl",
    "./src/main/resources/static/js/app.js",
  ],
  theme: {
    extend: {},
  },
  corePlugins: {
    preflight: true,
  },
};
