import { Component } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { HeaderComponent } from 'app/shared/header/header.component';
import { NavigatorComponent } from 'app/shared/navigator/navigator.component';

@Component({
  selector: 'cbc-no-content-found',
  templateUrl: './no-content-found.component.html',
  styleUrl: './no-content-found.component.scss',
  imports: [HeaderComponent, NavigatorComponent, TranslocoModule],
})
export class NoContentFoundComponent {}
