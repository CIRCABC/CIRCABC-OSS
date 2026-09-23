import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { ActionEmitterResult } from 'app/action-result';
import { ActionService } from 'app/action-result/action.service';
import { ActionResult } from 'app/action-result/action-result';
import { ActionType } from 'app/action-result/action-type';
import {
  Category,
  CategoryService,
  NodesService,
} from 'app/core/generated/circabc';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Standalone component that renders the details editing form for a single
 * CIRCABC category.
 *
 * The component reads the category id from the active route, loads the
 * corresponding node from the backend and displays a reactive form with the
 * category name (required) and a multilingual title. Users can edit these
 * values and either persist them via {@link CategoryDetailsComponent.update}
 * or discard local changes via {@link CategoryDetailsComponent.cancel}.
 *
 * Key collaborators:
 * - {@link NodesService} to fetch the category node.
 * - {@link CategoryService} to persist category updates.
 * - {@link ActionService} to broadcast that a category update finished.
 * - {@link ActivatedRoute} to obtain the category id from the URL.
 */
@Component({
  selector: 'cbc-category-details',
  templateUrl: './category-details.component.html',
  styleUrl: './category-details.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    ControlMessageComponent,
    MultilingualInputComponent,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class CategoryDetailsComponent implements OnInit {
  /** Active route used to read the category id parameter. */
  private readonly route = inject(ActivatedRoute);
  /** API client used to persist category changes. */
  private readonly categoryService = inject(CategoryService);
  /** API client used to load the category node by id. */
  private readonly nodesService = inject(NodesService);
  /** Factory used to build the reactive category form. */
  private readonly fb = inject(FormBuilder);
  /** Service used to broadcast completion of category actions. */
  private readonly actionService = inject(ActionService);

  /** Display name of the parent header, read from session storage. */
  public header!: string | null;
  /** Whether a category load operation is currently in progress. */
  public readonly loading = signal(false);
  /** The category currently being displayed and edited. */
  public category!: Category;
  /** Reactive form holding the editable category name and title. */
  public categoryForm!: FormGroup;

  /**
   * Angular lifecycle hook. Initializes the reactive form, subscribes to
   * route parameter changes to load the requested category, and reads the
   * current header name from session storage when available.
   */
  ngOnInit() {
    this.categoryForm = this.fb.group({
      name: ['', [Validators.required]],
      title: [''],
    });

    this.route.params.subscribe(async (params) => {
      await this.initCategory(params.id);
    });

    const headerSession = sessionStorage.getItem('currentHeader');
    if (headerSession) {
      const parsed = JSON.parse(headerSession) as { name: string };
      this.header = parsed.name;
    }
  }

  /**
   * Loads the category node with the given id and patches the form with its
   * values. Sets {@link CategoryDetailsComponent.loading} while the request
   * is in flight. Errors are caught and logged rather than propagated.
   *
   * @param id - Identifier of the category node to load.
   * @returns A promise that resolves once loading completes.
   */
  private async initCategory(id: string) {
    this.loading.set(true);
    try {
      const categoryNode = await this.nodesService.getNodeAsync({ id });
      if (categoryNode.name && categoryNode.title) {
        this.category = {
          id: categoryNode.id,
          name: categoryNode.name,
          title: categoryNode.title,
        };
      }

      if (this.categoryForm) {
        this.categoryForm.patchValue(this.category);
      }
    } catch (error) {
      console.error(error);
      console.error('impossible to get the category');
    }
    this.loading.set(false);
  }

  /**
   * Convenience accessor for the `name` form control, used by the template
   * to display validation state and messages.
   *
   * @returns The `name` control of the category form.
   */
  get nameControl(): AbstractControl {
    return this.categoryForm.controls.name;
  }

  /**
   * Discards local edits by reloading the category from the backend,
   * effectively resetting the form to its persisted state.
   *
   * @returns A promise that resolves once the category has been reloaded.
   */
  public async cancel() {
    if (this.category.id) {
      await this.initCategory(this.category.id);
    }
  }

  /**
   * Persists the current form values for the category via
   * {@link CategoryService.putCategory}, notifies listeners through
   * {@link ActionService} and reloads the category on success. Errors are
   * caught and logged rather than propagated.
   *
   * @returns A promise that resolves once the update flow completes.
   */
  public async update() {
    if (this.category?.id) {
      try {
        const result: ActionEmitterResult = {
          type: ActionType.UPDATE_CATEGORY,
          node: { id: this.category.id },
          result: ActionResult.SUCCEED,
        };
        await this.categoryService.putCategoryAsync({
          id: this.category.id,
          category: this.categoryForm.value,
        });
        this.actionService.propagateActionFinished(result);
        await this.initCategory(this.category.id);
      } catch (error) {
        console.error(error);
      }
    }
  }
}
