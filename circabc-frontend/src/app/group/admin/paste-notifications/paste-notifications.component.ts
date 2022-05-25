import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import {
  NotificationService,
  PasteNotificationsState,
} from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-paste-notifications',
  templateUrl: './paste-notifications.component.html',
  preserveWhitespaces: true,
})
export class PasteNotificationsComponent implements OnInit {
  public igId!: string;
  public saving = false;
  public pasteNotificationsState!: PasteNotificationsState;

  public pasteNotificationsForm!: FormGroup;

  constructor(
    private route: ActivatedRoute,
    private notificationService: NotificationService,
    private formBuilder: FormBuilder
  ) {}

  ngOnInit() {
    this.pasteNotificationsForm = this.formBuilder.group(
      {
        notifyPaste: [false],
        notifyPasteAll: [false],
      },
      {
        updateOn: 'change',
      }
    );

    this.route.params.subscribe(
      async (params) => await this.getNotificationState(params)
    );
  }

  private async getNotificationState(params: { [key: string]: string }) {
    this.igId = params.id;
    await this.getState();
  }

  private async getState() {
    this.pasteNotificationsState = await firstValueFrom(
      this.notificationService.getPasteNotifications(this.igId)
    );
    this.pasteNotificationsForm.controls.notifyPaste.patchValue(
      this.pasteNotificationsState.pasteEnabled
    );
    this.pasteNotificationsForm.controls.notifyPasteAll.patchValue(
      this.pasteNotificationsState.pasteAllEnabled
    );
  }

  public async save() {
    this.saving = true;

    await firstValueFrom(
      this.notificationService.postNotificationStatus(
        this.igId,
        this.pasteNotificationsForm.value.notifyPaste,
        this.pasteNotificationsForm.value.notifyPasteAll
      )
    );

    this.saving = false;
  }

  public async cancel() {
    await this.getState();
  }
}
