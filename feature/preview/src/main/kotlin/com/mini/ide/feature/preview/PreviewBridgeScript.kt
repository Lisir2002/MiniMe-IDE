package com.mini.ide.feature.preview

import com.mini.ide.engine.api.PreviewDocument

/**
 * 预览桥接脚本注入器 —— 对应设计说明 §2 决策 3。
 *
 * 把一段脚本注入到用户预览 HTML 的最前面，劫持 console 与 window.onerror，
 * 经独立的 window.AndroidBridge.post（PreviewBridge）回传 Kotlin。
 * 纯函数实现，可 JVM 单测；脚本内容用 raw string，避免 $ 插值（JS 内不用 $ 符号）。
 */
object PreviewBridgeScript {

    /** payload 格式与 RenderEngineImpl.onMessage 约定一致。 */
    const val SCRIPT = """
<script>
(function () {
  "use strict";
  function post(type, payload) {
    if (window.AndroidBridge && typeof window.AndroidBridge.post === "function") {
      try { window.AndroidBridge.post(JSON.stringify({ type: type, payload: payload })); } catch (e) {}
    }
  }
  var LEVELS = ["log", "info", "warn", "error", "debug"];
  LEVELS.forEach(function (level) {
    var orig = console[level];
    console[level] = function () {
      var args = [];
      for (var i = 0; i < arguments.length; i++) {
        var a = arguments[i];
        if (typeof a === "object") { try { a = JSON.stringify(a); } catch (e) { a = String(a); } }
        args.push(String(a));
      }
      var upper = level.toUpperCase();
      if (level === "info" || level === "debug") upper = "LOG";
      post("CONSOLE", upper + "|0|" + args.join(" "));
      if (orig) orig.apply(console, arguments);
    };
  });
  window.addEventListener("error", function (e) {
    var line = e.lineno || 0;
    var src = e.filename || "app.js";
    var msg = e.message || "运行时错误";
    post("RUNTIME_ERROR", line + "|" + src + "|" + msg);
  });
  window.addEventListener("unhandledrejection", function (e) {
    var msg = e.reason && e.reason.message ? e.reason.message : String(e.reason);
    post("RUNTIME_ERROR", "0|promise|" + msg);
  });
  post("RENDER_DONE", "");
})();
</script>
"""

    /** 把桥接脚本注入到预览 HTML 的 <head> 之后；无 head 时前置。 */
    fun wrap(document: PreviewDocument): PreviewDocument {
        val html = document.html
        val injected = when {
            html.contains("<head>", ignoreCase = true) ->
                html.replaceFirst(Regex("<head>", RegexOption.IGNORE_CASE), "<head>" + SCRIPT)
            html.contains("<html", ignoreCase = true) -> {
                val match = Regex("<html[^>]*>", RegexOption.IGNORE_CASE).find(html)!!
                html.substring(0, match.range.last + 1) + SCRIPT + html.substring(match.range.last + 1)
            }
            else -> SCRIPT + html
        }
        return document.copy(html = injected)
    }
}
