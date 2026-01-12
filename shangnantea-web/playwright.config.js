// @ts-check
const { defineConfig } = require('@playwright/test')

module.exports = defineConfig({
  testDir: './e2e',
  timeout: 30000,
  expect: {
    timeout: 5000
  },
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: 0,
  workers: 1,
  reporter: [
    ['html', { outputFolder: 'e2e-report' }],
    ['list']
  ],
  use: {
    baseURL: 'http://localhost:8083',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure'
  },
  projects: [
    {
      name: 'chromium',
      use: { 
        browserName: 'chromium',
        headless: false  // 显示浏览器窗口，方便调试
      }
    }
  ]
})
