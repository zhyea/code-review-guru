<#import "/layout/base.ftl" as b>
<@b.layout title="评审详情" active="reviews">

<div class="max-w-4xl mx-auto px-4 py-10">

    <a href="/reviews" class="text-sm text-indigo-600 hover:underline">← 返回历史</a>

    <div class="bg-white rounded-xl shadow-sm border border-slate-200 p-6 mt-4 mb-6">
        <div class="flex items-start justify-between gap-4 flex-wrap">
            <div>
                <h1 class="text-2xl font-bold text-slate-900">${req.title}</h1>
                <div class="mt-2 flex items-center gap-2 text-xs text-slate-500">
                    <span class="px-2 py-0.5 rounded bg-slate-100">${req.language}</span>
                    <span class="px-2 py-0.5 rounded <#if req.reviewerType=='AI'>bg-indigo-50 text-indigo-700<#else>bg-slate-100 text-slate-600</#if>">${req.reviewerType}<#if req.model?? && req.model != ''> · ${req.model}</#if></span>
                    <span class="text-slate-400">${createdAtText}</span>
                </div>
            </div>
            <#if req.score??>
                <div class="text-center">
                    <div class="text-3xl font-bold <#if (req.score!100) gte 80>text-emerald-600<#elseif (req.score!100) gte 60>text-amber-600<#else>text-red-600</#if>">${req.score}</div>
                    <div class="text-xs text-slate-400">综合评分</div>
                </div>
            </#if>
        </div>
        <#if req.summary?? && req.summary != ''>
            <p class="mt-4 text-sm text-slate-600 leading-relaxed whitespace-pre-wrap break-words">${req.summary}</p>
        </#if>
    </div>

    <#-- 被评审源码：原生 <details> 折叠，无需 JS（CSP script-src 'self' 下零依赖）。
         ${req.sourceCode} 由 FreeMarker HTMLOutputFormat 自动转义，XSS 安全，不可用 ?no_esc。 -->
    <details class="bg-white rounded-xl shadow-sm border border-slate-200 p-4 mb-6">
        <summary class="cursor-pointer select-none text-sm font-medium text-slate-700">查看被评审源码（${req.sourceSize} 字符）</summary>
        <pre class="mt-3 p-3 bg-slate-50 rounded text-xs font-mono text-slate-700 whitespace-pre-wrap break-words overflow-x-auto">${req.sourceCode!''}</pre>
    </details>

    <h2 class="text-lg font-semibold text-slate-800 mb-4">评审发现（${findings?size}）</h2>
    <#if findings?has_content>
        <div class="space-y-3">
            <#list findings as f>
                <div class="bg-white rounded-xl shadow-sm border border-slate-200 p-4">
                    <div class="flex items-center gap-2 mb-2 flex-wrap">
                        <#if f.severity == 'ERROR'>
                            <span class="px-2 py-0.5 rounded bg-red-50 text-red-700 border border-red-200 text-xs font-medium">ERROR</span>
                        <#elseif f.severity == 'WARNING'>
                            <span class="px-2 py-0.5 rounded bg-amber-50 text-amber-700 border border-amber-200 text-xs font-medium">WARNING</span>
                        <#else>
                            <span class="px-2 py-0.5 rounded bg-slate-100 text-slate-600 text-xs font-medium">INFO</span>
                        </#if>
                        <#if f.category?? && f.category != ''><span class="text-xs text-slate-400">${f.category}</span></#if>
                        <#if f.line??><span class="text-xs text-slate-400">行 ${f.line}</span></#if>
                        <span class="font-medium text-slate-800 text-sm">${f.title}</span>
                    </div>
                    <#if f.detail?? && f.detail != ''>
                        <p class="text-sm text-slate-600 whitespace-pre-wrap break-words">${f.detail}</p>
                    </#if>
                    <#if f.suggestion?? && f.suggestion != ''>
                        <div class="mt-2 text-sm">
                            <span class="text-emerald-700 font-medium">建议：</span>
                            <span class="text-slate-600 whitespace-pre-wrap break-words">${f.suggestion}</span>
                        </div>
                    </#if>
                </div>
            </#list>
        </div>
    <#else>
        <div class="bg-white rounded-xl shadow-sm border border-slate-200 p-8 text-center">
            <p class="text-sm text-slate-400">未发现问题，代码质量良好。</p>
        </div>
    </#if>

</div>
</@b.layout>
