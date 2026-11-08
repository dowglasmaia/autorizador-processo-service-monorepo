import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

export type TagTone = 'accent' | 'accent-2' | 'neutral';

const TONES: Record<TagTone, string> = {
  accent: 'bg-accent-100 text-accent-800',
  'accent-2': 'bg-danger-soft text-danger',
  neutral: 'bg-neutral-200 text-neutral-800',
};

@Component({
  selector: 'app-tag',
  template: '<ng-content />',
  host: { '[class]': 'classes()' },
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Tag {
  readonly tone = input<TagTone>('neutral');
  protected readonly classes = computed(
    () => `inline-flex items-center rounded-pill px-3 py-1 text-xs font-semibold ${TONES[this.tone()]}`,
  );
}
