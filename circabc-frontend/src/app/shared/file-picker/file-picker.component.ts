import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

import {
  Node as ModelNode,
  NodesService,
  PagedNodes,
  SpaceService,
} from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-file-picker',
  templateUrl: './file-picker.component.html',
  styleUrls: ['./file-picker.component.scss'],
  preserveWhitespaces: true,
})
export class FilePickerComponent implements OnInit {
  @Input()
  nodeId: string | undefined;
  @Input()
  selection!: string[];
  @Output()
  readonly selectionChange = new EventEmitter<string[]>();
  @Input()
  targetFolderMode = false;
  @Input()
  canSelectFolders = false;
  @Output()
  readonly pickerLoaded = new EventEmitter();

  public content!: PagedNodes;
  public currentNode!: ModelNode;
  public loading = false;

  constructor(
    private spaceService: SpaceService,
    private nodesService: NodesService
  ) {}

  async ngOnInit() {
    if (this.nodeId) {
      this.currentNode = await firstValueFrom(
        this.nodesService.getNode(this.nodeId)
      );
      await this.getContent(this.nodeId);
    }
  }

  public async getContent(nodeId: string | undefined) {
    if (nodeId !== undefined) {
      this.loading = true;

      this.currentNode = await firstValueFrom(
        this.nodesService.getNode(nodeId)
      );

      this.content = await firstValueFrom(
        this.spaceService.getChildren(
          nodeId,
          'en',
          false,
          -1,
          -1,
          'modified_DESC',
          this.targetFolderMode,
          false
        )
      );
      this.loading = false;
      this.pickerLoaded.emit();
    }
  }

  public isLibrary(): boolean {
    if (this.nodeId && this.currentNode) {
      return (
        this.nodeId === this.currentNode.id &&
        this.currentNode.name === 'Library'
      );
    }

    return false;
  }

  public isFolder(node: ModelNode): boolean {
    if (node.type) {
      return node.type.indexOf('folder') !== -1;
    } else {
      return false;
    }
  }

  public isEmpty(): boolean {
    return this.content && this.content.total === 0;
  }

  public isSelected(id: string | undefined): boolean {
    if (id === undefined) {
      return false;
    }
    if (this.selection === undefined) {
      this.selection = [];
    }
    return this.selection.indexOf(id) !== -1;
  }

  public toggleSelect(id: string | undefined) {
    if (id === undefined) {
      return;
    }
    // if in targetFolderMode, it means that we only select one folder, not many
    if (this.selection === undefined || this.targetFolderMode) {
      this.selection = [];
    }

    if (this.selection.indexOf(id) === -1) {
      this.selection.push(id);
    } else {
      this.selection.splice(this.selection.indexOf(id), 1);
    }

    this.selectionChange.emit(this.selection);
  }

  public getModified(node: ModelNode): string | null {
    if (node.properties) {
      return node.properties.modified;
    } else {
      return null;
    }
  }
}
