import { DatePipe } from '@angular/common';
import { Component, Input, inject, output } from '@angular/core';

import { MatDialog } from '@angular/material/dialog';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  CategoryService,
  GroupCreationRequest,
} from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { UserCardComponent } from 'app/shared/user-card/user-card.component';

import { MatTooltipModule } from '@angular/material/tooltip';
import { AcceptFormComponent } from './accept-form/accept-form.component';
import { DeclineFormComponent } from './decline-form/decline-form.component';

@Component({
  selector: 'cbc-group-request-create-element',
  templateUrl: './group-request-create-element.component.html',
  styleUrl: './group-request-create-element.component.scss',
  imports: [
    UserCardComponent,
    RouterLink,
    MatTooltipModule,
    DatePipe,
    I18nPipe,
    TranslocoModule,
  ],
})
export class GroupRequestCreateElementComponent {
  @Input() request!: GroupCreationRequest & { interestGroupId?: string };
  @Input() categoryId!: string;
  readonly reloadGroupRequests = output();
  categoryService = inject(CategoryService);
  dialog = inject(MatDialog);

  public async acceptDialog() {
    const dialogRef = this.dialog.open(AcceptFormComponent, {
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
    const dialogRef = this.dialog.open(DeclineFormComponent, {
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
}
