import {
  ChangeDetectionStrategy,
  Component,
  HostListener,
  inject,
  input,
  OnChanges,
  OnDestroy,
  OnInit,
  output,
  signal,
} from '@angular/core';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import { Node, NodesService, User } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { SaveAsService } from 'app/core/save-as.service';
import { SelectableNode } from 'app/core/ui-model/index';
import { ClipboardService } from 'app/group/library/clipboard/clipboard.service';
import { BulkDownloadPipe } from 'app/group/library/pipes/bulk-download.pipe';
import { ConfirmDialogComponent } from 'app/shared/confirm-dialog/confirm-dialog.component';
import { firstValueFrom, Subscription } from 'rxjs';

/**
 * Library clipboard side panel component.
 *
 * Renders a slide-in sidebar that lists the nodes (documents/folders) the user
 * has placed on the library clipboard and offers copy, move (cut), link,
 * remove and bulk-download operations against the folder the user is currently
 * standing in. It reacts to clipboard changes broadcast through
 * {@link ClipboardService} (items added/removed on other endpoints of the
 * communication channel) and persists the clipboard contents in
 * `sessionStorage`, keyed per interest group.
 *
 * Key collaborators:
 * - {@link ClipboardService}: observable channel for add/remove events.
 * - {@link NodesService}: backend paste/link operations.
 * - {@link PermissionEvaluatorService}: paste/delete permission checks.
 * - {@link SaveAsService} and {@link BulkDownloadPipe}: bulk download support.
 * - {@link MatDialog} / {@link ConfirmDialogComponent}: move-notification prompt.
 */
@Component({
  selector: 'cbc-clipboard',
  templateUrl: './clipboard.component.html',
  styleUrl: './clipboard.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslocoModule, MatDialogModule],
})
export class ClipboardComponent implements OnInit, OnChanges, OnDestroy {
  private readonly clipboardService = inject(ClipboardService);
  private readonly nodesService = inject(NodesService);
  private readonly permEvalService = inject(PermissionEvaluatorService);
  private readonly route = inject(ActivatedRoute);
  private readonly loginService = inject(LoginService);
  private readonly bulkDownloadPipe = inject(BulkDownloadPipe);
  private readonly saveAsService = inject(SaveAsService);
  private readonly dialog = inject(MatDialog);

  public readonly opened = input(false);
  /** The folder/node the user is currently viewing; paste targets this node. */
  public readonly currentStandingNode = input.required<Node>();
  // current Library folder/subfolder contents
  // current Library folder/subfolder contents
  /** Contents (selectable nodes) of the current library folder/subfolder. */
  public readonly contents = input.required<SelectableNode[]>();
  /** Whether the current standing node is a folder (paste is only allowed into folders). */
  public readonly currentStandingNodeIsFolder = input.required<boolean>();
  /** Emitted when the sidebar requests to be closed. */
  public readonly closeEmitter = output<void>();
  /** Emitted after a clipboard action completes, carrying its success result. */
  public readonly actionFinished = output<ActionEmitterResult>();
  /** Emitted whenever the number of items on the clipboard changes. */
  public readonly itemsAmount = output<number>();
  // clipboard contents
  /** Nodes currently held on the clipboard. */
  public readonly nodes = signal<Node[]>([]);
  /** Interest group id, derived from the active route, used as sessionStorage key suffix. */
  private igId!: string;
  /** Currently authenticated user, used for node ownership checks. */
  private user!: User;
  /** Subscription to {@link ClipboardService.itemsAdded$}. */
  private itemsAddedSubscription$!: Subscription;
  /** Subscription to {@link ClipboardService.itemsRemoved$}. */
  private itemsRemovedSubscription$!: Subscription;
  /** Whether to notify on move; set from the confirmation dialog. */
  private notify = true;
  /** True while an asynchronous clipboard operation is in progress. */
  public readonly processing = signal(false);
  /** Whether the clipboard sidebar is currently visible. */
  public visible = false;

  /**
   * Angular lifecycle hook. Subscribes to clipboard add/remove events.
   */
  ngOnInit(): void {
    this.subscribe();
  }

