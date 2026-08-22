// Allows tunneling tools (ngrok, etc.) to reach the dev server for testing on real devices —
// webpack-dev-server rejects unrecognized Host headers by default (DNS rebinding protection).
config.devServer = config.devServer || {};
config.devServer.allowedHosts = "all";
