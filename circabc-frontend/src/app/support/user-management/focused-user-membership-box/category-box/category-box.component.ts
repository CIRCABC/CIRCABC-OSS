import { Component, Input, output, input } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { type Category, CategoryService } from 'app/core/generated/circabc';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-category-box',
  templateUrl: './category-box.component.html',
  styleUrl: './category-box.component.scss',
  imports: [InlineDeleteComponent, I18nPipe, TranslocoModule],
})
export class CategoryBoxComponent {
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input() category!: Category;
  readonly userId = input.required<string>();
  readonly userUninvited = output();

  public uninviting = false;

  constructor(private categoryService: CategoryService) {}

  public async uninviteUSer() {
    const userId = this.userId();
    if (this.category?.id && userId) {
      this.uninviting = true;
      try {
        await firstValueFrom(
          this.categoryService.deleteCategoryAdministartor(
            this.category.id,
            userId
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
