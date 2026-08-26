// script to serve  with express and gzip 
// need to install express ,  compression, http-proxy-middleware
// npm i express  compression http-proxy-middleware
const express = require('express');
const compression = require('compression');
const { createProxyMiddleware } = require('http-proxy-middleware');
const path = require('path');
const app = express();


// Enable gzip compression for all responses
app.use(compression());

// Alfresco API proxy
app.use('/alfresco_api', createProxyMiddleware({
  target: 'http://10.178.57.119:8080/alfresco/service/api',
  secure: false,
  changeOrigin: true,
  pathRewrite: {
    '^/alfresco_api': ''
  }
}));

// Circabc API proxy
app.use('/circabc_api', createProxyMiddleware({
  target: 'http://10.178.57.119:8080/alfresco/service/circabc',
  secure: false,
  changeOrigin: true,
  pathRewrite: {
    '^/circabc_api': ''
  }
}));

// Alfresco Root proxy
app.use('/alfresco_root', createProxyMiddleware({
  target: 'http://10.178.57.119:8080/alfresco',
  secure: false,
  changeOrigin: true,
  pathRewrite: {
    '^/alfresco_root': ''
  }
}));

// Alfresco Host proxy
app.use('/alfresco_host', createProxyMiddleware({
  target: 'http://10.178.57.119:8080',
  secure: false,
  changeOrigin: true,
  pathRewrite: {
    '^/alfresco_host': ''
  }
}));


// Serve static files from the Angular dist directory
app.use('/ui',express.static(path.join(__dirname, 'dist/circabc')));

// Catch-all route: send index.html for any requests that don't match a static file
app.get('/ui{/*path}', (req, res) => {
  res.sendFile(path.join(__dirname, 'dist/circabc', 'index.html'));
});

const port = process.env.PORT || 4200;
app.listen(port, () => {
  console.log(`Server running on port ${port}`);
});
