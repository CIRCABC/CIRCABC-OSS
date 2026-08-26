import { Component, Input, OnInit, output, input } from '@angular/core';

import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  InformationPage,
  InformationService,
} from 'app/core/generated/circabc';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-configure-information',
  templateUrl: './configure-information.component.html',
  preserveWhitespaces: true,
  imports: [ModalComponent, ReactiveFormsModule, TranslocoModule],
})
export class ConfigureInformationComponent implements OnInit {
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal = false;
  readonly groupId = input.required<string>();
  readonly modalHide = output<ActionEmitterResult>();

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

    const groupId = this.groupId();
    if (groupId) {
      this.infPage = await firstValueFrom(
        this.informationService.getInformationDefinitions(groupId)
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
        this.informationService.putInformationDefinitions(this.groupId(), body)
      );
      this.showModal = false;
      res.result = ActionResult.SUCCEED;
    } catch (_error) {
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
