<#import "/layout/base.ftl" as b>
<@b.layout title="历史评审" active="reviews">

<div class="max-w-4xl mx-auto px-4 py-10">
    <div class="flex items-center justify-between mb-6">
        <h1 class="text-2xl font-bold text-slate-900">历史评审</h1>
        <a href="/" class="text-sm text-indigo-600 hover:underline">+ 新建评审</a>
    </div>

    <#if reviews?has_content>
        <div class="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
            <table class="w-full text-sm">
                <thead class="bg-slate-50 text-slate-500">
                    <tr>
                        <th class="text-left px-4 py-3 font-medium">标题</th>
                        <th class="text-left px-4 py-3 font-medium">语言</th>
                        <th class="text-left px-4 py-3 font-medium">评审方式</th>
                        <th class="text-left px-4 py-3 font-medium">评分</th>
                        <th class="text-left px-4 py-3 font-medium">时间</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-slate-100">
                    <#list reviews as r>
                        <tr class="hover:bg-slate-50">
                            <td class="px-4 py-3">
                                <a href="/reviews/${r.id}" class="text-indigo-600 hover:underline">${r.title}</a>
                            </td>
                            <td class="px-4 py-3 text-slate-600">${r.language}</td>
                            <td class="px-4 py-3">
                                <span class="px-2 py-0.5 rounded <#if r.reviewerType=='AI'>bg-indigo-50 text-indigo-700<#else>bg-slate-100 text-slate-600</#if>">${r.reviewerType}</span>
                            </td>
                            <td class="px-4 py-3 text-slate-600">${r.scoreText!'-'}</td>
                            <td class="px-4 py-3 text-slate-400">${r.createdAtText}</td>
                        </tr>
                    </#list>
                </tbody>
            </table>
        </div>
        <#if hasMore>
            <div class="mt-6 text-center">
                <a href="/reviews?page=${page+1}" class="text-sm text-indigo-600 hover:underline">查看更多 →</a>
            </div>
        </#if>
    <#else>
        <div class="bg-white rounded-xl shadow-sm border border-slate-200 p-8 text-center">
            <p class="text-sm text-slate-400">暂无评审记录。</p>
            <a href="/" class="mt-3 inline-block text-sm text-indigo-600 hover:underline">去提交第一段代码 →</a>
        </div>
    </#if>
</div>
</@b.layout>
