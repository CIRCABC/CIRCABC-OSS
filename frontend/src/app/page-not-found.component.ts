import { ChangeDetectionStrategy, Component } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { HeaderComponent } from 'app/shared/header/header.component';
import { NavigatorComponent } from 'app/shared/navigator/navigator.component';

/**
 * Standalone component rendered for the "404 / page not found" route.
 *
 * It presents a not-found page composed of the shared application
 * {@link HeaderComponent} and {@link NavigatorComponent}, together with
 * translated messaging provided through Transloco. The component holds no
 * state or behavior of its own; it exists purely to display the
 * page-not-found template when a user navigates to an unknown URL.
 */
@Component({
  selector: 'cbc-not-found',
  templateUrl: './page-not-found.component.html',
  styleUrl: './page-not-found.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [HeaderComponent, NavigatorComponent, TranslocoModule],
})
export class PageNotFoundComponent {}
