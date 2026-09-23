import {
  ChangeDetectionStrategy,
  Component,
  forwardRef,
  inject,
  input,
  OnDestroy,
  OnInit,
  output,
  signal,
} from '@angular/core';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ForumService,
  Node as ModelNode,
  SpaceService,
} from 'app/core/generated/circabc';
import { HtmlTooltipDirective } from 'app/shared/html-tooltip/html-tooltip.directive';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { I18nService } from 'app/shared/services/i18n.service';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { TreeNode } from 'app/shared/treeview/tree-node';
import { Subscription } from 'rxjs';

/**
 * Recursive tree view node component.
 *
 * Renders a single node of a hierarchical tree (a folder in the document
 * library or a forum in the newsgroups service) together with an optional
 * selector, an expander control and, when applicable, a "new" flag. Because
 * the component references itself in its `imports` (via `forwardRef`), it
 * recursively renders the children of the node it represents, producing a
 * fully expandable tree.
 *
 * Children are loaded lazily and on demand: when a node is expanded the
 * component fetches its sub-spaces (library) or sub-forums (newsgroups) from
 * the backend. It also reloads its children whenever the active UI language
 * changes so that translated titles stay in sync.
 *
 * Key collaborators:
 * - `SpaceService` / `ForumService` — fetch the child nodes for each service.
 * - `I18nService` — notifies the component of language changes.
 * - `I18nPipe` — resolves multilingual titles/descriptions to the active language.
 */
@Component({
  selector: 'cbc-tree-node',
  templateUrl: './tree-node.component.html',
  styleUrl: './tree-node.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    SpinnerComponent,
    MatTooltipModule,
    HtmlTooltipDirective,
    forwardRef(() => TreeNodeComponent),
    TranslocoModule,
  ],
})
export class TreeNodeComponent implements OnInit, OnDestroy {
  private readonly spaceService = inject(SpaceService);
  private readonly forumService = inject(ForumService);
  private readonly i18nService = inject(I18nService);
  private readonly i18nPipe = inject(I18nPipe);

  /** The tree node model rendered by this component. Required. */
  node = input.required<TreeNode>();
  /** Identifier of the root node of the tree; used to trigger the initial load. Required. */
  readonly rootId = input.required<string>();
  /**
   * Which backend service supplies the children of this node:
   * `'library'` loads sub-spaces, `'newsgroups'` loads sub-forums.
   * Defaults to `'library'`.
   */
  readonly service = input<'library' | 'newsgroups'>('library');
  /**
   * Path (list of ancestor nodes) that should be automatically expanded,
   * e.g. to reveal a deep-linked or searched node. Defaults to an empty array.
   */
  readonly displayedPath = input<ModelNode[]>([]);
  /** Identifier of the node that should be highlighted as selected, if any. */
  readonly searchedNodeId = input<string>();
  /** When `true`, the node is rendered in a disabled (non-interactive) state. Defaults to `false`. */
  readonly disabled = input(false);
  /** When `true`, the selection control is rendered for the node. Defaults to `true`. */
  readonly showSelector = input(true);
  /** When `true`, the expander control is rendered for the node. Defaults to `true`. */
  readonly showExpander = input(true);
  /**
   * Number of days a node is considered "new" after its creation date.
   * A value of `-1` (the default) disables the "new" flag.
   */
  readonly flagNewDays = input(-1);
  /** Emits the node when it is selected (used when {@link showSelector} is `true`). */
  readonly selectedNodeEmitter = output<TreeNode>();
  /** Emits the node when it is clicked (used when {@link showSelector} is `false`). */
  readonly clickedNodeEmitter = output<TreeNode>();

  /** Whether this node should be expanded automatically once children are known. */
  needsExpansion = false;
  /** Whether children are currently being fetched from the backend. */
  public readonly loading = signal(false);

  /** Subscription to language-change events; used to reload translated child titles. */
  private langaugeChangeSubscription$!: Subscription;

