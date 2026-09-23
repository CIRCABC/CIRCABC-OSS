import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  linkedSignal,
  OnDestroy,
  OnInit,
  output,
  resource,
  viewChild,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { ActionEmitterResult } from 'app/action-result';
import { ActionService } from 'app/action-result/action.service';
import { ActionResult } from 'app/action-result/action-result';
import { ActionType } from 'app/action-result/action-type';
import {
  type InterestGroup,
  Node as ModelNode,
  NodesService,
} from 'app/core/generated/circabc';
import { TreeNode } from 'app/shared/treeview/tree-node';
import { TreeViewComponent } from 'app/shared/treeview/tree-view.component';
import { Subscription } from 'rxjs';

/**
 * Renders a collapsible sidebar tree view of the document library folder
 * hierarchy for an interest group.
 *
 * The component wraps a {@link TreeViewComponent} rooted at the group's
 * library node and keeps it in sync with the currently viewed node:
 * - It computes and exposes the path to the current node so the tree can
 *   expand and highlight the active folder.
 * - It reacts to library structural changes (space create/delete) emitted by
 *   the {@link ActionService} and reloads the tree accordingly.
 * - Selecting a node navigates the router to that folder.
 * - Its visibility is persisted in `localStorage` under the `showTreeView`
 *   key so the open/closed state survives navigation and reloads.
 *
 * Key collaborators: {@link Router}/{@link ActivatedRoute} for navigation,
 * {@link NodesService} for resolving node paths, and {@link ActionService}
 * for reacting to library actions.
 */
@Component({
  selector: 'cbc-folder-tree-view',
  templateUrl: './folder-tree-view.component.html',
  styleUrl: './folder-tree-view.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TreeViewComponent, TranslocoModule],
})
export class FolderTreeViewComponent implements OnInit, OnDestroy {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly nodesService = inject(NodesService);
  private readonly actionService = inject(ActionService);

  /**
   * The interest group whose library folder tree is displayed. Its
   * `libraryId` is used as the root node of the tree.
   */
  readonly group = input<InterestGroup>();
  /**
   * The node currently being viewed in the library. Changes trigger a reload
   * of the path so the tree highlights the corresponding folder.
   */
  readonly currentNode = input.required<ModelNode>();

  /**
   * Initial visibility of the tree view, exposed to templates under the
   * alias `shown`. Seeds the {@link shown} writable signal.
   */
  // eslint-disable-next-line @angular-eslint/no-input-rename
  readonly shownInput = input(false, { alias: 'shown' });
  /**
   * Writable signal tracking whether the tree view is currently shown,
   * linked to {@link shownInput} and reconciled with the persisted
   * `localStorage` value on init.
   */
  readonly shown = linkedSignal(this.shownInput);
  /** Emits the new visibility state whenever the tree view is closed. */
  readonly shownChange = output<boolean>();

  /** Root tree node representing the group's library. */
  public root!: TreeNode;

  /**
   * Loads the path from the library root down to the current node whenever
   * {@link currentNode}'s id changes. Idle while the current node has no id.
   */
  private readonly pathResource = resource({
    params: () => this.currentNode().id || undefined,
    loader: ({ params: id }) => this.nodesService.getPathAsync({ id }),
    defaultValue: [],
  });
  /** Path of nodes from the library root down to the current node. */
  public readonly path = this.pathResource.value;
  /**
   * Id of the node that should be searched for and highlighted in the tree.
   * Defaults to the current node's id whenever the path reloads for a new
   * node, but remains user-writable so direct tree navigation can override
   * it (see {@link navigate}).
   */
  public readonly searchedNodeId = linkedSignal({
    source: () => this.currentNode().id,
    computation: (id) => id ?? '',
  });
  /** Subscription to {@link ActionService.actionFinished$} used to reload the tree. */
  private actionFinishedSubscription$!: Subscription;
  /** Reference to the underlying tree view child component. */
  readonly treeViewComponent = viewChild.required(TreeViewComponent);

  /**
   * Angular lifecycle hook. Subscribes to library action events and performs
   * initial setup of the tree root and visibility.
   */
  ngOnInit() {
    this.subscribe();
    this.init();
  }

  /**
   * Subscribes to {@link ActionService.actionFinished$} and reloads the tree
   * view whenever a space is successfully created or deleted.
   */
  private subscribe() {
    this.actionFinishedSubscription$ =
      this.actionService.actionFinished$.subscribe(
        async (action: ActionEmitterResult) => {
          if (
            (action.type === ActionType.CREATE_SPACE ||
              action.type === ActionType.DELETE_SPACE) &&
            action.result === ActionResult.SUCCEED
          ) {
            await this.treeViewComponent().reload();
          }
        }
      );
  }

  /**
   * Angular lifecycle hook. Cleans up the action subscription when the
   * component is destroyed.
   */
  ngOnDestroy(): void {
    this.unsubscribe();
  }

  /** Unsubscribes from the action-finished subscription if it exists. */
  private unsubscribe() {
    if (this.actionFinishedSubscription$) {
      this.actionFinishedSubscription$.unsubscribe();
    }
  }

  /**
   * Initializes the tree view: restores the persisted visibility state and,
   * when the group exposes a `libraryId`, builds the root tree node.
   */
  private init() {
    this.shown.set(this.shouldShow());
    const group = this.group();
    if (group?.libraryId) {
      this.root = new TreeNode('Library', group.libraryId);
    }
  }

  /**
   * Reads the persisted visibility preference from `localStorage`.
   *
   * @returns `true` if the tree view should be shown, otherwise `false`.
   */
  private shouldShow(): boolean {
    return localStorage.getItem('showTreeView') === 'true';
  }

  /**
   * Handles selection of a tree node by navigating to the corresponding
   * folder.
   *
   * @param node The tree node selected by the user.
   */
  public propagateNavigation(node: TreeNode) {
    const id: string = node.nodeId;
    this.navigate(id);
  }

  /**
   * Navigates the router to the folder identified by `id`, resetting to the
   * first page of results.
   *
   * @param id The id of the folder node to navigate to.
   */
  private navigate(id: string) {
    this.searchedNodeId.set(id);

    this.router.navigate(['..', id], {
      relativeTo: this.route,
      queryParams: { p: 1 },
    });
  }

  /**
   * Closes the tree view, persists the hidden state to `localStorage`, and
   * emits the change through {@link shownChange}.
   */
  public close() {
    this.shown.set(false);
    const shown = this.shown();
    localStorage.setItem('showTreeView', `${shown}`);
    this.shownChange.emit(shown);
  }

  /**
   * Determines which node the tree view should highlight, preferring the
   * explicitly searched node and falling back to the last node in the path.
   *
   * @returns The id of the node to highlight, or `undefined` if none is
   * available.
   */
  public getSearchedNodeId() {
    if (this.searchedNodeId()) {
      return this.searchedNodeId();
    }
    const path = this.path();
    if (path && path.length > 0) {
      return path.at(-1)?.id;
    }
    return undefined;
  }
}
