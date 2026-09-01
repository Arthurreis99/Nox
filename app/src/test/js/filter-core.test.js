"use strict";

const assert = require("node:assert/strict");
const filter = require("../../main/assets/extensions/noxshield/filter-core.js");

assert.equal(filter.isBlockedHostname("doubleclick.net"), true);
assert.equal(filter.isBlockedHostname("ad.doubleclick.net"), true);
assert.equal(filter.isBlockedHostname("notdoubleclick.net"), false);
assert.equal(filter.shouldBlockRequest("https://www.youtube.com/api/stats/ads?ver=2"), true);
assert.equal(filter.shouldBlockRequest("https://rr1---sn.googlevideo.com/videoplayback?id=1"), false);
assert.equal(
  filter.sanitizeNavigationUrl("https://m.youtube.com/watch?v=abc&utm_source=test&fbclid=x"),
  "https://m.youtube.com/watch?v=abc"
);
assert.equal(filter.sanitizeNavigationUrl("not a url"), "not a url");

process.stdout.write("Nox Shield filter tests passed\n");
