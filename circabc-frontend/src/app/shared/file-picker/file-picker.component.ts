import { Component, Input, OnInit, output, input } from '@angular/core';

import { DatePipe } from '@angular/common';
import { TranslocoModule } from '@jsverse/transloco';
import {
  Node as ModelNode,
  NodesService,
  PagedNodes,
  SpaceService,
} from 'app/core/generated/circabc';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-file-picker',
  templateUrl: './file-picker.component.html',
  styleUrl: './file-picker.component.scss',
  preserveWhitespaces: true,
  imports: [SpinnerComponent, DatePipe, TranslocoModule],
})
export class FilePickerComponent implements OnInit {
  readonly nodeId = input<string>();
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  selection!: string[];
  readonly selectionChange = output<string[]>();
  readonly targetFolderMode = input(false);
  readonly canSelectFolders = input(false);
  readonly pickerLoaded = output();

  public content!: PagedNodes;
  public currentNode!: ModelNode;
  public loading = false;

  constructor(
    private spaceService: SpaceService,
    private nodesService: NodesService
  ) {}

  async ngOnInit() {
    const nodeId = this.nodeId();
    if (nodeId) {
      this.currentNode = await firstValueFrom(
        this.nodesService.getNode(nodeId)
      );
      await this.getContent(nodeId);
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
          this.targetFolderMode(),
          false
        )
      );
      this.loading = false;
      this.pickerLoaded.emit();
    }
  }

  public isLibrary(): boolean {
    const nodeId = this.nodeId();
    if (nodeId && this.currentNode) {
      return (
        nodeId === this.currentNode.id && this.currentNode.name === 'Library'
      );
    }

    return false;
  }

  public isFolder(node: ModelNode): boolean {
    if (node.type) {
      return node.type.indexOf('folder') !== -1;
    }
    return false;
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
    if (this.selection === undefined || this.targetFolderMode()) {
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
    }
    return null;
  }
}
