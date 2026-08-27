import {
  Component,
  output,
  input,
  model,
  signal,
  ChangeDetectionStrategy,
} from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { ModalComponent } from 'app/shared/modal/modal.component';

@Component({
  selector: 'cbc-help-delete-confirmation-modal',
  templateUrl: './help-delete-confirmation-modal.component.html',
  styleUrls: ['./help-delete-confirmation-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ModalComponent, TranslocoModule],
})
export class HelpDeleteConfirmationModalComponent {
  // Inputs and outputs using signals API
  readonly visible = model.required<boolean>();
  readonly entityType = input.required<'section' | 'subsection' | 'article'>();
  readonly entityTitle = input.required<string>();
  readonly hasChildren = input<boolean>(false);
  readonly childrenCount = input<number>(0);

  readonly confirmed = output<void>();
  readonly cancelled = output<void>();

  // Internal state using signals
  readonly isDeleting = signal<boolean>(false);

  protected onConfirm(): void {
    this.visible.set(false);
    this.confirmed.emit();
  }

  protected onCancel(): void {
    this.visible.set(false);
    this.cancelled.emit();
  }

  // Helper to get the appropriate confirmation message key
  protected get confirmationMessageKey(): string {
    const type = this.entityType();
    const hasChildren = this.hasChildren();

    switch (type) {
      case 'section':
        return hasChildren
          ? 'help.delete.confirmation.section.cascade.message'
          : 'help.delete.confirmation.section.message';
      case 'subsection':
        return hasChildren
          ? 'help.delete.confirmation.subsection.cascade.message'
          : 'help.delete.confirmation.subsection.message';
      case 'article':
        return 'help.delete.confirmation.article.message';
      default:
        return '';
    }
  }
}