  /**
   * Subscribes to the {@link ClipboardService} channels so the local `nodes`
   * list stays in sync with items added or removed on other endpoints of the
   * communication channel (for example a delete operation elsewhere).
   */
  private subscribe() {
    // handler to be called when an item is added on the other endpoint of the comm channel
    this.itemsAddedSubscription$ = this.clipboardService.itemsAdded$.subscribe(
      (node: Node) => {
        if (!this.nodes().some((arrayNode) => arrayNode.id === node.id)) {
          this.nodes.set([...this.nodes(), node]);
          sessionStorage.setItem(
            `cbc-clipboard${this.igId}`,
            JSON.stringify(this.nodes())
          );
          this.itemsAmount.emit(this.nodes().length);
        }
      }
    );
    // handler to be called when an item is removed on the other endpoint of the comm channel (ex. delete operation)
    this.itemsRemovedSubscription$ =
      this.clipboardService.itemsRemoved$.subscribe((node: Node) => {
        if (this.nodes().some((arrayNode) => arrayNode.id === node.id)) {
          this.removeItem(node);
        }
      });
  }

  /**
   * Angular lifecycle hook. Re-reads the interest group id from the route,
   * restores the clipboard contents from `sessionStorage` when the local list
   * is empty, resolves the current user and emits the current item count.
   */
  public ngOnChanges() {
    this.route.params.subscribe((params) => this.getIGId(params));

    if (this.nodes().length === 0) {
      const json = sessionStorage.getItem(`cbc-clipboard${this.igId}`);

      if (json === null) {
        this.nodes.set([]);
      } else {
        this.nodes.set(JSON.parse(json) as Node[]);
      }
    }

    this.user ??= this.loginService.getUser();

    this.itemsAmount.emit(this.nodes().length);

    this.processing.set(false);
  }

  /**
   * Angular lifecycle hook. Unsubscribes from clipboard event streams.
   */
  public ngOnDestroy(): void {
    this.unsubscribe();
  }

  /**
   * Tears down the clipboard add/remove subscriptions.
   */
  private unsubscribe() {
    this.itemsAddedSubscription$.unsubscribe();
    this.itemsRemovedSubscription$.unsubscribe();
  }

  /**
   * Stores the interest group id from the resolved route params.
   *
   * @param params Route parameters; the `id` entry is used as the group id.
   */
  private getIGId(params: { [key: string]: string }) {
    this.igId = params.id;
  }

  // check:
  // 1. if the current currentStandingNode corresponds to a folder
  // 2. if the current user has permission to paste according to the desired operation
  // 3. if the current user has permission to remove the node from its parent when moved
  /**
   * Determines whether the given node may be pasted into the current standing
   * node for the requested action.
   *
   * Checks that the standing node is a folder, that the user has paste
   * permission on it and, for a move, that the user may delete the source node.
   *
   * @param node The node to be pasted.
   * @param action The paste action, e.g. `'Move'`.
   * @returns `true` if the action is permitted, otherwise `false`.
   */
  public isAuthorized(node: Node, action: string) {
    if (!this.currentStandingNodeIsFolder()) {
      // if it is not a folder, cannot paste
      return false;
    }

    const currentStandingNode = this.currentStandingNode();
    if (
      currentStandingNode === undefined ||
      !this.hasPasteLibraryPermission(currentStandingNode)
    ) {
      // current user does not have permission to paste
      return false;
    }

    if (action === 'Move' && !this.hasDeleteLibraryPermission(node)) {
      // current user does not have permission to delete
      return false;
    }

    return true;
  }

  /**
   * Checks whether the user is allowed to paste into the given target node.
   *
   * @param node The prospective paste target folder.
   * @returns `true` if the user is a library admin, has full edit rights or can
   * manage own-or-higher content and owns the node.
   */
  private hasPasteLibraryPermission(node: Node): boolean {
    return (
      this.permEvalService.isLibAdmin(node) ||
      this.permEvalService.isLibFullEdit(node) ||
      (this.permEvalService.isLibManageOwnOrHigher(node) &&
        this.isNodeOwner(node))
    );
  }

  /**
   * Checks whether the user is allowed to delete the given node (required when
   * moving it away from its parent).
   *
   * @param node The node to be deleted/moved.
   * @returns `true` if the user is a library admin, or has full-edit/manage
   * rights and owns the node.
   */
  private hasDeleteLibraryPermission(node: Node): boolean {
    return (
      this.permEvalService.isLibAdmin(node) ||
      (this.permEvalService.isLibFullEdit(node) && this.isNodeOwner(node)) ||
      (this.permEvalService.isLibManageOwnOrHigher(node) &&
        this.isNodeOwner(node))
    );
  }

