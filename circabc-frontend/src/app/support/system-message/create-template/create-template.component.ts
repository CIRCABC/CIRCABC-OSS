import { Component, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AppMessageService } from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-create-template',
  templateUrl: './create-template.component.html',
  preserveWhitespaces: true,
})
export class CreateTemplateComponent implements OnInit {
  public templateForm!: FormGroup;
  public processing = false;
  public updateMode = false;

  constructor(
    private fb: FormBuilder,
    private appMessageService: AppMessageService,
    private router: Router,
    private activatedRoute: ActivatedRoute
  ) {}

  ngOnInit() {
    this.templateForm = this.fb.group({
      id: [''],
      content: ['', Validators.required],
      displayTime: [15],
      dateClosure: [''],
      level: ['info'],
      enabled: [false],
      notification: [false],
    });

    if (this.activatedRoute.params) {
      this.activatedRoute.params.subscribe(async (params) => {
        if (params.id) {
          await this.getTemplate(params.id);
        }
      });
    }
  }

  private async getTemplate(templateID: string) {
    this.updateMode = true;
    const data = await firstValueFrom(
      this.appMessageService.getAppMessageTemplate(templateID)
    );
    let autoClose: string | Date = '';
    if (
      data.dateClosure !== undefined &&
      data.dateClosure.toLocaleString().length > 0
    ) {
      autoClose = new Date(data.dateClosure);
    }
    this.templateForm.patchValue({
      id: data.id,
      content: data.content,
      displayTime: data.displayTime,
      dateClosure: autoClose,
      level: data.level,
      enabled: data.enabled,
    });
  }

  public isEnabled(): boolean {
    if (this.templateForm && this.templateForm.value) {
      return this.templateForm.value.enabled;
    }
    return false;
  }

  get contentControl(): AbstractControl {
    return this.templateForm.controls.content;
  }

  public async saveOrUpdateTemplate() {
    this.processing = true;

    try {
      if (!this.updateMode) {
        await firstValueFrom(
          this.appMessageService.addAppMessageTemplate(
            this.templateForm.value,
            this.templateForm.value.notification
          )
        );
      } else {
        await firstValueFrom(
          this.appMessageService.updateAppMessageTemplate(
            this.templateForm.value.id,
            this.templateForm.value,
            this.templateForm.value.notification
          )
        );
      }
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      this.router.navigate(['support', 'system-message']);
    } catch (error) {
      console.error(error);
    }

    this.processing = false;
  }

  public isFormValid(): boolean {
    if (this.templateForm) {
      return this.templateForm.valid;
    }

    return false;
  }

  public cleanClosure() {
    if (this.templateForm) {
      this.templateForm.patchValue({ dateClosure: '' });
    }
  }

  public isNotified(): boolean {
    if (this.templateForm) {
      return this.templateForm.value.notification;
    }

    return false;
  }

  public async cancel() {
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    this.router.navigate(['support', 'system-message']);
  }

  public checkNotification() {
    if (this.templateForm && !this.templateForm.value.enabled) {
      this.templateForm.controls.notification.setValue(false);
    }
  }
}
