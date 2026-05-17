import { defineConfig } from '@playwright/test'

const port = Number(process.env.E2E_PORT || 4173)
const frontendBaseUrl = process.env.E2E_BASE_URL || `http://127.0.0.1:${port}`

export default defineConfig({
    testDir: './tests/e2e',
    fullyParallel: false,
    timeout: 60_000,
    expect: {
        timeout: 10_000
    },
    reporter: 'list',
    retries: process.env.CI ? 2 : 0,
    use: {
        baseURL: frontendBaseUrl,
        headless: true,
        trace: 'retain-on-failure',
        viewport: {
            width: 1440,
            height: 900
        }
    },
    webServer: process.env.E2E_USE_EXISTING_FRONTEND
        ? undefined
        : {
              command: `npm run dev -- --host 127.0.0.1 --port ${port}`,
              url: frontendBaseUrl,
              reuseExistingServer: !process.env.CI,
              timeout: 120_000
          }
})