  /**
   * Checks whether the current user owns the given node.
   *
   * @param node The node to check.
   * @returns `true` if the node has owner properties matching the current user.
   */
  private isNodeOwner(node: Node): boolean {
    return (
      node.properties !== undefined &&
      node.properties.owner === this.user.userId
    );
  }

  /**
   * Checks whether every node on the clipboard is authorized for the action.
   *
   * @param action The paste action, e.g. `'Move'`.
   * @returns `true` only if all clipboard nodes pass {@link isAuthorized}.
   */
  public allAuthorized(action: string) {
    return this.nodes().reduce(
      (result, node) => result && this.isAuthorized(node, action),
      true
    );
  }

  // for a particular item

  /**
   * Removes a single node from the clipboard, updates `sessionStorage` and
   * emits the new item count.
   *
   * @param node The node to remove.
   */
  public removeItem(node: Node) {
    this.processing.set(true);
    const index: number = this.nodes().indexOf(node, 0);
    const updated = [...this.nodes()];
    updated.splice(index, 1);
    this.nodes.set(updated);
    sessionStorage.setItem(
      `cbc-clipboard${this.igId}`,
      JSON.stringify(this.nodes())
    );
    this.itemsAmount.emit(this.nodes().length);
    this.processing.set(false);
  }

  /**
   * Copies a single node into the current standing folder (paste as copy) and
   * emits a success result.
   *
   * @param node The node to copy-paste.
   * @returns A promise that resolves once the paste completes.
   */
  public async copyPasteItem(node: Node) {
    this.processing.set(true);
    const currentStandingNode = this.currentStandingNode();
    if (node.id !== undefined && currentStandingNode.id !== undefined) {
      const nodeIds: string[] = [];
      nodeIds.push(node.id);
      await this.nodesService.postPasteAsync({
        id: currentStandingNode.id,
        nodeIds,
      });
      this.actionFinished.emit(
        this.buildSuccessResult(ActionType.CLIPBOARD_COPY_NODE)
      );
    }
    this.processing.set(false);
  }

  /**
   * Moves a single node into the current standing folder (paste as move) after
   * confirming the notification choice, then removes it from the clipboard and
   * emits a success result.
   *
   * @param node The node to cut-paste (move).
   * @returns A promise that resolves once the move completes.
   */
  public async cutPasteItem(node: Node) {
    await this.showConfirmationDialog();
    this.processing.set(true);
    const currentStandingNode = this.currentStandingNode();
    if (node.id !== undefined && currentStandingNode.id !== undefined) {
      const nodeIds: string[] = [];
      nodeIds.push(node.id);
      // move node and remove from clipboard if success

      await this.nodesService.putPasteAsync({
        id: currentStandingNode.id,
        nodeIds,
        notify: this.notify,
      });
      this.removeItem(node);
      this.actionFinished.emit(
        this.buildSuccessResult(ActionType.CLIPBOARD_MOVE_NODE)
      );
    }
    this.processing.set(false);
  }

  /**
   * Creates a link to a single node in the current standing folder, removes the
   * node from the clipboard and emits a success result.
   *
   * @param node The node to link-paste.
   * @returns A promise that resolves once the link is created.
   */
  public async linkPasteItem(node: Node) {
    this.processing.set(true);
    const currentStandingNode = this.currentStandingNode();
    if (node.id !== undefined && currentStandingNode.id !== undefined) {
      const nodeIds: string[] = [];
      nodeIds.push(node.id);
      await this.nodesService.postLinkAsync({
        id: currentStandingNode.id,
        nodeIds,
      });
      this.removeItem(node);
      this.actionFinished.emit(
        this.buildSuccessResult(ActionType.CLIPBOARD_LINK_NODE)
      );
    }
    this.processing.set(false);
  }

  // for all items in the clipboard

  /**
   * Clears the entire clipboard, updates `sessionStorage` and emits the new
   * (zero) item count.
   */
  public removeAll() {
    this.processing.set(true);
    this.nodes.set([]);
    sessionStorage.setItem(
      `cbc-clipboard${this.igId}`,
      JSON.stringify(this.nodes())
    );
    this.itemsAmount.emit(this.nodes().length);
    this.processing.set(false);
  }

