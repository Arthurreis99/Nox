/* global NoxFilter */
(function () {
  "use strict";

  const nativeApp = "nox";

  function notifyNative(payload) {
    try {
      const result = browser.runtime.sendNativeMessage(nativeApp, payload);
      if (result && typeof result.catch === "function") result.catch(function () {});
    } catch (_) {
      // The extension continues protecting the page if the native UI is unavailable.
    }
  }

  browser.webRequest.onBeforeRequest.addListener(
    function (details) {
      if (details.type === "main_frame") {
        const sanitized = NoxFilter.sanitizeNavigationUrl(details.url);
        if (sanitized !== details.url) return { redirectUrl: sanitized };
      }

      if (NoxFilter.shouldBlockRequest(details.url)) {
        notifyNative({ type: "blocked" });
        return { cancel: true };
      }
      return {};
    },
    { urls: ["<all_urls>"] },
    ["blocking"]
  );

  browser.webRequest.onBeforeSendHeaders.addListener(
    function (details) {
      const headers = details.requestHeaders || [];
      let hasDnt = false;
      let hasGpc = false;
      headers.forEach(function (header) {
        const name = String(header.name || "").toLowerCase();
        if (name === "dnt") {
          header.value = "1";
          hasDnt = true;
        }
        if (name === "sec-gpc") {
          header.value = "1";
          hasGpc = true;
        }
      });
      if (!hasDnt) headers.push({ name: "DNT", value: "1" });
      if (!hasGpc) headers.push({ name: "Sec-GPC", value: "1" });
      return { requestHeaders: headers };
    },
    { urls: ["<all_urls>"] },
    ["blocking", "requestHeaders"]
  );

  browser.runtime.onMessage.addListener(function (message) {
    if (!message || typeof message !== "object") return;
    if (message.type === "adSkipped" || message.type === "blocked") {
      notifyNative(message);
    }
  });
})();
