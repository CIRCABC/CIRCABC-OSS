import {
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
} from '@angular/core';

import { TranslocoService } from '@ngneat/transloco';

import {
  ForumService,
  Node as ModelNode,
  SpaceService,
} from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { TreeNode } from 'app/shared/treeview/tree-node';
import { firstValueFrom, Subscription } from 'rxjs';

@Component({
  selector: 'cbc-tree-node',
  templateUrl: './tree-node.component.html',
  styleUrls: ['./tree-node.component.scss'],
  preserveWhitespaces: true,
})
export class TreeNodeComponent implements OnInit, OnDestroy {
  @Input()
  node!: TreeNode;
  @Input()
  rootId!: string;
  @Input()
  service: 'library' | 'newsgroups' = 'library';
  @Input()
  displayedPath: ModelNode[] = [];
  @Input()
  searchedNodeId: string | undefined;
  @Input()
  disabled = false;
  @Input()
  showSelector = true;
  @Input()
  showExpander = true;
  @Input()
  flagNewDays = -1;
  @Output()
  readonly selectedNodeEmitter = new EventEmitter<TreeNode>();
  @Output()
  readonly clickedNodeEmitter = new EventEmitter<TreeNode>();

  needsExpansion = false;
  public loading = false;

  private langaugeChangeSubscription$!: Subscription;

  constructor(
    private spaceService: SpaceService,
    private translateService: TranslocoService,
    private forumService: ForumService
  ) {}

  public async reload() {
    if (this.displayedPath.length > 0 && this.pathContainsId()) {
      await this.loadChildren();
      this.node.expanded = true;
    } else if (this.rootId && this.node && this.rootId === this.node.nodeId) {
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
    for (const node of this.displayedPath) {
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
    if (!this.showSelector) {
      this.clickedNodeEmitter.emit(node);
    } else {
      this.selectedNodeEmitter.emit(node);
    }
  }

  // checks if this node is the one to mark selected
  markSelected(): boolean {
    return (
      this.searchedNodeId !== undefined &&
      this.searchedNodeId === this.node.nodeId
    );
  }

  // method called when expanding a node, to load its children on demand
  async loadChildren() {
    this.loading = true;
    this.node.children = [];
    if (this.service === 'library') {
      await this.listSpaces();
    } else if (this.service === 'newsgroups') {
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
      if (forum.properties && forum.properties.created) {
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
      if (space.properties && space.properties.created) {
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
    if (this.flagNewDays > 0) {
      const comparableDate = new Date();
      const day = comparableDate.getDate() - this.flagNewDays;
      comparableDate.setDate(day);
      if (this.node && this.node.created) {
        const nodeDate = new Date(this.node.created);
        return nodeDate >= comparableDate;
      }
    }
    return false;
  }
}
