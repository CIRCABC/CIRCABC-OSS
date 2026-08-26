import {
  Component,
  ChangeDetectionStrategy,
  input,
  output,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslocoModule } from '@jsverse/transloco';
import { TooltipModule } from 'primeng/tooltip';

@Component({
  selector: 'cbc-help-action-buttons',
  templateUrl: './help-action-buttons.component.html',
  styleUrls: ['./help-action-buttons.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, TranslocoModule, TooltipModule],
})
export class HelpActionButtonsComponent {
  readonly entityType = input.required<'section' | 'subsection' | 'article'>();
  readonly showAdd = input<boolean>(true);
  readonly showEdit = input<boolean>(true);
  readonly showDelete = input<boolean>(true);
  readonly disabled = input<boolean>(false);

  readonly addClicked = output<void>();
  readonly editClicked = output<void>();
  readonly deleteClicked = output<void>();

  protected onAddClick(): void {
    this.addClicked.emit();
  }

  protected onEditClick(): void {
    this.editClicked.emit();
  }

  protected onDeleteClick(): void {
    this.deleteClicked.emit();
  }
}
