import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { HelpCategory } from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

@Component({
  selector: 'cbc-category-list-select',
  templateUrl: './category-list-select.component.html',
  styleUrl: './category-list-select.component.scss',
  imports: [RouterLink, I18nPipe, TranslocoModule],
})
export class CategoryListSelectComponent {
  readonly categories = input<HelpCategory[]>([]);
  readonly currentId = input<string>();
}
