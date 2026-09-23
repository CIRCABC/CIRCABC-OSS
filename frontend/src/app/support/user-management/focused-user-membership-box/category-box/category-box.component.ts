import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  output,
} from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { type Category, CategoryService } from 'app/core/generated/circabc';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

/**
 * Displays a single category in which a focused user holds an administrator
 * role, within the support user-management membership overview.
 *
 * The component renders the category's localized title (via {@link I18nPipe})
 * together with an inline delete control (via {@link InlineDeleteComponent})
 * that lets a support operator revoke the user's category-administrator
 * assignment. Removal is delegated to the generated {@link CategoryService}
 * and, on success, notifies the parent through the {@link userUninvited}
 * output so the surrounding view can refresh.
 */
@Component({
  selector: 'cbc-category-box',
  templateUrl: './category-box.component.html',
  styleUrl: './category-box.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [InlineDeleteComponent, I18nPipe, TranslocoModule],
})
export class CategoryBoxComponent {
  /** Generated API client used to remove the user's category-administrator role. */
  private readonly categoryService = inject(CategoryService);

  /** Required input: the category whose administrator assignment is displayed. */
  category = input.required<Category>();
  /** Required input: identifier of the user whose membership is being managed. */
  readonly userId = input.required<string>();
  /**
   * Emitted once the user has been successfully removed as an administrator
   * of the category, allowing the parent component to refresh its state.
   */
  readonly userUninvited = output();

  /**
   * Indicates whether an uninvite request is currently in flight, used to
   * drive loading/disabled UI state on the delete control.
   */
  public uninviting = false;

  /**
   * Revokes the focused user's administrator role for the current category.
   *
   * Reads the current {@link userId} and category id, and only proceeds when
   * both are present. On success it emits {@link userUninvited}; any request
   * error is caught and logged. The {@link uninviting} flag is toggled around
   * the request to reflect progress in the UI.
   *
   * @returns A promise that resolves once the uninvite attempt completes
   * (whether it succeeded or failed).
   */
  public async uninviteUSer() {
    const userId = this.userId();
    const categoryId = this.category().id;
    if (categoryId && userId) {
      this.uninviting = true;
      try {
        await this.categoryService.deleteCategoryAdministartorAsync({
          id: categoryId,
          userId,
        });

        this.userUninvited.emit();
      } catch (error) {
        console.error(error);
      }
      this.uninviting = false;
    }
  }
}
