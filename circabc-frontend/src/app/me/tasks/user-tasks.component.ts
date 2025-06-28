import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'cbc-user-tasks',
  templateUrl: './user-tasks.component.html',
  preserveWhitespaces: true,
  imports: [RouterLink],
})
export class UserTasksComponent {}
