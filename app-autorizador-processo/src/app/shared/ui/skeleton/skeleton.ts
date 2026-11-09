import { ChangeDetectionStrategy, Component } from '@angular/core';

/**
 * Bloco de carregamento com brilho (shimmer, só transform). Defina o tamanho com classes
 * (`h-9 w-24`). Com movimento reduzido fica estático.
 */
@Component({
  selector: 'app-skeleton',
  template: '<span class="absolute inset-0 animate-shimmer bg-gradient-to-r from-transparent via-white/60 to-transparent motion-reduce:hidden"></span>',
  host: {
    class: 'relative block overflow-hidden rounded-input bg-neutral-200',
    'aria-hidden': 'true',
  },
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Skeleton {}
