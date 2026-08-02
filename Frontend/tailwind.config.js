/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#eef7f5',
          100: '#d5ebe6',
          200: '#abd7ce',
          300: '#7bbdb0',
          400: '#4d9f90',
          500: '#328476',
          600: '#26695e',
          700: '#20544d',
          800: '#1c443f',
          900: '#193936',
        },
      },
      fontFamily: {
        sans: ['Inter', 'Segoe UI', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
