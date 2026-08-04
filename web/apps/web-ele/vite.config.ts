import { defineConfig } from '@vben/vite-config';

import ElementPlus from 'unplugin-element-plus/vite';

export default defineConfig(async () => {
  return {
    application: {
      injectMetadata: false,
      license: false,
      printInfoMap: {},
    },
    vite: {
      plugins: [
        ElementPlus({
          format: 'esm',
        }),
      ],
      server: {
        proxy: {
          '/api': {
            changeOrigin: true,
            target: 'http://localhost:8087',
            ws: true,
            xfwd: true,
          },
          '/v3/api-docs': {
            changeOrigin: true,
            target: 'http://localhost:8087',
            xfwd: true,
          },
        },
      },
    },
  };
});
