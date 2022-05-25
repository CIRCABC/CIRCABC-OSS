import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Category, CategoryService } from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-category-box',
  templateUrl: './category-box.component.html',
  styleUrls: ['./category-box.component.scss'],
})
export class CategoryBoxComponent {
  @Input() category!: Category;
  @Input() userId!: string;
  @Output() readonly userUninvited = new EventEmitter();

  public uninviting = false;

  constructor(private categoryService: CategoryService) {}

  public async uninviteUSer() {
    if (this.category && this.category.id && this.userId) {
      this.uninviting = true;
      try {
        await firstValueFrom(
          this.categoryService.deleteCategoryAdministartor(
            this.category.id,
            this.userId
          )
        );

        this.userUninvited.emit();
      } catch (error) {
        console.error(error);
      }
      this.uninviting = false;
    }
  }
}