  // ...existing code...

  /**
   * Reloads the node's children when appropriate.
   *
   * Children are (re)loaded and the node is expanded when the node lies on the
   * {@link displayedPath}, or the children are loaded (without forcing
   * expansion) when this node is the tree root.
   *
   * @returns A promise that resolves once any required loading has completed.
   */
  public async reload() {
    const rootId = this.rootId();
    if (this.displayedPath().length > 0 && this.pathContainsId()) {
      await this.loadChildren();
      this.node().expanded = true;
    } else if (rootId && rootId === this.node().nodeId) {
      await this.loadChildren();
    }
  }

  /**
   * Angular lifecycle hook. Subscribes to language-change events so the node's
   * children are reloaded (and their translated titles refreshed) whenever the
   * active UI language changes.
   */
  ngOnInit() {
    this.langaugeChangeSubscription$ = this.i18nService
      .getLangChanges$()
      ?.subscribe(() => {
        this.reload();
      });
  }

  /**
   * Angular lifecycle hook. Unsubscribes from the language-change subscription
   * to avoid memory leaks when the component is destroyed.
   */
  ngOnDestroy(): void {
    if (this.langaugeChangeSubscription$ !== undefined) {
      this.langaugeChangeSubscription$.unsubscribe();
    }
  }

  /**
   * Determines whether this node's id appears in the configured
   * {@link displayedPath}.
   *
   * @returns `true` if the node lies on the displayed path, otherwise `false`.
   */
  private pathContainsId(): boolean {
    for (const node of this.displayedPath()) {
      if (node.id === this.node().nodeId) {
        return true;
      }
    }

    return false;
  }

  /**
   * Emits the given node through {@link selectedNodeEmitter}.
   *
   * @param node The tree node that was selected.
   */
  selectNode(node: TreeNode) {
    this.selectedNodeEmitter.emit(node);
  }

  /**
   * Handles a click on the node, emitting it through the appropriate output:
   * {@link selectedNodeEmitter} when {@link showSelector} is `true`, otherwise
   * {@link clickedNodeEmitter}.
   *
   * @param node The tree node that was clicked.
   * @returns A promise that resolves once the click has been handled.
   */
  async clickNode(node: TreeNode) {
    if (this.showSelector()) {
      this.selectedNodeEmitter.emit(node);
    } else {
      this.clickedNodeEmitter.emit(node);
    }
  }

  // checks if this node is the one to mark selected
  /**
   * Checks whether this node should be marked as selected, i.e. its id matches
   * the configured {@link searchedNodeId}.
   *
   * @returns `true` if this node is the searched/selected node, otherwise `false`.
   */
  markSelected(): boolean {
    const searchedNodeId = this.searchedNodeId();
    return (
      searchedNodeId !== undefined && searchedNodeId === this.node().nodeId
    );
  }

  // method called when expanding a node, to load its children on demand
  /**
   * Loads the node's children on demand from the backend, delegating to the
   * library or newsgroups loader depending on the configured {@link service}.
   * Toggles the {@link loading} flag around the fetch.
   *
   * @returns A promise that resolves once the children have been loaded.
   */
  async loadChildren() {
    this.loading.set(true);
    this.node().children = [];
    const service = this.service();
    if (service === 'library') {
      await this.listSpaces();
    } else if (service === 'newsgroups') {
      await this.listForums();
    }
    this.loading.set(false);
  }

