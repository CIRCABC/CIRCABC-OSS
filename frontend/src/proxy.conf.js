const PROXY_CONFIG = [
  {
    context: ["/service", "/rest/download", "/d/a"],
    target: "http://localhost/",
    secure: false,
    changeOrigin: true
  },
];

module.exports = PROXY_CONFIG;
