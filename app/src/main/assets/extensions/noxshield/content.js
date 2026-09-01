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
    ".ytp-ad-skip-button-slot",
    ".ytp-ad-skip-button-container button",
    "button[aria-label^='Skip']",
    "button[aria-label^='Pular']",
    "button.ytp-ad-skip-button",
    "button[class*='skip']"
  ];

  let currentAd = null;
  let lastCleanup = 0;
  let sweepQueued = false;

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
    const adShowing = Boolean(player && (
      player.classList.contains("ad-showing") ||
      player.classList.contains("ad-interrupting")
    ));
    const video = player ? player.querySelector("video") : null;

    if (!adShowing) {
      if (currentAd && video) {
        video.defaultPlaybackRate = currentAd.defaultPlaybackRate;
        video.playbackRate = currentAd.playbackRate;
        video.muted = currentAd.muted;
      }
      currentAd = null;
      return;
    }

    if (!currentAd) {
      currentAd = {
        muted: video ? video.muted : false,
        defaultPlaybackRate: video ? video.defaultPlaybackRate : 1,
        playbackRate: video ? video.playbackRate : 1,
        reported: false
      };
    }

    const skipButton = skipSelectors
      .map(function (selector) { return player.querySelector(selector); })
      .find(function (element) { return element && (isVisible(element) || element.offsetParent); });

    if (skipButton) {
      skipButton.click();
      if (!currentAd.reported) {
        currentAd.reported = true;
        send("adSkipped", { method: "button" });
      }
      // Keep advancing the media as a fallback: YouTube can ignore synthetic clicks.
    }

    if (video) {
      video.muted = true;
      video.defaultPlaybackRate = 16;
      video.playbackRate = 16;
      let seekableEnd = 0;
      try {
        if (video.seekable && video.seekable.length > 0) {
          seekableEnd = video.seekable.end(video.seekable.length - 1);
        }
      } catch (_) {}
      const adEnd = Math.max(
        Number.isFinite(video.duration) ? video.duration : 0,
        Number.isFinite(seekableEnd) ? seekableEnd : 0
      );

      if (adEnd > video.currentTime + 0.05) {
        try {
          video.currentTime = Math.max(video.currentTime, adEnd - 0.01);
          const playResult = video.play();
          if (playResult && typeof playResult.catch === "function") {
            playResult.catch(function () {});
          }
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

  function queueSweep() {
    if (sweepQueued) return;
    sweepQueued = true;
    requestAnimationFrame(function () {
      sweepQueued = false;
      sweep();
    });
  }

  const observer = new MutationObserver(queueSweep);
  observer.observe(document.documentElement, { childList: true, subtree: true });

  document.addEventListener("yt-navigate-finish", queueSweep, true);
  document.addEventListener("visibilitychange", queueSweep, true);
  document.addEventListener("durationchange", queueSweep, true);
  document.addEventListener("loadedmetadata", queueSweep, true);
  setInterval(sweep, 200);
  sweep();
})();
