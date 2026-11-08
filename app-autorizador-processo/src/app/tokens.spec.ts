// @ts-expect-error -- sem @types/node no projeto; o módulo existe no runtime de testes
import { readFileSync } from 'node:fs';

describe('tokens de design (src/styles.scss)', () => {
  const css: string = readFileSync('src/styles.scss', 'utf8');
  const tokens = [...css.matchAll(/^\s*(--(?:color|radius|shadow|font)-[\w-]+):\s*([^;]+);/gm)].map(
    (m) => `${m[1]}: ${m[2].trim()}`,
  );

  it('mantém os tokens semânticos principais', () => {
    expect(tokens).toContain('--color-accent: #0fa88c');
    expect(tokens).toContain('--color-accent-800: #065e52');
    expect(tokens).toContain('--color-danger: #aa3a2a');
    expect(tokens).toContain('--radius-card: 20px');
    expect(tokens).toContain('--radius-pill: 999px');
  });

  it('snapshot da lista de tokens', () => {
    expect(tokens).toMatchSnapshot();
  });
});