  /**
   * Fetches the sub-forums of this node and populates its `children`.
   *
   * Forum titles are resolved to the active language (falling back to the
   * forum name when no translation is available) and duplicate forum ids are
   * de-duplicated. The resulting children are sorted alphabetically by name.
   *
   * @returns A promise that resolves once the child forums have been loaded.
   */
  private async listForums() {
    const subForums = await this.forumService.getSubforumsAsync({
      id: this.node().nodeId,
      sort: 'title',
      order: 'ASC',
    });
    // Use a Map to ensure unique forum IDs
    const childrenMap = new Map<string, TreeNode>();
    for (const forum of subForums) {
      if (childrenMap.has(forum.id as string)) continue;
      let text: string | undefined =
        forum.title === undefined
          ? forum.name
          : this.i18nPipe.transform(forum.title);
      if (text === undefined || text.trim().length === 0) {
        text = forum.name;
      }
      const childNode = new TreeNode(
        text ?? (forum.id as string),
        forum.id as string
      );
      const desc = this.i18nPipe.transform(forum.description);
      if (desc) {
        childNode.description = desc;
      }
      childNode.hasSubFolders = forum.hasSubFolders
        ? forum.hasSubFolders
        : false;
      if (forum.properties?.created) {
        childNode.created = new Date(forum.properties.created);
      }
      childrenMap.set(forum.id as string, childNode);
    }
    this.node().children = Array.from(childrenMap.values()).sort((a, b) =>
      a.name.localeCompare(b.name)
    );
  }

  /**
   * Fetches the sub-spaces (library folders) of this node and populates its
   * `children`.
   *
   * Space titles are resolved to the active language (falling back to the
   * space name when no translation is available) and duplicate space ids are
   * de-duplicated. The resulting children are sorted alphabetically by name.
   *
   * @returns A promise that resolves once the child spaces have been loaded.
   */
  private async listSpaces() {
    const subspaces = await this.spaceService.getSubspacesAsync({
      id: this.node().nodeId,
      language: '',
      sort: 'title',
      order: 'ASC',
      skipExpiredItems: true,
    });
    // Use a Map to ensure unique space IDs
    const childrenMap = new Map<string, TreeNode>();
    for (const space of subspaces) {
      if (childrenMap.has(space.id as string)) continue;
      let text: string | undefined =
        space.title === undefined
          ? space.name
          : this.i18nPipe.transform(space.title);
      if (text === undefined || text.trim().length === 0) {
        text = space.name;
      }

      const childNode = new TreeNode(
        text ?? (space.id as string),
        space.id as string
      );

      const desc = this.i18nPipe.transform(space.description);
      if (desc) {
        childNode.description = desc;
      }

      childNode.hasSubFolders = space.hasSubFolders
        ? space.hasSubFolders
        : false;
      if (space.properties?.created) {
        childNode.created = new Date(space.properties.created);
      }
      childrenMap.set(space.id as string, childNode);
    }

    this.node().children = Array.from(childrenMap.values()).sort((a, b) =>
      a.name.localeCompare(b.name)
    );
  }

  // checks if this node can be expanded by checking if it has children
  /**
   * Determines whether this node can be expanded, i.e. it already has loaded
   * children or is known to have sub-folders.
   *
   * @returns `true` if the node is expandable, otherwise `false`.
   */
  canBeExpanded() {
    return (
      (this.node().children !== undefined && this.node().children.length > 0) ||
      this.node().hasSubFolders
    );
  }

  /**
   * Loads the node's children (if not already loaded) and toggles its expanded
   * state.
   *
   * @returns A promise that resolves once children are loaded and the expanded
   * state has been toggled.
   */
  async toggleExpand() {
    await this.loadChildren();
    this.node().toggleExpand();
  }

  /**
   * Determines whether this node should be flagged as "new" based on its
   * creation date and the configured {@link flagNewDays} window.
   *
   * @returns `true` if the node was created within the last {@link flagNewDays}
   * days (and the flag is enabled), otherwise `false`.
   */
  public isNew(): boolean {
    if (this.flagNewDays() > 0) {
      const comparableDate = new Date();
      const day = comparableDate.getDate() - this.flagNewDays();
      comparableDate.setDate(day);
      const created = this.node().created;
      if (created) {
        const nodeDate = new Date(created);
        return nodeDate >= comparableDate;
      }
    }
    return false;
  }
}
