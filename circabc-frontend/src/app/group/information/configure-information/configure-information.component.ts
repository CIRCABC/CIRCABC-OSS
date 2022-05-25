import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

import { FormBuilder, FormGroup } from '@angular/forms';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  InformationPage,
  InformationService,
} from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-configure-information',
  templateUrl: './configure-information.component.html',
  preserveWhitespaces: true,
})
export class ConfigureInformationComponent implements OnInit {
  @Input()
  showModal = false;
  @Input()
  groupId!: string;
  @Output()
  readonly modalHide = new EventEmitter<ActionEmitterResult>();

  public processing = false;
  public infPage!: InformationPage;
  public configurationForm!: FormGroup;

  constructor(
    private informationService: InformationService,
    private fb: FormBuilder
  ) {}

  async ngOnInit() {
    this.configurationForm = this.fb.group({
      displayOldInformation: [false],
    });

    if (this.groupId) {
      this.infPage = await firstValueFrom(
        this.informationService.getInformationDefinitions(this.groupId)
      );

      this.configurationForm.patchValue({
        displayOldInformation:
          this.infPage.displayOldInformation !== undefined
            ? this.infPage.displayOldInformation
            : false,
      });
    }
  }

  public cancel() {
    const res: ActionEmitterResult = {};
    res.result = ActionResult.CANCELED;
    res.type = ActionType.UPDATE_INFORMATION_CONFIGURATION;
    this.configurationForm.patchValue({
      displayOldInformation:
        this.infPage.displayOldInformation !== undefined
          ? this.infPage.displayOldInformation
          : false,
    });
    this.showModal = false;
    this.modalHide.emit(res);
  }

  public async save() {
    const res: ActionEmitterResult = {};
    res.type = ActionType.UPDATE_INFORMATION_CONFIGURATION;
    try {
      const body = this.infPage;
      body.displayOldInformation =
        this.configurationForm.value.displayOldInformation;
      await firstValueFrom(
        this.informationService.putInformationDefinitions(this.groupId, body)
      );
      this.showModal = false;
      res.result = ActionResult.SUCCEED;
    } catch (error) {
      console.error('impossible to save the configuration');
      res.result = ActionResult.FAILED;
    }

    this.modalHide.emit(res);
  }

  public getDisplayValue(): boolean {
    if (this.configurationForm) {
      return this.configurationForm.value.displayOldInformation;
    }
    return false;
  }
}