  /**
   * Copies all clipboard nodes into the current standing folder and emits a
   * success result.
   *
   * @returns A promise that resolves once the paste completes.
   */
  public async copyPasteAll() {
    this.processing.set(true);
    const currentStandingNode = this.currentStandingNode();
    if (currentStandingNode.id !== undefined) {
      const nodeIds = this.nodes().map((localNode) => localNode.id);
      await this.nodesService.postPasteAsync({
        id: currentStandingNode.id,
        nodeIds: nodeIds as string[],
      });
      this.actionFinished.emit(
        this.buildSuccessResult(ActionType.CLIPBOARD_COPY_NODE)
      );
    }
    this.processing.set(false);
  }

  /**
   * Moves all clipboard nodes into the current standing folder after confirming
   * the notification choice, then clears the clipboard and emits a success
   * result.
   *
   * @returns A promise that resolves once the move completes.
   */
  public async cutPasteAll() {
    await this.showConfirmationDialog();
    this.processing.set(true);
    const currentStandingNode = this.currentStandingNode();
    if (currentStandingNode.id !== undefined) {
      const nodeIds = this.nodes().map((localNode) => localNode.id);
      await this.nodesService.putPasteAsync({
        id: currentStandingNode.id,
        nodeIds: nodeIds as string[],
        notify: this.notify,
      });
      this.removeAll();
      this.actionFinished.emit(
        this.buildSuccessResult(ActionType.CLIPBOARD_MOVE_NODE)
      );
    }
    this.processing.set(false);
  }

  /**
   * Creates links to all clipboard nodes in the current standing folder, clears
   * the clipboard and emits a success result.
   *
   * @returns A promise that resolves once the links are created.
   */
  public async linkPasteAll() {
    this.processing.set(true);
    const currentStandingNode = this.currentStandingNode();
    if (currentStandingNode.id !== undefined) {
      const nodeIds = this.nodes().map((localNode) => localNode.id);
      await this.nodesService.postLinkAsync({
        id: currentStandingNode.id,
        nodeIds: nodeIds as string[],
      });
      this.removeAll();
      this.actionFinished.emit(
        this.buildSuccessResult(ActionType.CLIPBOARD_LINK_NODE)
      );
    }
    this.processing.set(false);
  }

  /**
   * Triggers a bulk download of all clipboard nodes as a `bulk.zip` archive via
   * {@link SaveAsService}.
   *
   * @returns Always `false` to prevent default anchor/click navigation.
   */
  public bulkDownload() {
    this.processing.set(true);
    const url = this.bulkDownloadPipe.transform(this.getNodeIds());
    const name = 'bulk.zip';
    this.saveAsService.saveUrlAs(url, name);
    this.processing.set(false);
    return false;
  }

  /**
   * Builds a success {@link ActionEmitterResult} for the given action type.
   *
   * @param type The clipboard action type that succeeded.
   * @returns The populated success result.
   */
  private buildSuccessResult(type: ActionType): ActionEmitterResult {
    const result: ActionEmitterResult = {};
    result.result = ActionResult.SUCCEED;
    result.type = type;
    return result;
  }

  /**
   * Returns the ids of all nodes currently on the clipboard.
   *
   * @returns An array of node ids (entries may be `undefined`).
   */
  public getNodeIds() {
    return this.nodes().map((node: Node) => node.id);
  }

  // close clipboard side bar when clicked outside of it

  /**
   * Prevents a click inside the sidebar from bubbling up and triggering the
   * outside-click close handler.
   *
   * @param event The click event originating inside the sidebar.
   */
  public clickedInside(event: Event) {
    event.preventDefault();
    // stop propagation on lower layers
    event.stopPropagation();
  }

  /**
   * Document-level click handler that closes the sidebar when a click occurs
   * outside of it while it is visible.
   *
   * @param event The document click event.
   */
  @HostListener('document:click', ['$event'])
  public clickedOutside(event: MouseEvent) {
    if (this.visible) {
      this.close(event);
    }
  }

  // close the sidebar
  /**
   * Hides the sidebar and emits {@link closeEmitter}.
   *
   * @param _event Optional originating mouse event (unused).
   */
  public close(_event?: MouseEvent) {
    this.visible = false;
    this.closeEmitter.emit();
  }

  /**
   * Opens the set-notification confirmation dialog and stores the user's choice
   * in {@link notify} for subsequent move operations.
   *
   * @returns A promise that resolves once the dialog is closed.
   */
  public async showConfirmationDialog() {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      ariaLabel: 'Dialog',
      data: {
        title: 'label.title.set-notification',
        layoutStyle: 'setNotification',
      },
    });
    // listen to response
    this.notify = await firstValueFrom(dialogRef.afterClosed());
  }
}
