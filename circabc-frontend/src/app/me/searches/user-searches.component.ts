import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';

@Component({
  selector: 'cbc-user-searches',
  templateUrl: './user-searches.component.html',
  preserveWhitespaces: true,
  imports: [RouterLink, TranslocoModule],
})
export class UserSearchesComponent {}
