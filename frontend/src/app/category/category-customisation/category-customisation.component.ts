import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { ActionService } from 'app/action-result/action.service';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result/index';
import {
  CategoryService,
  Node as ModelNode,
  NodesService,
} from 'app/core/generated/circabc';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { OldDownloadPipe } from 'app/shared/pipes/old.download.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';
import { AddCategoryLogoComponent } from './add-category-logo/add-category-logo.component';

/**
 * Standalone Angular component that renders the customisation view for a
 * category, allowing administrators to manage the category's logo images.
 *
 * The component displays the collection of logos available for a category,
 * highlights the one currently assigned, and provides actions to upload a new
 * logo (via {@link AddCategoryLogoComponent}), select an existing logo as the
 * active one, and delete logos. The target category is resolved from the `id`
 * route parameter.
 *
 * Key collaborators:
 * - {@link NodesService} to load the category node.
 * - {@link CategoryService} to list, select and delete category logos.
 * - {@link ActionService} to broadcast completed actions to the rest of the app.
 */
@Component({
  selector: 'cbc-category-customisation',
  templateUrl: './category-customisation.component.html',
  styleUrl: './category-customisation.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    InlineDeleteComponent,
    AddCategoryLogoComponent,
    OldDownloadPipe,
    SecurePipe,
    TranslocoModule,
  ],
})
export class CategoryCustomisationComponent implements OnInit {
  /** Provides access to the current route, used to read the category `id` param. */
  private readonly route = inject(ActivatedRoute);
  /** Generated API client used to list, select and delete category logos. */
  private readonly categoryService = inject(CategoryService);
  /** Generated API client used to load the category node metadata. */
  private readonly nodesService = inject(NodesService);
  /** Service used to broadcast completed actions across the application. */
  private readonly actionService = inject(ActionService);

  /** Logo nodes currently available for the category. */
  public readonly logos = signal<ModelNode[]>([]);
  /** Whether an asynchronous load operation is in progress. */
  public loading = false;
  /** Controls the visibility of the "upload logo" modal in the template. */
  public readonly showUploadModal = signal(false);
  /** Identifier of the category being customised, taken from the route. */
  public readonly categoryId = signal<string>('');
  /** The loaded category node, including its properties (e.g. `logoRef`). */
  public readonly category = signal<ModelNode | undefined>(undefined);

  /**
   * Angular lifecycle hook. Subscribes to route parameter changes and, for
   * each emitted `id`, loads the corresponding category and its logos.
   */
  ngOnInit() {
    this.route.params.subscribe(async (params) => {
      await this.loadCategory(params.id);
      await this.listLogos(params.id);
    });
  }

  /**
   * Loads the category node for the given identifier and stores it on
   * {@link category}. Errors are caught and logged; they do not propagate.
   *
   * @param id - The identifier of the category to load.
   * @returns A promise that resolves once the load attempt has completed.
   */
  private async loadCategory(id: string) {
    this.categoryId.set(id);
    this.loading = true;
    try {
      this.category.set(await this.nodesService.getNodeAsync({ id }));
    } catch (error) {
      console.error(error);
      console.error('impossible to get the category');
    }

    this.loading = false;
  }

  /**
   * Retrieves the list of logos associated with the given category and stores
   * them on {@link logos}. Errors are caught and logged; they do not propagate.
   *
   * @param id - The identifier of the category whose logos should be listed.
   * @returns A promise that resolves once the logos have been fetched.
   */
  private async listLogos(id: string) {
    this.logos.set([]);
    this.categoryId.set(id);
    this.loading = true;
    try {
      this.logos.set(
        await this.categoryService.getCategoryLogoByCategoryIdAsync({ id })
      );
    } catch (error) {
      console.error(error);
      console.error('impossible to get the logos');
    }
    this.loading = false;
  }

  /**
   * Handles the completion of a child action (e.g. a logo upload). On success
   * the logo list is refreshed, and the upload modal is always closed.
   *
   * @param result - The outcome emitted by the child action component.
   * @returns A promise that resolves once any refresh has completed.
   */
  public async refresh(result: ActionEmitterResult) {
    if (result.result === ActionResult.SUCCEED) {
      await this.listLogos(this.categoryId());
    }
    this.showUploadModal.set(false);
  }

  /**
   * Determines whether the given logo is the one currently assigned to the
   * category, by matching it against the category's `logoRef` property.
   *
   * @param id - The identifier of the logo to test, if any.
   * @returns `true` if the logo is referenced by the category, otherwise `false`.
   */
  public isSelected(id: string | undefined): boolean {
    const logoRef = this.category()?.properties?.logoRef;
    if (id && logoRef) {
      if (logoRef.includes(id)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Sets the given logo as the active logo for the current category. On success
   * the corresponding action is broadcast via {@link ActionService} and the
   * category is reloaded to reflect the change. Errors are caught and logged.
   *
   * @param id - The identifier of the logo to select, if any.
   * @returns A promise that resolves once the selection attempt has completed.
   */
  public async select(id: string | undefined) {
    if (id && this.categoryId()) {
      try {
        const result: ActionEmitterResult = {
          type: ActionType.ADD_CATEGORY_LOGO,
          node: { id: this.categoryId() },
          result: ActionResult.SUCCEED,
        };
        await this.categoryService.selectCategoryLogoByLogoIdAsync({
          id: this.categoryId(),
          logoId: id,
        });
        this.actionService.propagateActionFinished(result);
        await this.loadCategory(this.categoryId());
      } catch (error) {
        console.error(error);
        console.error('impossible to select the image');
      }
    }
  }

  /**
   * Deletes the given logo from the current category and updates {@link logos}
   * with the remaining logos returned by the API. Errors are caught and logged.
   *
   * @param id - The identifier of the logo to delete, if any.
   * @returns A promise that resolves once the deletion attempt has completed.
   */
  public async delete(id: string | undefined) {
    if (this.categoryId() && id) {
      try {
        this.logos.set(
          await this.categoryService.deleteCategoryLogoByLogoIdAsync({
            id: this.categoryId(),
            logoId: id,
          })
        );
      } catch (error) {
        console.error(error);
        console.error('impossible to select the image');
      }
    }
  }
}
