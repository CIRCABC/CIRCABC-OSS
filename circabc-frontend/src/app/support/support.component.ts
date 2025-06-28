import { Component } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { HeaderComponent } from 'app/shared/header/header.component';
import { NavigatorComponent } from 'app/shared/navigator/navigator.component';

@Component({
  selector: 'cbc-support',
  templateUrl: './support.component.html',
  preserveWhitespaces: true,
  imports: [
    HeaderComponent,
    NavigatorComponent,
    RouterLink,
    RouterOutlet,
    TranslocoModule,
  ],
})
export class SupportComponent {
  constructor(private router: Router) {}

  public isRoute(part: string) {
    return this.router.url.indexOf(part) > -1;
  }
}
