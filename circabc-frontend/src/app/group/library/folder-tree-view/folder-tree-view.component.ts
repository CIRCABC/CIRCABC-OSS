import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ActionEmitterResult } from 'app/action-result';
import { ActionResult } from 'app/action-result/action-result';
import { ActionType } from 'app/action-result/action-type';
import { ActionService } from 'app/action-result/action.service';
import {
  InterestGroup,
  Node as ModelNode,
  NodesService,
} from 'app/core/generated/circabc';
import { TreeNode } from 'app/shared/treeview/tree-node';
import { TreeViewComponent } from 'app/shared/treeview/tree-view.component';
import { firstValueFrom, Subscription } from 'rxjs';

@Component({
  selector: 'cbc-folder-tree-view',
  templateUrl: './folder-tree-view.component.html',
  styleUrls: ['./folder-tree-view.component.scss'],
  preserveWhitespaces: true,
})
export class FolderTreeViewComponent implements OnInit, OnChanges, OnDestroy {
  @Input()
  group!: InterestGroup;
  @Input()
  currentNode!: ModelNode;

  @Input()
  shown = false;
  @Output()
  readonly shownChange = new EventEmitter();

  public root!: TreeNode;
  public searchedNodeId!: string;
  public path: ModelNode[] = [];
  private actionFinishedSubscription$!: Subscription;
  @ViewChild(TreeViewComponent)
  treeViewComponent!: TreeViewComponent;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private nodesService: NodesService,
    private actionService: ActionService
  ) {}

  async ngOnInit() {
    this.subscribe();
    await this.init();
  }

  private subscribe() {
    this.actionFinishedSubscription$ =
      this.actionService.actionFinished$.subscribe(
        async (action: ActionEmitterResult) => {
          if (
            (action.type === ActionType.CREATE_SPACE ||
              action.type === ActionType.DELETE_SPACE) &&
            action.result === ActionResult.SUCCEED
          ) {
            await this.treeViewComponent.reload();
          }
        }
      );
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  private unsubscribe() {
    if (this.actionFinishedSubscription$) {
      this.actionFinishedSubscription$.unsubscribe();
    }
  }

  private async init() {
    this.shown = this.shouldShow();
    if (this.currentNode && this.currentNode.id) {
      this.path = await firstValueFrom(
        this.nodesService.getPath(this.currentNode.id)
      );
    }
    if (this.group && this.group.libraryId) {
      this.root = new TreeNode('Library', this.group.libraryId);
    }
  }

  private shouldShow(): boolean {
    return localStorage.getItem('showTreeView') === 'true';
  }

  async ngOnChanges(changes: SimpleChanges) {
    await this.onChange(changes);
  }

  private async onChange(changes: SimpleChanges) {
    if (changes.currentNode && changes.currentNode.currentValue.id) {
      await this.reload(changes.currentNode.currentValue.id);
    }
  }

  private async reload(id: string) {
    this.path = await firstValueFrom(this.nodesService.getPath(id));
    this.searchedNodeId = id;
  }

  public async propagateNavigation(node: TreeNode) {
    const id: string = node.nodeId;
    this.navigate(id);
  }

  private navigate(id: string) {
    this.searchedNodeId = id;
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    this.router.navigate(['..', id], {
      relativeTo: this.route,
      queryParams: { p: 1 },
    });
  }

  public close() {
    this.shown = false;
    localStorage.setItem('showTreeView', `${this.shown}`);
    this.shownChange.emit(this.shown);
  }

  public getSearchedNodeId() {
    if (this.searchedNodeId) {
      return this.searchedNodeId;
    } else if (this.path && this.path.length > 0) {
      return this.path[this.path.length - 1].id;
    } else {
      return undefined;
    }
  }
}
