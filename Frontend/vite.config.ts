import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'node:path'

// El backend es un .war desplegado en Tomcat 10.1 (contexto /VETERINARIAITQ).
// En desarrollo hacemos proxy para evitar depender de la config CORS del servidor.
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: { '@': path.resolve(__dirname, './src') },
  },
  server: {
    port: 5174,
    proxy: {
      '/api': {
        target: 'http://localhost:8080/VETERINARIAITQ',
        changeOrigin: true,
      },
    },
  },
})
