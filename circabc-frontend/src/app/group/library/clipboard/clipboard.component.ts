import {
  Component,
  EventEmitter,
  HostListener,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { firstValueFrom, Subscription } from 'rxjs';

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

@Component({
  selector: 'cbc-clipboard',
  templateUrl: './clipboard.component.html',
  styleUrls: ['./clipboard.component.scss'],
  preserveWhitespaces: true,
})
export class ClipboardComponent implements OnInit, OnChanges, OnDestroy {
  @Input()
  public opened = false;
  @Input()
  public currentStandingNode!: Node;
  // current Library folder/subfolder contents
  // current Library folder/subfolder contents
  @Input()
  public contents!: SelectableNode[];
  @Input()
  public currentStandingNodeIsFolder!: boolean;
  @Output()
  public readonly closeEmitter: EventEmitter<MouseEvent | void> = new EventEmitter<MouseEvent | void>();
  @Output()
  public readonly actionFinished: EventEmitter<ActionEmitterResult> = new EventEmitter<ActionEmitterResult>();
  @Output()
  public readonly itemsAmount: EventEmitter<number> = new EventEmitter<number>();
  // clipboard contents
  public nodes: Node[] = [];
  private igId!: string;
  private user!: User;
  private itemsAddedSubscription$!: Subscription;
  private itemsRemovedSubscription$!: Subscription;

  public processing = false;
  public visible = false;

  public constructor(
    private clipboardService: ClipboardService,
    private nodesService: NodesService,
    private permEvalService: PermissionEvaluatorService,
    private route: ActivatedRoute,
    private loginService: LoginService,
    private bulkDownloadPipe: BulkDownloadPipe,
    private saveAsService: SaveAsService
  ) {}
  ngOnInit(): void {
    this.subscribe();
  }

  private subscribe() {
    // handler to be called when an item is added on the other endpoint of the comm channel
    this.itemsAddedSubscription$ = this.clipboardService.itemsAdded$.subscribe(
      (node: Node) => {
        if (
          this.nodes.find((arrayNode) => arrayNode.id === node.id) === undefined
        ) {
          this.nodes.push(node);
          sessionStorage.setItem(
            `cbc-clipboard${this.igId}`,
            JSON.stringify(this.nodes)
          );
          this.itemsAmount.emit(this.nodes.length);
        }
      }
    );
    // handler to be called when an item is removed on the other endpoint of the comm channel (ex. delete operation)
    this.itemsRemovedSubscription$ =
      this.clipboardService.itemsRemoved$.subscribe((node: Node) => {
        if (
          this.nodes.find((arrayNode) => arrayNode.id === node.id) !== undefined
        ) {
          this.removeItem(node);
        }
      });
  }

  public ngOnChanges() {
    this.route.params.subscribe((params) => this.getIGId(params));

    if (this.nodes === undefined || this.nodes.length === 0) {
      const json = sessionStorage.getItem(`cbc-clipboard${this.igId}`);

      if (json !== null) {
        this.nodes = JSON.parse(json);
      } else {
        this.nodes = [];
      }
    }

    if (this.user === undefined) {
      this.user = this.loginService.getUser();
    }

    this.itemsAmount.emit(this.nodes.length);

    this.processing = false;
  }

  public ngOnDestroy(): void {
    this.unsubscribe();
  }

  private unsubscribe() {
    this.itemsAddedSubscription$.unsubscribe();
    this.itemsRemovedSubscription$.unsubscribe();
  }

  private getIGId(params: { [key: string]: string }) {
    this.igId = params.id;
  }

  // check:
  // 1. if the current currentStandingNode corresponds to a folder
  // 2. if the current user has permission to paste according to the desired operation
  // 3. if the current user has permission to remove the node from its parent when moved
  public isAuthorized(node: Node, action: string) {
    if (!this.currentStandingNodeIsFolder) {
      // if it is not a folder, cannot paste
      return false;
    }

    if (
      this.currentStandingNode === undefined ||
      !this.hasPasteLibraryPermission(this.currentStandingNode)
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

  private hasPasteLibraryPermission(node: Node): boolean {
    return (
      this.permEvalService.isLibAdmin(node) ||
      this.permEvalService.isLibFullEdit(node) ||
      (this.permEvalService.isLibManageOwnOrHigher(node) &&
        this.isNodeOwner(node))
    );
  }

  private hasDeleteLibraryPermission(node: Node): boolean {
    return (
      this.permEvalService.isLibAdmin(node) ||
      (this.permEvalService.isLibFullEdit(node) && this.isNodeOwner(node)) ||
      (this.permEvalService.isLibManageOwnOrHigher(node) &&
        this.isNodeOwner(node))
    );
  }

  private isNodeOwner(node: Node): boolean {
    return (
      node.properties !== undefined &&
      node.properties.owner === this.user.userId
    );
  }

  public allAuthorized(action: string) {
    return this.nodes.reduce(
      (result, node) => result && this.isAuthorized(node, action),
      true
    );
  }

  // for a particular item

  public removeItem(node: Node) {
    this.processing = true;
    const index: number = this.nodes.indexOf(node, 0);
    this.nodes.splice(index, 1);
    sessionStorage.setItem(
      `cbc-clipboard${this.igId}`,
      JSON.stringify(this.nodes)
    );
    this.itemsAmount.emit(this.nodes.length);
    this.processing = false;
  }

  public async copyPasteItem(node: Node) {
    this.processing = true;
    if (node.id !== undefined && this.currentStandingNode.id !== undefined) {
      const nodeIds: string[] = [];
      nodeIds.push(node.id);
      await firstValueFrom(
        this.nodesService.postPaste(this.currentStandingNode.id, nodeIds)
      );
      this.actionFinished.emit(
        this.buildSuccessResult(ActionType.CLIPBOARD_COPY_NODE)
      );
    }
    this.processing = false;
  }

  public async cutPasteItem(node: Node) {
    this.processing = true;
    if (node.id !== undefined && this.currentStandingNode.id !== undefined) {
      const nodeIds: string[] = [];
      nodeIds.push(node.id);
      // move node and remove from clipboard if success

      await firstValueFrom(
        this.nodesService.putPaste(this.currentStandingNode.id, nodeIds)
      );
      this.removeItem(node);
      this.actionFinished.emit(
        this.buildSuccessResult(ActionType.CLIPBOARD_MOVE_NODE)
      );
    }
    this.processing = false;
  }

  public async linkPasteItem(node: Node) {
    this.processing = true;
    if (node.id !== undefined && this.currentStandingNode.id !== undefined) {
      const nodeIds: string[] = [];
      nodeIds.push(node.id);
      await firstValueFrom(
        this.nodesService.postLink(this.currentStandingNode.id, nodeIds)
      );
      this.removeItem(node);
      this.actionFinished.emit(
        this.buildSuccessResult(ActionType.CLIPBOARD_LINK_NODE)
      );
    }
    this.processing = false;
  }

  // for all items in the clipboard

  public removeAll() {
    this.processing = true;
    this.nodes = [];
    sessionStorage.setItem(
      `cbc-clipboard${this.igId}`,
      JSON.stringify(this.nodes)
    );
    this.itemsAmount.emit(this.nodes.length);
    this.processing = false;
  }

  public async copyPasteAll() {
    this.processing = true;
    if (this.currentStandingNode.id !== undefined) {
      const nodeIds = this.nodes.map((localNode) => localNode.id);
      await firstValueFrom(
        this.nodesService.postPaste(
          this.currentStandingNode.id,
          nodeIds as string[]
        )
      );
      this.actionFinished.emit(
        this.buildSuccessResult(ActionType.CLIPBOARD_COPY_NODE)
      );
    }
    this.processing = false;
  }

  public async cutPasteAll() {
    this.processing = true;
    if (this.currentStandingNode.id !== undefined) {
      const nodeIds = this.nodes.map((localNode) => localNode.id);
      await firstValueFrom(
        this.nodesService.putPaste(
          this.currentStandingNode.id,
          nodeIds as string[]
        )
      );
      this.removeAll();
      this.actionFinished.emit(
        this.buildSuccessResult(ActionType.CLIPBOARD_MOVE_NODE)
      );
    }
    this.processing = false;
  }

  public async linkPasteAll() {
    this.processing = true;
    if (this.currentStandingNode.id !== undefined) {
      const nodeIds = this.nodes.map((localNode) => localNode.id);
      await firstValueFrom(
        this.nodesService.postLink(
          this.currentStandingNode.id,
          nodeIds as string[]
        )
      );
      this.removeAll();
      this.actionFinished.emit(
        this.buildSuccessResult(ActionType.CLIPBOARD_LINK_NODE)
      );
    }
    this.processing = false;
  }

  public bulkDownload() {
    this.processing = true;
    const url = this.bulkDownloadPipe.transform(this.getNodeIds());
    const name = 'bulk.zip';
    this.saveAsService.saveUrlAs(url, name);
    this.processing = false;
    return false;
  }

  private buildSuccessResult(type: ActionType): ActionEmitterResult {
    const result: ActionEmitterResult = {};
    result.result = ActionResult.SUCCEED;
    result.type = type;
    return result;
  }

  public getNodeIds() {
    return this.nodes.map((node: Node) => node.id);
  }

  // close clipboard side bar when clicked outside of it

  public clickedInside(event: Event) {
    event.preventDefault();
    // stop propagation on lower layers
    event.stopPropagation();
  }

  @HostListener('document:click', ['$event'])
  public clickedOutside(event: MouseEvent) {
    if (this.visible) {
      this.close(event);
    }
  }

  // close the sidebar
  public close(event?: MouseEvent) {
    this.opened = false;
    this.visible = false;
    if (event) {
      this.closeEmitter.emit(event);
    } else {
      this.closeEmitter.emit();
    }
  }
}
