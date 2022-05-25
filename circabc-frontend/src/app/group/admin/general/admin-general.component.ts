import { Component, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import {
  InterestGroup,
  InterestGroupService,
} from 'app/core/generated/circabc';
import { GroupReloadListenerService } from 'app/core/group-reload-listener.service';
import { ValidationService } from 'app/core/validation.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-admin-general',
  templateUrl: './admin-general.component.html',
  preserveWhitespaces: true,
})
export class AdminGeneralComponent implements OnInit {
  public ig!: InterestGroup;
  public igForm!: FormGroup;
  public saving = false;

  constructor(
    private route: ActivatedRoute,
    private groupsService: InterestGroupService,
    private fb: FormBuilder,
    private groupReloadListenerService: GroupReloadListenerService
  ) {}

  ngOnInit() {
    this.igForm = this.fb.group(
      {
        id: [''],
        name: ['', [Validators.required, ValidationService.fileNameValidator]],
        title: [''],
        description: [''],
        contact: [''],
      },
      {
        updateOn: 'change',
      }
    );

    this.route.params.subscribe(async (params) => await this.loadIg(params));
  }

  public async loadIg(params: { [key: string]: string }) {
    const id = params.id;

    if (id) {
      this.ig = await firstValueFrom(this.groupsService.getInterestGroup(id));

      this.igForm.patchValue({ id: this.ig.id });
      this.igForm.patchValue({ name: this.ig.name });
      this.igForm.patchValue({ title: this.ig.title });
      this.igForm.patchValue({ description: this.ig.description });
      this.igForm.patchValue({ contact: this.ig.contact });
    }

    // scroll top not needed in angular 9 but needed in angular 10
    window.scroll(0, 0);
  }

  public async cancel() {
    await this.loadIg({ id: this.ig.id as string });
  }

  public async save() {
    if (this.igForm.valid) {
      this.saving = true;
      const body = this.igForm.value;
      try {
        await firstValueFrom(
          this.groupsService.putInterestGroup(this.ig.id as string, body)
        );
        await this.loadIg({ id: this.ig.id as string });
        if (this.ig.id) {
          this.groupReloadListenerService.propagateGroupRefresh(this.ig.id);
        }
      } catch (error) {
        console.error(error);
      } finally {
        this.saving = false;
      }
    }
  }

  get nameControl(): AbstractControl {
    return this.igForm.controls.name;
  }
}
