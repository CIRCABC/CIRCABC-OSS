import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  input,
  linkedSignal,
  output,
  resource,
} from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import {
  Node as ModelNode,
  NodesService,
  SpaceService,
} from 'app/core/generated/circabc';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Standalone component that renders a browsable file/folder picker for a
 * CIRCABC node hierarchy.
 *
 * Given a starting node id, it loads that node and lists its children
 * (documents and folders) so the user can navigate the tree and select one
 * or more items. It supports two selection modes: a multi-selection mode for
 * picking several items, and a single-selection "target folder" mode used
 * when only one destination folder may be chosen. A loading spinner is shown
 * while the node content is being fetched.
 *
 * Key collaborators:
 * - {@link NodesService} to fetch individual node metadata.
 * - {@link SpaceService} to fetch the paged children of a node.
 */
@Component({
  selector: 'cbc-file-picker',
  templateUrl: './file-picker.component.html',
  styleUrl: './file-picker.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [SpinnerComponent, DatePipe, TranslocoModule],
})
export class FilePickerComponent {
  /** Service used to retrieve the paged children of a node. */
  private readonly spaceService = inject(SpaceService);
  /** Service used to retrieve metadata for a single node. */
  private readonly nodesService = inject(NodesService);

  /**
   * Input: the id of the node whose contents should be displayed and
   * navigated from. When set, the component loads this node and its children
   * on initialization.
   */
  readonly nodeId = input<string>();
  /**
   * Input (aliased as `selection`): the initial list of selected node ids.
   * Used to seed the internal {@link selection} signal.
   */
  // eslint-disable-next-line @angular-eslint/no-input-rename
  selectionInput = input<string[]>(undefined, { alias: 'selection' });
  /**
   * Writable signal holding the currently selected node ids. It is linked to
   * {@link selectionInput} so it reflects the initial input value while
   * remaining locally mutable as the user changes the selection.
   */
  selection = linkedSignal(this.selectionInput);
  /**
   * Output: emits the updated list of selected node ids whenever the
   * selection changes.
   */
  readonly selectionChange = output<string[]>();
  /**
   * Input: when `true`, the picker operates in single-folder selection mode,
   * allowing only one folder to be selected at a time (used for choosing a
   * destination folder). Defaults to `false`.
   */
  readonly targetFolderMode = input(false);
  /**
   * Input: when `true`, folders may be selected in addition to being
   * navigable. Defaults to `false`.
   */
  readonly canSelectFolders = input(false);
  /** Output: emitted once the picker has finished loading node content. */
  readonly pickerLoaded = output();

  /** The id of the node currently being displayed and navigated from. Seeded
   * from {@link nodeId} but locally writable so folder navigation (and the
   * "back" link) can change it without touching the input. */
  private readonly currentNodeId = linkedSignal(this.nodeId);

  /**
   * Resource that loads the current node together with its paged children
   * whenever {@link currentNodeId} changes. Idle while no node id is set.
   */
  private readonly pickerResource = resource({
    params: () => this.currentNodeId() || undefined,
    loader: async ({ params: nodeId }) => {
      try {
        const currentNode = await this.nodesService.getNodeAsync({
          id: nodeId,
        });
        const content = await this.spaceService.getChildrenAsync({
          id: nodeId,
          language: 'en',
          guest: false,
          limit: -1,
          page: -1,
          order: 'modified_DESC',
          folderOnly: this.targetFolderMode(),
          fileOnly: false,
          skipExpiredItems: true,
        });
        return { currentNode, content };
      } catch (error) {
        console.error(error);
        return { currentNode: undefined, content: undefined };
      }
    },
  });

  /** The paged list of child nodes currently displayed. */
  public readonly content = computed(
    () => this.pickerResource.value()?.content
  );
  /** The node whose children are currently being displayed. */
  public readonly currentNode = computed(
    () => this.pickerResource.value()?.currentNode
  );
  /** Whether content is currently being loaded from the backend. */
  public readonly loading = this.pickerResource.isLoading;

  constructor() {
    // Notify the parent once the picker has finished (re)loading its
    // content, mirroring the previous `pickerLoaded.emit()` call performed
    // at the end of the imperative fetch.
    effect(() => {
      if (this.pickerResource.status() === 'resolved') {
        this.pickerLoaded.emit();
      }
    });
  }

  /**
   * Navigates the picker to the given node, causing {@link pickerResource}
   * to reload {@link currentNode} and {@link content}. Does nothing if
   * `nodeId` is undefined.
   *
   * @param nodeId - The id of the node whose content should be loaded.
   */
  public getContent(nodeId: string | undefined) {
    if (nodeId !== undefined) {
      this.currentNodeId.set(nodeId);
    }
  }

  /**
   * Determines whether the current node is the library root, i.e. the input
   * {@link nodeId} matches the loaded {@link currentNode} and it is named
   * `Library`.
   *
   * @returns `true` if the current node is the library root, otherwise `false`.
   */
  public isLibrary(): boolean {
    const nodeId = this.nodeId();
    const currentNode = this.currentNode();
    if (nodeId && currentNode) {
      return nodeId === currentNode.id && currentNode.name === 'Library';
    }

    return false;
  }

  /**
   * Determines whether the given node represents a folder based on its type.
   *
   * @param node - The node to inspect.
   * @returns `true` if the node's type indicates a folder, otherwise `false`.
   */
  public isFolder(node: ModelNode): boolean {
    if (node.type) {
      return node.type.includes('folder');
    }
    return false;
  }

  /**
   * Indicates whether the currently loaded content has no child nodes.
   *
   * @returns `true` if the loaded content contains zero items, otherwise `false`.
   */
  public isEmpty(): boolean {
    return this.content()?.total === 0;
  }

  /**
   * Checks whether the node with the given id is part of the current selection.
   *
   * @param id - The node id to check, or `undefined`.
   * @returns `true` if the id is defined and currently selected, otherwise `false`.
   */
  public isSelected(id: string | undefined): boolean {
    if (id === undefined) {
      return false;
    }
    const selection = this.selection() ?? [];
    return selection.includes(id);
  }

  /**
   * Toggles the selection state of the node with the given id, updating the
   * {@link selection} signal and emitting {@link selectionChange}. In
   * {@link targetFolderMode} the selection is reset first so only a single
   * item can be selected. Does nothing if `id` is undefined.
   *
   * @param id - The node id to select or deselect.
   */
  public toggleSelect(id: string | undefined) {
    if (id === undefined) {
      return;
    }
    // if in targetFolderMode, it means that we only select one folder, not many
    let selection = this.selection() ?? [];
    if (this.targetFolderMode()) {
      selection = [];
    } else {
      selection = [...selection];
    }

    const idx = selection.indexOf(id);
    if (idx === -1) {
      selection.push(id);
    } else {
      selection.splice(idx, 1);
    }

    this.selection.set(selection);
    this.selectionChange.emit(selection);
  }

  /**
   * Returns the last-modified timestamp of the given node from its properties.
   *
   * @param node - The node whose modified date should be read.
   * @returns The modified timestamp string, or `null` if unavailable.
   */
  public getModified(node: ModelNode): string | null {
    if (node.properties) {
      return node.properties.modified;
    }
    return null;
  }
}
