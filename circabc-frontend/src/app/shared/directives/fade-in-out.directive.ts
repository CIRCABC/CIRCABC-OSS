import {
  AnimationBuilder,
  AnimationMetadata,
  animate,
  sequence,
  style,
} from '@angular/animations';
import { Directive, ElementRef, input, effect } from '@angular/core';

@Directive({
  selector: '[cbcFadeInOut]',
})
export class FadeInOutDirective {
  public show = input.required<boolean>();

  constructor(
    private builder: AnimationBuilder,
    private el: ElementRef
  ) {
    effect(() => {
      const metadata = this.show() ? this.fadeIn() : this.fadeOut();

      const factory = this.builder.build(metadata);
      const player = factory.create(this.el.nativeElement);

      player.play();
    });
  }

  private fadeIn(): AnimationMetadata[] {
    return [
      style({ opacity: 0, height: 0 }),
      sequence([
        animate('200ms', style({ height: 'fit-content' })),
        animate('200ms ease-in', style({ opacity: 1 })),
      ]),
    ];
  }

  private fadeOut(): AnimationMetadata[] {
    return [
      style({ opacity: '*' }),
      sequence([
        animate('200ms ease-out', style({ opacity: 0 })),
        animate('200ms', style({ height: 0 })),
      ]),
    ];
  }
}
