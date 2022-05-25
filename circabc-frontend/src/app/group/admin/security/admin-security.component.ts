import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import {
  InterestGroup,
  InterestGroupService,
} from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-admin-security',
  templateUrl: './admin-security.component.html',
  styleUrls: ['./admin-security.component.scss'],
  preserveWhitespaces: true,
})
export class AdminSecurityComponent implements OnInit {
  public securityForm!: FormGroup;
  public ig!: InterestGroup;
  public saving = false;

  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private interestGroupService: InterestGroupService
  ) {}

  ngOnInit() {
    this.securityForm = this.fb.group(
      {
        visibility: [0],
        applicants: [false],
      },
      {
        updateOn: 'change',
      }
    );
    this.route.params.subscribe(async (params) => await this.loadIg(params));
  }

  private async loadIg(params: { [key: string]: string }) {
    const id = params.id;
    if (id) {
      this.ig = await firstValueFrom(
        this.interestGroupService.getInterestGroup(id)
      );

      if (!this.ig.isPublic && !this.ig.isRegistered) {
        this.securityForm.controls.visibility.setValue(0);
      } else if (this.ig.isPublic) {
        // guest
        this.securityForm.controls.visibility.setValue(2);
      } else if (!this.ig.isPublic && this.ig.isRegistered) {
        // registered
        this.securityForm.controls.visibility.setValue(1);
      }

      this.securityForm.controls.applicants.setValue(this.ig.allowApply);
    }
  }

  public async cancel() {
    this.saving = false;
    await this.loadIg({ id: this.ig.id as string });
  }

  public async save() {
    this.saving = true;

    if (this.securityForm.value.visibility === 2) {
      this.ig.isPublic = true;
      this.ig.isRegistered = true;
    } else if (this.securityForm.value.visibility === 1) {
      this.ig.isPublic = false;
      this.ig.isRegistered = true;
    } else {
      this.ig.isPublic = false;
      this.ig.isRegistered = false;
    }

    this.ig.allowApply = this.securityForm.value.applicants;

    await firstValueFrom(
      this.interestGroupService.putInterestGroup(this.ig.id as string, this.ig)
    );
    await this.loadIg({ id: this.ig.id as string });

    this.saving = false;
  }

  public isGuestVisible(): boolean {
    return this.securityForm.value.visibility === 2;
  }

  public isRegisteredVisible(): boolean {
    return this.securityForm.value.visibility === 1;
  }

  public isPrivateVisible(): boolean {
    return this.securityForm.value.visibility === 0;
  }

  public isApplicationAllowed(): boolean {
    return this.securityForm.value.applicants === true;
  }

  public getVisibilityLabel(): string {
    if (this.isGuestVisible()) {
      return 'label.public';
    } else if (this.isRegisteredVisible()) {
      return 'label.users';
    } else {
      return 'label.private';
    }
  }

  public getApplicantsYesNoLabel(): string {
    return this.isApplicationAllowed() ? 'label.yes' : 'label.no';
  }
}
