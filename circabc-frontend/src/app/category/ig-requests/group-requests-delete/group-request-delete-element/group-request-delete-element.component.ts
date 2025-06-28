import { Component, inject, Input, output } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import { DatePipe, I18nSelectPipe } from '@angular/common';
import { SharedModule } from 'primeng/api';
import { EditorModule } from 'primeng/editor';
import type { GroupDeletionRequest } from 'app/core/generated/circabc';
import { UserCardComponent } from 'app/shared/user-card/user-card.component';
import { RouterLink } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { AcceptDeleteFormComponent } from './accept-delete-form/delete-form.component';
import { DeclineDeleteFormComponent } from './decline-delete-form/decline-delete-form.component';

@Component({
  selector: 'cbc-group-request-delete-element',
  templateUrl: './group-request-delete-element.component.html',
  styleUrl: './group-request-delete-element.component.scss',
  providers: [I18nSelectPipe],
  imports: [
    ReactiveFormsModule,
    EditorModule,
    SharedModule,
    RouterLink,
    TranslocoModule,
    UserCardComponent,
    DatePipe,
  ],
})
export class GroupRequestDeleteElementComponent {
  @Input() request!: GroupDeletionRequest;
  @Input() categoryId!: string | undefined;
  readonly reloadGroupRequests = output();
  dialog = inject(MatDialog);

  public async acceptDialog() {
    const dialogRef = this.dialog.open(AcceptDeleteFormComponent, {
      data: {
        request: this.request,
        categoryId: this.categoryId,
      },
    });

    dialogRef.afterClosed().subscribe((response) => {
      if (response) {
        this.reloadGroupRequests.emit();
      }
    });
  }

  public async rejectDialog() {
    const dialogRef = this.dialog.open(DeclineDeleteFormComponent, {
      data: {
        request: this.request,
        categoryId: this.categoryId,
      },
    });

    dialogRef.afterClosed().subscribe((response) => {
      if (response) {
        this.reloadGroupRequests.emit();
      }
    });
  }
  cleanSpace(text?: string) {
    if (text) {
      return text.replace(/&nbsp;/g, ' ');
    }
    return '';
  }
}
