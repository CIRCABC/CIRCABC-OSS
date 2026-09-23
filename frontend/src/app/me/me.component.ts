import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from 'app/shared/header/header.component';
import { NavigatorComponent } from 'app/shared/navigator/navigator.component';

/**
 * Standalone shell component for the user's personal area ("me" section).
 *
 * Rendered at the `cbc-me` selector, it lays out the personal-space page by
 * combining the shared application {@link HeaderComponent} and
 * {@link NavigatorComponent} with a {@link RouterOutlet}. The router outlet
 * hosts the lazily routed child views of the personal area (e.g. profile,
 * settings, notifications), while the header and navigator provide the
 * surrounding chrome and navigation.
 *
 * The component holds no state or logic of its own; it acts purely as a
 * layout container. Change detection uses the OnPush strategy; the component
 * has no template-bound state, so no manual triggers are required.
 */
@Component({
  selector: 'cbc-me',
  templateUrl: './me.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [HeaderComponent, NavigatorComponent, RouterOutlet],
})
export class MeComponent {}
