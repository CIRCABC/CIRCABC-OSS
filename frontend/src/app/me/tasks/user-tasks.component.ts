import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/**
 * Standalone component that renders the user's tasks view within the
 * personal area (`me`) of the application.
 *
 * Rendered via the `cbc-user-tasks` selector, it displays the associated
 * `user-tasks.component.html` template. The component relies on Angular's
 * {@link RouterLink} directive to provide in-app navigation from the tasks
 * view to other routes.
 *
 * The component currently holds no state or behaviour of its own; its role
 * is limited to presenting the tasks template and enabling router-based
 * links declared in that template.
 */
@Component({
  selector: 'cbc-user-tasks',
  templateUrl: './user-tasks.component.html',
  preserveWhitespaces: true,
  imports: [RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserTasksComponent {}
