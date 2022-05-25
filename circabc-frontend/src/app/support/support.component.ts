import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'cbc-support',
  templateUrl: './support.component.html',
  preserveWhitespaces: true,
})
export class SupportComponent {
  constructor(private router: Router) {}

  public isRoute(part: string) {
    return this.router.url.indexOf(part) > -1;
  }
}
