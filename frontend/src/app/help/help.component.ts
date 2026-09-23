import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from 'app/shared/header/header.component';
import { NavigatorComponent } from 'app/shared/navigator/navigator.component';

/**
 * Standalone layout component for the application's help section.
 *
 * Rendered via the `cbc-help` selector, this component provides the shell
 * for the help pages. It composes the shared {@link HeaderComponent} and
 * {@link NavigatorComponent} around a {@link RouterOutlet}, so that the
 * concrete help sub-pages are displayed inside the standard header/navigator
 * chrome as they are activated by the router.
 *
 * The component holds no state or behavior of its own; it acts purely as a
 * container/layout wrapper for the routed help content.
 */
@Component({
  selector: 'cbc-help',
  templateUrl: './help.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [HeaderComponent, NavigatorComponent, RouterOutlet],
})
export class HelpComponent {}
