// script to serve  with express and gzip 
// need to install express ,  compression, http-proxy-middleware
// npm i express  compression http-proxy-middleware
const express = require('express');
const compression = require('compression');
const { createProxyMiddleware } = require('http-proxy-middleware');
const path = require('node:path');
const fs = require('node:fs');
const app = express();
app.disable('x-powered-by');


// Enable gzip compression for all responses
app.use(compression());


// Parametrize Alfresco host
const ALFRESCO_HOST = process.env.ALFRESCO_HOST || 'localhost';
const ALFRESCO_PORT = process.env.ALFRESCO_PORT || '8080';
const ALFRESCO_BASE = `http://${ALFRESCO_HOST}:${ALFRESCO_PORT}`;

// --- Security Headers ---
app.use((req, res, next) => {
  res.setHeader('X-Content-Type-Options', 'nosniff');
  res.setHeader('X-Frame-Options', 'DENY');
  res.setHeader('Referrer-Policy', 'strict-origin-when-cross-origin');
  res.setHeader('Permissions-Policy', 'camera=(), microphone=(), geolocation=()');
  res.setHeader(
    'Content-Security-Policy',
    [
      "default-src 'self'",
      "script-src 'self' 'unsafe-inline' https://webtools.europa.eu",
      "style-src 'self' 'unsafe-inline' https://webtools.europa.eu",
      "img-src 'self' data: blob: https:",
      "font-src 'self'",
      `connect-src 'self' ${ALFRESCO_BASE}`,
      "frame-src 'self' https: http:",
      "frame-ancestors 'none'",
      "base-uri 'self'",
      "form-action 'self'",
    ].join('; ')
  );

  // Only add HSTS when behind HTTPS
  if (req.secure || req.headers['x-forwarded-proto'] === 'https') {
    res.setHeader('Strict-Transport-Security', 'max-age=31536000; includeSubDomains');
  }

  next();
});

// Alfresco API proxy
app.use('/alfresco_api', createProxyMiddleware({
  target: `${ALFRESCO_BASE}/alfresco/service/api`,
  secure: false,
  changeOrigin: true,
  pathRewrite: {
    '^/alfresco_api': ''
  }
}));

// Circabc API proxy
app.use('/circabc_api', createProxyMiddleware({
  target: `${ALFRESCO_BASE}/alfresco/service/circabc`,
  secure: false,
  changeOrigin: true,
  pathRewrite: {
    '^/circabc_api': ''
  }
}));

// Alfresco Root proxy
app.use('/alfresco_root', createProxyMiddleware({
  target: `${ALFRESCO_BASE}/alfresco`,
  secure: false,
  changeOrigin: true,
  pathRewrite: {
    '^/alfresco_root': ''
  }
}));

// Alfresco Host proxy
app.use('/alfresco_host', createProxyMiddleware({
  target: `${ALFRESCO_BASE}`,
  secure: false,
  changeOrigin: true,
  pathRewrite: {
    '^/alfresco_host': ''
  }
}));

// EU Captcha proxy
app.use('/eu_captcha', createProxyMiddleware({
  target: `http://${ALFRESCO_HOST}:9898/eu-captcha`,
  secure: false,
  changeOrigin: true,
  pathRewrite: {
    '^/eu_captcha': ''
  }
}));


// Resolve Angular dist directory (Angular 16+ outputs to dist/<project>/browser)
const staticDir = path.join(__dirname, 'dist', 'circabc','browser');

// Serve static files from the resolved Angular dist directory under /ui
app.use('/ui', express.static(staticDir, { redirect: false }));

// SPA fallback: send index.html for any /ui route that isn't a static file
app.get(/^\/ui(?:\/.*)?$/, (req, res) => {
  res.sendFile(path.join(staticDir, 'index.html'));
});

const port = process.env.PORT || 4200;
app.listen(port, () => {
  console.log(`Server running on port ${port}`);
});
