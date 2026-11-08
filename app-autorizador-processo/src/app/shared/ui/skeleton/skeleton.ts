import { ChangeDetectionStrategy, Component } from '@angular/core';

/** Bloco de carregamento. Defina o tamanho com classes (`h-9 w-24`). */
@Component({
  selector: 'app-skeleton',
  template: '',
  host: {
    class: 'block animate-pulse rounded-input bg-neutral-200 motion-reduce:animate-none',
    'aria-hidden': 'true',
  },
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Skeleton {}
