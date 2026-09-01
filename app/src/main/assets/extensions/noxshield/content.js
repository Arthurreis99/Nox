(function () {
  "use strict";

  const adSelectors = [
    "ytd-display-ad-renderer",
    "ytd-promoted-sparkles-web-renderer",
    "ytd-promoted-video-renderer",
    "ytd-in-feed-ad-layout-renderer",
    "ytd-banner-promo-renderer",
    "ytm-promoted-sparkles-web-renderer",
    "ytm-promoted-video-renderer",
    "#masthead-ad",
    "#player-ads"
  ];

  const skipSelectors = [
    ".ytp-ad-skip-button",
    ".ytp-ad-skip-button-modern",
    ".ytp-skip-ad-button",
    "button.ytp-ad-skip-button",
    "button[class*='skip']"
  ];

  let currentAd = null;
  let lastCleanup = 0;

  function send(type, extra) {
    try {
      browser.runtime.sendMessage(Object.assign({ type: type }, extra || {}));
    } catch (_) {}
  }

  function isVisible(element) {
    if (!element) return false;
    const rect = element.getBoundingClientRect();
    const style = getComputedStyle(element);
    return rect.width > 0 && rect.height > 0 && style.display !== "none" && style.visibility !== "hidden";
  }

  function removePageAds() {
    const now = Date.now();
    if (now - lastCleanup < 350) return;
    lastCleanup = now;

    let removed = 0;
    document.querySelectorAll(adSelectors.join(",")).forEach(function (element) {
      if (element.dataset.noxHidden === "1") return;
      element.dataset.noxHidden = "1";
      element.setAttribute("aria-hidden", "true");
      element.style.setProperty("display", "none", "important");
      removed += 1;
    });
    if (removed > 0) send("blocked", { count: removed });
  }

  function skipPlayerAd() {
    const player = document.querySelector(".html5-video-player");
    const adShowing = Boolean(player && player.classList.contains("ad-showing"));
    const video = player ? player.querySelector("video") : null;

    if (!adShowing) {
      if (currentAd && video) {
        video.playbackRate = currentAd.playbackRate;
        video.muted = currentAd.muted;
      }
      currentAd = null;
      return;
    }

    if (!currentAd) {
      currentAd = {
        muted: video ? video.muted : false,
        playbackRate: video ? video.playbackRate : 1,
        reported: false
      };
    }

    const skipButton = skipSelectors
      .map(function (selector) { return document.querySelector(selector); })
      .find(isVisible);

    if (skipButton) {
      skipButton.click();
      if (!currentAd.reported) {
        currentAd.reported = true;
        send("adSkipped", { method: "button" });
      }
      return;
    }

    if (video) {
      video.muted = true;
      video.playbackRate = 16;
      if (Number.isFinite(video.duration) && video.duration > 0.2) {
        try {
          video.currentTime = Math.max(video.currentTime, video.duration - 0.05);
          if (!currentAd.reported) {
            currentAd.reported = true;
            send("adSkipped", { method: "seek" });
          }
        } catch (_) {}
      }
    }
  }

  function sweep() {
    removePageAds();
    skipPlayerAd();
  }

  const observer = new MutationObserver(sweep);
  observer.observe(document.documentElement, { childList: true, subtree: true });

  document.addEventListener("yt-navigate-finish", sweep, true);
  document.addEventListener("visibilitychange", sweep, true);
  setInterval(sweep, 500);
  sweep();
})();
