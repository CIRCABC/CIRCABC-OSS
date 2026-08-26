import {
  Component,
  Input,
  OnChanges,
  SimpleChanges,
  output,
} from '@angular/core';

import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  ContentService,
  Node as ModelNode,
  SpaceService,
} from 'app/core/generated/circabc';
import { ClipboardService } from 'app/group/library/clipboard/clipboard.service';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-delete-multiple',
  templateUrl: './delete-multiple.component.html',
  styleUrl: './delete-multiple.component.scss',
  preserveWhitespaces: true,
  imports: [
    ReactiveFormsModule,
    MatSlideToggleModule,
    SpinnerComponent,
    DataCyDirective,
    TranslocoModule,
  ],
})
export class DeleteMultipleComponent implements OnChanges {
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input()
  nodes!: ModelNode[];
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal = false;
  public readonly modalHide = output<ActionEmitterResult>();

  public deleting = false;
  public progressValue = 0;
  public progressMax = 0;
  private notify = true;
  notifyFormGroup = this.notifyFormBuilder.group({
    notify: true,
  });
  constructor(
    private notifyFormBuilder: FormBuilder,
    private spaceService: SpaceService,
    private contentService: ContentService,
    private clipboardService: ClipboardService
  ) {}

  ngOnChanges(changes: SimpleChanges) {
    this.progressValue = 0;
    this.deleting = false;
    const chng = changes.nodes;
    if (chng) {
      if (chng.currentValue) {
        this.progressMax = (chng.currentValue as ModelNode[]).length;
      } else {
        this.progressMax = 0;
      }
    }
  }

  public cancelWizard(_action: string): void {
    this.showModal = false;

    const result: ActionEmitterResult = {};
    result.result = ActionResult.CANCELED;
    result.type = ActionType.DELETE_ALL;

    this.modalHide.emit(result);
  }

  public async deleteAll() {
    if (!this.notifyFormGroup.controls.notify.value) {
      this.notify = false;
    }
    this.deleting = true;
    for (const node of this.nodes) {
      if (node.id) {
        if (node.type && node.type.indexOf('folder') !== -1) {
          await firstValueFrom(
            this.spaceService.deleteSpace(node.id, this.notify)
          );
        } else if (node.type && node.type.indexOf('folder') === -1) {
          await firstValueFrom(
            this.contentService.deleteContent(node.id, this.notify)
          );
        }
        this.clipboardService.removeItem(node);
      }
      this.progressValue += 1;
    }

    this.showModal = false;
    const result: ActionEmitterResult = {};
    result.result = ActionResult.SUCCEED;
    result.type = ActionType.DELETE_ALL;
    this.modalHide.emit(result);
  }
}
