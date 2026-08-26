import { Component, Input, forwardRef, input } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { type StructureNode } from 'app/group/admin/summary/structure-tree/structure-node';

@Component({
  selector: 'cbc-structure-tree',
  templateUrl: './structure-tree.component.html',
  styleUrl: './structure-tree.component.scss',
  preserveWhitespaces: true,
  imports: [forwardRef(() => StructureTreeComponent), TranslocoModule],
})
export class StructureTreeComponent {
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input()
  tree!: StructureNode;
  readonly isRoot = input(true);
}
