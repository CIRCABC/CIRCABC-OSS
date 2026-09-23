import { ChangeDetectionStrategy, Component } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { HeaderComponent } from 'app/shared/header/header.component';
import { NavigatorComponent } from 'app/shared/navigator/navigator.component';

/**
 * Standalone page component rendered when a requested resource or route
 * cannot be found (the application's "no content found" / 404-style view).
 *
 * It renders the shared application {@link HeaderComponent} and
 * {@link NavigatorComponent} around a translated "content not found" message,
 * using Transloco for internationalization.
 *
 * The component holds no state and exposes no `input()` or `output()`
 * members; all behavior is presentational and driven by its template.
 *
 * @remarks Selector: `cbc-no-content-found`.
 */
@Component({
  selector: 'cbc-no-content-found',
  templateUrl: './no-content-found.component.html',
  styleUrl: './no-content-found.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [HeaderComponent, NavigatorComponent, TranslocoModule],
})
export class NoContentFoundComponent {}
