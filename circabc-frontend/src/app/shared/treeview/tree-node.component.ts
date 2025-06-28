import {
  Component,
  Input,
  OnDestroy,
  OnInit,
  forwardRef,
  output,
  input,
} from '@angular/core';

import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

import {
  ForumService,
  Node as ModelNode,
  SpaceService,
} from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { TreeNode } from 'app/shared/treeview/tree-node';
import { TooltipModule } from 'primeng/tooltip';
import { Subscription, firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-tree-node',
  templateUrl: './tree-node.component.html',
  styleUrl: './tree-node.component.scss',
  preserveWhitespaces: true,
  imports: [
    SpinnerComponent,
    TooltipModule,
    forwardRef(() => TreeNodeComponent),
    TranslocoModule,
  ],
})
export class TreeNodeComponent implements OnInit, OnDestroy {
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input()
  node!: TreeNode;
  readonly rootId = input.required<string>();
  readonly service = input<'library' | 'newsgroups'>('library');
  readonly displayedPath = input<ModelNode[]>([]);
  readonly searchedNodeId = input<string>();
  readonly disabled = input(false);
  readonly showSelector = input(true);
  readonly showExpander = input(true);
  readonly flagNewDays = input(-1);
  readonly selectedNodeEmitter = output<TreeNode>();
  readonly clickedNodeEmitter = output<TreeNode>();

  needsExpansion = false;
  public loading = false;

  private langaugeChangeSubscription$!: Subscription;

  constructor(
    private spaceService: SpaceService,
    private translateService: TranslocoService,
    private forumService: ForumService
  ) {}

  public async reload() {
    const rootId = this.rootId();
    if (this.displayedPath().length > 0 && this.pathContainsId()) {
      await this.loadChildren();
      this.node.expanded = true;
    } else if (rootId && this.node && rootId === this.node.nodeId) {
      await this.loadChildren();
    }
  }

  async ngOnInit() {
    await this.reload();
    this.langaugeChangeSubscription$ =
      this.translateService.langChanges$.subscribe(async (_event: string) => {
        await this.reload();
      });
  }

  ngOnDestroy(): void {
    if (this.langaugeChangeSubscription$ !== undefined) {
      this.langaugeChangeSubscription$.unsubscribe();
    }
  }

  private pathContainsId(): boolean {
    for (const node of this.displayedPath()) {
      if (node.id === this.node.nodeId) {
        return true;
      }
    }

    return false;
  }

  selectNode(node: TreeNode) {
    this.selectedNodeEmitter.emit(node);
  }

  async clickNode(node: TreeNode) {
    if (this.showSelector()) {
      this.selectedNodeEmitter.emit(node);
    } else {
      this.clickedNodeEmitter.emit(node);
    }
  }

  // checks if this node is the one to mark selected
  markSelected(): boolean {
    const searchedNodeId = this.searchedNodeId();
    return searchedNodeId !== undefined && searchedNodeId === this.node.nodeId;
  }

  // method called when expanding a node, to load its children on demand
  async loadChildren() {
    this.loading = true;
    this.node.children = [];
    const service = this.service();
    if (service === 'library') {
      await this.listSpaces();
    } else if (service === 'newsgroups') {
      await this.listForums();
    }
    this.loading = false;
  }

  private async listForums() {
    const subForums = await firstValueFrom(
      this.forumService.getSubforums(this.node.nodeId, 'title', 'ASC')
    );
    for (const forum of subForums) {
      let text: string | undefined =
        forum.title === undefined
          ? forum.name
          : new I18nPipe(this.translateService).transform(forum.title);
      if (text === undefined || text.trim().length === 0) {
        text = forum.name;
      }
      const childNode = new TreeNode(
        text === undefined ? (forum.id as string) : text,
        forum.id as string
      );
      const desc = new I18nPipe(this.translateService).transform(
        forum.description
      );
      if (desc) {
        childNode.description = desc;
      }
      childNode.hasSubFolders = forum.hasSubFolders
        ? forum.hasSubFolders
        : false;
      if (forum.properties?.created) {
        childNode.created = new Date(forum.properties.created);
      }
      this.node.children.push(childNode);
    }

    this.node.children.sort((a, b) => {
      return a.name.localeCompare(b.name);
    });
  }

  private async listSpaces() {
    const subspaces = await firstValueFrom(
      this.spaceService.getSubspaces(this.node.nodeId, '', 'title', 'ASC')
    );
    for (const space of subspaces) {
      let text: string | undefined =
        space.title === undefined
          ? space.name
          : new I18nPipe(this.translateService).transform(space.title);
      if (text === undefined || text.trim().length === 0) {
        text = space.name;
      }

      const childNode = new TreeNode(
        text === undefined ? (space.id as string) : text,
        space.id as string
      );

      const desc = new I18nPipe(this.translateService).transform(
        space.description
      );
      if (desc) {
        childNode.description = desc;
      }

      childNode.hasSubFolders = space.hasSubFolders
        ? space.hasSubFolders
        : false;
      if (space.properties?.created) {
        childNode.created = new Date(space.properties.created);
      }
      this.node.children.push(childNode);
    }

    this.node.children.sort((a, b) => {
      return a.name.localeCompare(b.name);
    });
  }

  // checks if this node can be expanded by checking if it has children
  canBeExpanded() {
    return (
      (this.node.children !== undefined && this.node.children.length > 0) ||
      this.node.hasSubFolders
    );
  }

  async toggleExpand() {
    await this.loadChildren();
    this.node.toggleExpand();
  }

  public isNew(): boolean {
    if (this.flagNewDays() > 0) {
      const comparableDate = new Date();
      const day = comparableDate.getDate() - this.flagNewDays();
      comparableDate.setDate(day);
      if (this.node?.created) {
        const nodeDate = new Date(this.node.created);
        return nodeDate >= comparableDate;
      }
    }
    return false;
  }
}
