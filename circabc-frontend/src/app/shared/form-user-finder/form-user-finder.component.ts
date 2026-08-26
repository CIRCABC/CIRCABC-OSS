import { Component, OnInit, input } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import { MatTooltipModule } from '@angular/material/tooltip';

import {
  InterestGroup,
  InterestGroupService,
  User,
  UserService,
} from 'app/core/generated/circabc';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { firstValueFrom } from 'rxjs';
import { CommonModule } from '@angular/common';
import { LoginService } from 'app/core/login.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'cbc-form-user-finder',
  templateUrl: './form-user-finder.component.html',
  styleUrl: './form-user-finder.component.scss',
  preserveWhitespaces: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    SpinnerComponent,
    MatTooltipModule,
    TranslocoModule,
  ],
})
export class FormUserFinderComponent implements OnInit {
  public readonly label = input('label.search.user');

  public readonly searchAdvancedForm = input.required<FormGroup>();

  public userSelected: User | undefined;
  public addUserForm!: FormGroup;
  public availableUsers: User[] = [];
  public searchingUsers = false;
  public isExternalUser = true;
  public currentGroup!: InterestGroup;
  public isReadOnly = false;
  public noAvailableUsers = false;
  public noAccess = false;

  constructor(
    private route: ActivatedRoute,
    private groupsService: InterestGroupService,
    private fb: FormBuilder,
    private userService: UserService,
    private loginService: LoginService
  ) {}

  public ngOnInit() {
    this.buildForm();
    this.searchAdvancedForm().valueChanges.subscribe((dataForm) => {
      this.userSelected = dataForm.creatorUser;
      if (dataForm.creatorUser === undefined) {
        this.resetForm();
      }
    });
    this.isExternalUser =
      this.loginService.getUser().properties?.domain === 'external';

    this.route.params.subscribe(async (params) => {
      if (params.id)
        this.currentGroup = await firstValueFrom(
          this.groupsService.getInterestGroup(params.id)
        );
    });
  }

  public buildForm(): void {
    this.addUserForm = this.fb.group(
      {
        name: [''],
        possibleUsers: [''],
      },
      {
        updateOn: 'change',
      }
    );
  }

  public async searchUsers() {
    let isValid = true;
    if (this.isExternalUser && !this.isValidEmail()) {
      isValid = false;
    }
    if (isValid) {
      if (this.addUserForm.controls.name.value !== '') {
        await this.populateUsers(this.addUserForm.controls.name.value);
      }
    }
  }

  public async populateUsers(query: string) {
    this.searchingUsers = true;
    this.availableUsers = [];
    this.noAvailableUsers = false;
    try {
      const res = await firstValueFrom(this.userService.getUsers(query));
      for (const user of res) {
        this.availableUsers.push(user);
      }
    } catch (_error) {
      this.noAccess = true;
    }
    if (this.availableUsers.length === 0) {
      this.noAvailableUsers = true;
    }
    this.searchingUsers = false;
  }

  public resetForm(): void {
    this.availableUsers = [];
    this.addUserForm.controls.name.setValue('');
    this.addUserForm.controls.possibleUsers.setValue('');
    this.noAvailableUsers = false;
    this.noAccess = false;
  }

  public async selectUser(user: User) {
    this.userSelected = user;
    this.addUserForm.controls.possibleUsers.setValue('');
    this.searchAdvancedForm().controls.creatorUser.setValue(user);
  }

  public removeUser(): void {
    this.userSelected = undefined;
    this.searchAdvancedForm().controls.creatorUser.setValue(null);
    this.resetForm();
  }

  public isSearchEmpty() {
    if (this.addUserForm) {
      return (
        this.addUserForm.value.name === '' ||
        this.addUserForm.value.name === null
      );
    }

    return false;
  }

  public isValidEmail() {
    if (this.addUserForm) {
      const emailRegex: RegExp = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      const email = this.addUserForm.value.name;
      return emailRegex.test(email);
    }
    return false;
  }
}
