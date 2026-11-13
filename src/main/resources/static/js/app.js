/* 站点脚本（原生 JS，零依赖）：
 *
 *  1) 移动端菜单展开/收起 —— data-menu-toggle + data-menu-panel，切换 hidden class。
 *  2) 表单提交态 —— form[data-loading-form] 提交时禁用按钮并改为「分析中…」，
 *     避免重复提交并给用户反馈；CSP script-src 'self' 下不允许内联 onsubmit，
 *     故统一在此用 addEventListener。
 *
 * 约束：不得写内联事件属性（会被 CSP 拦死），不得引入依赖 eval/new Function 的库。
 */
document.addEventListener("DOMContentLoaded", function () {

  /* ---- 1) 移动端菜单 ---- */
  var toggle = document.querySelector("[data-menu-toggle]");
  var panel = document.querySelector("[data-menu-panel]");
  if (toggle && panel) {
    var setOpen = function (open) {
      panel.classList.toggle("hidden", !open);
      toggle.setAttribute("aria-expanded", open ? "true" : "false");
    };
    toggle.addEventListener("click", function () {
      setOpen(panel.classList.contains("hidden"));
    });
    panel.querySelectorAll("a").forEach(function (a) {
      a.addEventListener("click", function () {
        setOpen(false);
      });
    });
  }

  /* ---- 2) 表单提交态 ---- */
  document.querySelectorAll("form[data-loading-form]").forEach(function (form) {
    form.addEventListener("submit", function () {
      var btn = form.querySelector("button[type=submit][data-submit-label]");
      if (!btn) {
        return;
      }
      if (btn.disabled) {
        return;
      }
      btn.disabled = true;
      btn.classList.add("opacity-60", "cursor-not-allowed");
      btn.textContent = "分析中…";
    });
  });
});
