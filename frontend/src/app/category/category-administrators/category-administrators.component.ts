import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { AddCategoryAdministratorComponent } from 'app/category/category-administrators/add-category-administrator/add-category-administrator.component';
import { CategoryService, User } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getErrorTranslation, getSuccessTranslation } from 'app/core/util';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { DownloadPipe } from 'app/shared/pipes/download.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';

/**
 * Standalone Angular component that manages the list of administrators for a
 * given category.
 *
 * It renders the current category administrators (retrieved from the backend
 * via {@link CategoryService}), allows an authorised user to add a new
 * administrator through the {@link AddCategoryAdministratorComponent} modal and
 * to remove ("uninvite") existing administrators. Success and error feedback is
 * surfaced through the {@link UiMessageService}, and translations are resolved
 * with the {@link TranslocoService}.
 *
 * The category whose administrators are displayed is derived from the `id`
 * route parameter exposed by the {@link ActivatedRoute}.
 */
@Component({
  selector: 'cbc-category-administrators',
  templateUrl: './category-administrators.component.html',
  styleUrl: './category-administrators.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    InlineDeleteComponent,
    AddCategoryAdministratorComponent,
    DownloadPipe,
    SecurePipe,
    TranslocoModule,
  ],
})
export class CategoryAdministratorsComponent implements OnInit {
  /** Provides access to the current route parameters (notably the category `id`). */
  private readonly route = inject(ActivatedRoute);
  /** Generated API client used to fetch and mutate category administrators. */
  private readonly categoryService = inject(CategoryService);
  /** Resolves i18n keys into localised messages for user feedback. */
  private readonly translateService = inject(TranslocoService);
  /** Displays success and error notifications to the user. */
  private readonly uiMessageService = inject(UiMessageService);
  /** Provides information about the currently authenticated user. */
  private readonly loginService = inject(LoginService);

  /** Whether an administrator list request is currently in progress. */
  public loading!: boolean;
  /** The administrators currently assigned to the category. */
  public readonly administrators = signal<User[]>([]);
  /** Identifier of the category whose administrators are being managed. */
  public readonly categoryId = signal<string>('');
  /** Controls the visibility of the "add administrator" modal. */
  public readonly showModal = signal(false);

  /**
   * Angular lifecycle hook. Subscribes to the route parameters and loads the
   * administrators for the category identified by the `id` parameter whenever
   * it changes.
   */
  ngOnInit() {
    this.route.params.subscribe(async (params) => {
      await this.initCategory(params.id);
    });
  }

  /**
   * Loads the administrators for the given category and stores them in
   * {@link administrators}. Manages the {@link loading} flag around the request
   * and records the active {@link categoryId}. Any failure is logged to the
   * console and does not propagate.
   *
   * @param id The identifier of the category whose administrators should be loaded.
   * @returns A promise that resolves once the load attempt has completed.
   */
  private async initCategory(id: string) {
    this.loading = true;
    this.categoryId.set(id);
    try {
      if (id) {
        this.administrators.set(
          await this.categoryService.getCategoryAdministratorsAsync({ id })
        );
      }
    } catch (error) {
      console.error(error);
      console.error('impossible to get the category');
    }
    this.loading = false;
  }

  /**
   * Removes ("uninvites") the given administrator from the current category.
   *
   * On success the administrator list is reloaded and a success message is
   * shown; on failure an error message is shown. Does nothing when the
   * administrator has no `userId`.
   *
   * @param admin The administrator to remove from the category.
   * @returns A promise that resolves once the removal attempt has completed.
   */
  public async uninviteUser(admin: User) {
    if (admin?.userId) {
      try {
        await this.categoryService.deleteCategoryAdministartorAsync({
          id: this.categoryId(),
          userId: admin.userId,
        });
        await this.initCategory(this.categoryId());
        const text = this.translateService.translate(
          getSuccessTranslation(ActionType.DELETE_CATEGORY_ADMIN)
        );
        if (text) {
          this.uiMessageService.addSuccessMessage(text, true);
        }
      } catch (error) {
        console.error(error);
        const text = this.translateService.translate(
          getErrorTranslation(ActionType.DELETE_CATEGORY_ADMIN)
        );
        if (text) {
          this.uiMessageService.addErrorMessage(text, true);
        }
      }
    }
  }

  /**
   * Handles the result emitted by the add-administrator modal. Reloads the
   * administrator list and closes the modal on success, or simply closes the
   * modal when the operation was cancelled.
   *
   * @param res The result emitted by the {@link AddCategoryAdministratorComponent}.
   * @returns A promise that resolves once any required refresh has completed.
   */
  public async refresh(res: ActionEmitterResult) {
    if (res.result === ActionResult.SUCCEED) {
      await this.initCategory(this.categoryId());
      this.showModal.set(false);
    } else if (res.result === ActionResult.CANCELED) {
      this.showModal.set(false);
    }
  }

  /**
   * Determines whether the given administrator may be deleted.
   *
   * An administrator can be removed only when more than one administrator
   * exists and the target is not the currently authenticated user (users
   * cannot remove themselves).
   *
   * @param admin The administrator to evaluate.
   * @returns `true` if the administrator can be deleted, otherwise `false`.
   */
  public canDeleteAdmin(admin: User): boolean {
    let result = false;
    const found = this.loginService.getCurrentUsername() === admin.userId;

    if (this.administrators().length > 1 && !found) {
      result = true;
    }

    return result;
  }
}
