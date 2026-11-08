import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { App } from './app';
import { routes } from './app.routes';

describe('Shell', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideZonelessChangeDetection(), provideRouter(routes)],
    });
  });

  it('renderiza o componente raiz', () => {
    const fixture = TestBed.createComponent(App);
    expect(fixture.componentInstance).toBeTruthy();
    expect(fixture.nativeElement.querySelector('router-outlet')).not.toBeNull();
  });

  it('renderiza a página inicial na rota raiz', async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/');
    const titulo = harness.routeNativeElement?.querySelector('h1');
    expect(titulo?.textContent).toContain('Frontend pronto para evoluir');
  });
});
