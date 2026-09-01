(function (root, factory) {
  const api = factory();
  if (typeof module === "object" && module.exports) {
    module.exports = api;
  } else {
    root.NoxFilter = api;
  }
})(typeof globalThis !== "undefined" ? globalThis : this, function () {
  "use strict";

  const blockedHosts = Object.freeze([
    "2mdn.net",
    "ad.doubleclick.net",
    "adservice.google.com",
    "adservice.google.com.br",
    "doubleclick.net",
    "googleadservices.com",
    "googlesyndication.com",
    "imasdk.googleapis.com"
  ]);

  const youtubeBlockedPaths = Object.freeze([
    "/api/stats/ads",
    "/pagead/",
    "/ptracking",
    "/get_midroll_info"
  ]);

  const trackingParameters = Object.freeze([
    "dclid",
    "fbclid",
    "gclid",
    "mc_cid",
    "mc_eid",
    "msclkid",
    "utm_campaign",
    "utm_content",
    "utm_medium",
    "utm_source",
    "utm_term"
  ]);

  function hostMatches(host, rule) {
    return host === rule || host.endsWith("." + rule);
  }

  function isBlockedHostname(hostname) {
    const host = String(hostname || "").toLowerCase().replace(/^\.+|\.+$/g, "");
    return blockedHosts.some(function (rule) {
      return hostMatches(host, rule);
    });
  }

  function isYouTubeHost(hostname) {
    const host = String(hostname || "").toLowerCase();
    return hostMatches(host, "youtube.com");
  }

  function shouldBlockRequest(rawUrl) {
    let url;
    try {
      url = new URL(rawUrl);
    } catch (_) {
      return false;
    }

    if (isBlockedHostname(url.hostname)) return true;
    if (!isYouTubeHost(url.hostname)) return false;

    const path = url.pathname.toLowerCase();
    return youtubeBlockedPaths.some(function (blockedPath) {
      return path.includes(blockedPath);
    });
  }

  function sanitizeNavigationUrl(rawUrl) {
    let url;
    try {
      url = new URL(rawUrl);
    } catch (_) {
      return rawUrl;
    }

    let changed = false;
    trackingParameters.forEach(function (parameter) {
      if (url.searchParams.has(parameter)) {
        url.searchParams.delete(parameter);
        changed = true;
      }
    });
    return changed ? url.toString() : rawUrl;
  }

  return Object.freeze({
    blockedHosts: blockedHosts,
    trackingParameters: trackingParameters,
    hostMatches: hostMatches,
    isBlockedHostname: isBlockedHostname,
    shouldBlockRequest: shouldBlockRequest,
    sanitizeNavigationUrl: sanitizeNavigationUrl
  });
});
