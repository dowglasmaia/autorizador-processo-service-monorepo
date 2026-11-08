import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-card',
  template: '<ng-content />',
  host: { class: 'block rounded-card border border-divider bg-surface p-5 shadow-card' },
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Card {}
