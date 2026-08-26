import { DatePipe } from '@angular/common';
import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { HelpSubcategory } from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

@Component({
  selector: 'cbc-subcategory-list-select',
  templateUrl: './subcategory-list-select.component.html',
  styleUrl: './subcategory-list-select.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, I18nPipe, TranslocoModule, DatePipe],
})
export class SubcategoryListSelectComponent {
  readonly subcategories = input<HelpSubcategory[]>([]);
  readonly currentId = input<string>();
  readonly categoryId = input<string>();
}
