import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { AppMessageService } from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-old-ui-configuration',
  templateUrl: './old-ui-configuration.component.html',
  styleUrls: ['./old-ui-configuration.component.scss'],
  preserveWhitespaces: true,
})
export class OldUiConfigurationComponent implements OnInit {
  public form!: FormGroup;

  constructor(
    private appMessageService: AppMessageService,
    private fb: FormBuilder
  ) {}

  async ngOnInit() {
    this.form = this.fb.group({
      enableOld: false,
      display: true,
    });

    const config = await firstValueFrom(
      this.appMessageService.getDisplayOldMessage()
    );
    const configOldMessage = await firstValueFrom(
      this.appMessageService.getEnableOldMessage()
    );

    this.form.controls.display.setValue(config.display);
    this.form.controls.enableOld.setValue(configOldMessage.enable);

    this.form.controls.display.valueChanges.forEach((value) => {
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      this.updateSettings(value);
    });

    this.form.controls.enableOld.valueChanges.forEach((value) => {
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      this.toggleOldMessage(value);
    });
  }

  async updateSettings(value: boolean) {
    if (value !== undefined) {
      await firstValueFrom(this.appMessageService.setDisplayOldMessage(value));
    }
  }

  async toggleOldMessage(value: boolean) {
    if (value !== undefined) {
      await firstValueFrom(this.appMessageService.setEnableOldMessage(value));
    }
  }
}
