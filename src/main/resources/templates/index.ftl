<#import "/layout/base.ftl" as b>
<@b.layout title="代码评审" active="home">

<div class="max-w-3xl mx-auto px-4 py-10">

    <h1 class="text-3xl font-bold text-slate-900 mb-2">AI 代码评审</h1>
    <p class="text-slate-500 mb-6">粘贴你的代码，平台会用 spring-ai 调用大模型评审，并结构化列出问题、给出修改建议。
       未配置 AI 密钥时自动降级为本地规则评审（离线可用）。</p>

    <#if error??>
        <div class="mb-6 border border-red-200 bg-red-50 text-red-700 text-sm rounded-lg px-4 py-3" role="alert">
            ${error}
        </div>
    </#if>

    <form method="post" action="/reviews" class="bg-white rounded-xl shadow-sm border border-slate-200 p-6 mb-10" data-loading-form>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
            <div>
                <label class="block text-sm font-medium text-slate-700 mb-1">语言</label>
                <select name="language"
                        class="w-full border border-slate-300 rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400">
                    <#list languages as lang>
                        <option value="${lang}" <#if lang == (language!'java')>selected</#if>>${lang}</option>
                    </#list>
                </select>
            </div>
            <div class="md:col-span-2">
                <label class="block text-sm font-medium text-slate-700 mb-1">标题（选填）</label>
                <input name="title" value="${titleValue!''}" maxlength="200" placeholder="例如：用户登录校验"
                       class="w-full border border-slate-300 rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400">
            </div>
        </div>
        <div class="mb-4">
            <label class="block text-sm font-medium text-slate-700 mb-1">代码 <span class="text-red-500">*</span></label>
            <textarea name="code" rows="14" maxlength="200000" required placeholder="在此粘贴需要评审的代码片段..."
                      class="w-full border border-slate-300 rounded px-3 py-2 text-sm font-mono focus:outline-none focus:ring-2 focus:ring-indigo-400">${codeValue!''}</textarea>
            <p class="text-xs text-slate-400 mt-1">单次上限 200000 字符。</p>
        </div>
        <button type="submit"
                class="bg-indigo-600 text-white px-5 py-2 rounded-lg text-sm font-medium hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-400"
                data-submit-label="提交评审">提交评审</button>
    </form>

    <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-semibold text-slate-800">最近评审</h2>
        <a href="/reviews" class="text-sm text-indigo-600 hover:underline">查看全部 →</a>
    </div>
    <#if recent?has_content>
        <ul class="space-y-3">
            <#list recent as r>
                <li>
                    <a href="/reviews/${r.id}"
                       class="block bg-white rounded-xl shadow-sm border border-slate-200 p-4 hover:border-indigo-400">
                        <div class="flex items-center justify-between gap-2">
                            <span class="font-medium text-slate-800 truncate">${r.title}</span>
                            <span class="text-xs text-slate-400 shrink-0">${r.createdAtText}</span>
                        </div>
                        <div class="mt-1 flex items-center gap-2 text-xs text-slate-500">
                            <span class="px-2 py-0.5 rounded bg-slate-100">${r.language}</span>
                            <span class="px-2 py-0.5 rounded <#if r.reviewerType=='AI'>bg-indigo-50 text-indigo-700<#else>bg-slate-100 text-slate-600</#if>">${r.reviewerType}</span>
                            <#if r.scoreText??><span class="px-2 py-0.5 rounded bg-slate-100">评分 ${r.scoreText}</span></#if>
                        </div>
                    </a>
                </li>
            </#list>
        </ul>
    <#else>
        <p class="text-sm text-slate-400">还没有评审记录，提交第一段代码试试。</p>
    </#if>

</div>
</@b.layout>
