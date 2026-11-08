import { defineConfig, devices } from '@playwright/test';

const porta = process.env['APP_PORT'] ?? '4200';

export default defineConfig({
  testDir: './e2e',
  testMatch: '**/*.e2e.ts',
  globalSetup: './e2e/global-setup.ts',
  globalTeardown: './e2e/global-teardown.ts',
  // Os cenários compartilham um único banco; rodar em série evita interferência entre eles.
  fullyParallel: false,
  workers: 1,
  retries: 0,
  timeout: 60_000,
  expect: { timeout: 10_000 },
  reporter: [
    ['list'],
    ['html', { outputFolder: 'playwright-report', open: 'never' }],
    ['junit', { outputFile: 'test-results/junit.xml' }],
  ],
  outputDir: 'test-results/artefatos',
  use: {
    baseURL: `http://localhost:${porta}`,
    locale: 'pt-BR',
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    { name: 'desktop', use: { ...devices['Desktop Chrome'] } },
    // Chromium com viewport e toque de celular (só Chromium é instalado).
    { name: 'movel', use: { ...devices['Pixel 7'] } },
  ],
});
