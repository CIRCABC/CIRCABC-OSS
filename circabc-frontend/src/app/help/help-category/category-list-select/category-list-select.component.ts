import { Component, Input } from '@angular/core';
import { HelpCategory } from 'app/core/generated/circabc';

@Component({
  selector: 'cbc-category-list-select',
  templateUrl: './category-list-select.component.html',
  styleUrls: ['./category-list-select.component.scss'],
})
export class CategoryListSelectComponent {
  @Input()
  categories: HelpCategory[] = [];
  @Input()
  currentId: string | undefined;
}
