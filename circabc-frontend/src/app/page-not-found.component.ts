import { Component } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { HeaderComponent } from 'app/shared/header/header.component';
import { NavigatorComponent } from 'app/shared/navigator/navigator.component';

@Component({
  selector: 'cbc-not-found',
  templateUrl: './page-not-found.component.html',
  styleUrl: './page-not-found.component.scss',
  preserveWhitespaces: true,
  imports: [HeaderComponent, NavigatorComponent, TranslocoModule],
})
export class PageNotFoundComponent {}
