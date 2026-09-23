import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';

/**
 * Standalone component that renders the current user's saved searches area
 * within the personal "me" section of the application.
 *
 * The component is presentational: it has no inputs, outputs or behavioural
 * logic. Its associated template (`user-searches.component.html`) provides the
 * static layout, uses {@link RouterLink} for in-app navigation and
 * {@link TranslocoModule} for translating the displayed labels.
 *
 * Rendered via the `cbc-user-searches` selector.
 */
@Component({
  selector: 'cbc-user-searches',
  templateUrl: './user-searches.component.html',
  preserveWhitespaces: true,
  imports: [RouterLink, TranslocoModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserSearchesComponent {}
