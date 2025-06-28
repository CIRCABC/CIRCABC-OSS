// directive like in article https://medium.com/agilix/angular-and-cypress-data-cy-attributes-d698c01df062
import { Directive, ElementRef, Renderer2 } from '@angular/core';
import { environment } from 'environments/environment';
@Directive({
  // eslint-disable-next-line @angular-eslint/directive-selector
  selector: '[data-cy]',
})
export class DataCyDirective {
  constructor(el: ElementRef, renderer: Renderer2) {
    if (environment.environmentType === 'prod') {
      renderer.removeAttribute(el.nativeElement, 'data-cy');
    }
  }
}
