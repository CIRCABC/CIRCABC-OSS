import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  linkedSignal,
  OnDestroy,
} from '@angular/core';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { ActionService } from 'app/action-result/action.service';
import { Node as ModelNode, NodesService } from 'app/core/generated/circabc';
import { DownloadPipe } from 'app/shared/pipes/download.pipe';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';
import { Subscription } from 'rxjs';

/**
 * Displays the descriptor of a CIRCABC category: its logo (when available)
 * and a human-readable group description derived from the category node's
 * localized title or name.
 *
 * The category node is fetched reactively with a signal-based
 * {@link NodesService.getNodeResource} (`httpResource`) keyed off the
 * {@link categoryId} input: whenever the id changes the request re-runs
 * automatically. Category-related actions (metadata updates and logo changes)
 * broadcast by the {@link ActionService} trigger a `reload()` of the resource.
 *
 * @remarks This component is wired to the signal-based `getNodeResource`
 * factory of the generated `circabc` client (OpenAPI Generator, rx templates)
 * to drive the `httpResource` GET end-to-end. Auth headers are still added by
 * the global `authInterceptor`, since `httpResource` uses the DI-configured
 * `HttpClient`.
 */
@Component({
  selector: 'cbc-category-descriptor',
  templateUrl: './category-descriptor.component.html',
  styleUrl: './category-descriptor.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DownloadPipe, SecurePipe],
})
export class CategoryDescriptorComponent implements OnDestroy {
  /** Pipe used to resolve the localized value of the category title. */
  private readonly i18nPipe = inject(I18nPipe);
  /** API client used to fetch the category node by its identifier. */
  private readonly nodesService = inject(NodesService);
  /** Service exposing the stream of finished actions to react to. */
  private readonly actionService = inject(ActionService);

  /**
   * Required input holding the identifier of the category node to display.
   * Changing this value re-runs the underlying `httpResource` request.
   */
  readonly categoryId = input.required<string>();

  /**
   * Reactive `httpResource` fetching the category node. Stays idle while the
   * id is not yet available and re-runs whenever {@link categoryId} changes.
   */
  private readonly categoryResource = this.nodesService.getNodeResource(() => {
    const id = this.categoryId();
    return id ? { id } : undefined;
  });

  /**
   * The category node currently rendered by the component.
   *
   * Sourced from {@link categoryResource} via `linkedSignal` so it tracks the
   * resource value but remains writable (used by unit tests to exercise the
   * display helpers with synthetic nodes).
   */
  public readonly category = linkedSignal<ModelNode | undefined>(() =>
    this.categoryResource.value()
  );

  /**
   * Subscription to {@link ActionService.actionFinished$}; retained so it can
   * be released in {@link ngOnDestroy}.
   */
  private readonly actionFinishedSubscription$: Subscription =
    this.actionService.actionFinished$.subscribe(
      (actionResult: ActionEmitterResult) => {
        if (
          (actionResult.type === ActionType.UPDATE_CATEGORY ||
            actionResult.type === ActionType.ADD_CATEGORY_LOGO) &&
          actionResult.result === ActionResult.SUCCEED &&
          actionResult.node?.id
        ) {
          this.categoryResource.reload();
        }
      }
    );

  /**
   * Angular lifecycle hook. Unsubscribes from the action stream to prevent
   * memory leaks.
   */
  ngOnDestroy(): void {
    this.actionFinishedSubscription$.unsubscribe();
  }

  /**
   * Indicates whether the loaded category node has a non-empty logo reference.
   *
   * @returns `true` when the category defines a logo reference, otherwise
   * `false`.
   */
  public hasModelNodeLogo(): boolean {
    const category = this.category();
    if (category?.properties) {
      return !(
        category.properties.logoRef === undefined ||
        category.properties.logoRef === ''
      );
    }
    return false;
  }

  /**
   * Extracts the last path segment of the category's logo reference, which is
   * the file name used to build the logo download URL.
   *
   * @returns The logo file name, or an empty string when no logo reference is
   * available.
   */
  public getLogoRef(): string {
    const category = this.category();
    if (category?.properties) {
      const parts = category.properties.logoRef.split('/');
      return parts.at(-1) ?? '';
    }
    return '';
  }

  /**
   * Computes the display description for the category group, preferring the
   * localized title and falling back to the category name when no localized
   * title is available.
   *
   * @returns The resolved category group description, or an empty string when
   * neither a title nor a name is available.
   */
  public getCategoryGroupDescription(): string {
    let result = '';
    const category = this.category();
    if (category?.title !== undefined) {
      result = this.i18nPipe.transform(category.title);
      if (result === '') {
        result = this.i18nPipe.transform(category.title);
      }
    }

    if (category && result === '' && category.name) {
      result = category.name;
    }

    return result;
  }
}
