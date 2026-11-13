<#-- 全局布局：Tailwind(本地构建产物,管布局) + app.css(少量自定义) + app.js，全部同源加载。
     CSP 零豁免：无 unsafe-inline / unsafe-eval / CDN，故不得写内联 onclick/onload/<style>，
     交互一律走 data-* + app.js 的 addEventListener。 -->
<#macro layout title="Code Review Guru" active="">
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${title} · Code Review Guru</title>
    <#-- Tailwind 构建产物（见 tailwind.config.js）：负责布局/间距/圆角/阴影/配色 -->
    <link rel="stylesheet" href="/css/tailwind.css">
    <link rel="stylesheet" href="/css/app.css">
    <#-- 移动端菜单 / 表单提交态由 app.js 以 data-* + addEventListener 实现 -->
    <script defer src="/js/app.js"></script>
</head>
<body class="bg-slate-50 text-slate-800 antialiased flex flex-col min-h-screen">

<header class="bg-white shadow-sm sticky top-0 z-10">
    <div class="max-w-5xl mx-auto px-4 h-16 flex items-center justify-between gap-3">
        <a href="/" class="text-xl font-bold text-indigo-600 shrink-0">Code Review Guru</a>
        <nav class="hidden md:flex items-center gap-5 text-sm font-medium">
            <a href="/" class="hover:text-indigo-600 <#if active=='home'>text-indigo-600 font-semibold<#else>text-slate-600</#if>">首页</a>
            <a href="/reviews" class="hover:text-indigo-600 <#if active=='reviews'>text-indigo-600 font-semibold<#else>text-slate-600</#if>">历史评审</a>
        </nav>
        <button type="button" class="md:hidden text-slate-600" data-menu-toggle
                aria-controls="mobile-menu" aria-expanded="false" aria-label="菜单">☰</button>
    </div>
    <div id="mobile-menu" data-menu-panel
         class="hidden md:hidden border-t border-slate-200 px-4 py-2 flex flex-col gap-2 text-sm">
        <a href="/" class="py-1 text-slate-600">首页</a>
        <a href="/reviews" class="py-1 text-slate-600">历史评审</a>
    </div>
</header>

<main class="flex-1">
    <#nested>
</main>

<footer class="border-t border-slate-200 bg-white">
    <div class="max-w-5xl mx-auto px-4 py-6 text-sm text-slate-400 flex flex-col md:flex-row md:items-center md:justify-between gap-2">
        <div>© ${.now?string("yyyy")} Code Review Guru · 基于 spring-ai 的代码评审平台</div>
        <div>数据存储在内嵌 H2 数据库</div>
    </div>
</footer>

</body>
</html>
</#macro>
