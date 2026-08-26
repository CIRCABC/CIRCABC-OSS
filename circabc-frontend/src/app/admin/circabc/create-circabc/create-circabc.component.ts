import { Component, model, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import { CiracbcAdminReloadListenerService } from 'app/core/circabc-admin-reload-listener.service';
import { CircabcService, User, UserService } from 'app/core/generated/circabc';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-create-circabc',
  templateUrl: './create-circabc.component.html',
  styleUrl: './create-circabc.component.scss',
  preserveWhitespaces: true,
  imports: [
    ModalComponent,
    DataCyDirective,
    ReactiveFormsModule,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class CreateCircabcComponent implements OnInit {
  showModal = model(false);

  public addUserForm!: FormGroup;
  public step = 'admins';
  public futureAdmins: User[] = [];
  public availableUsers: User[] = [];
  public searchingUsers = false;
  public processing = false;

  constructor(
    private fb: FormBuilder,
    private circabcService: CircabcService,
    private userService: UserService,
    private ciracbcAdminReloadListenerService: CiracbcAdminReloadListenerService
  ) {}

  async ngOnInit() {
    this.addUserForm = this.fb.group({
      name: [''],
      possibleUsers: [''],
    });
  }

  public cancel() {
    this.addUserForm.reset();

    this.availableUsers = [];
    this.futureAdmins = [];

    this.showModal.set(false);
  }

  public async searchUsers() {
    if (this.addUserForm.controls.name.value !== '') {
      this.addUserForm.controls.possibleUsers.setValue('');
      await this.populateUsers(this.addUserForm.controls.name.value);
    }
  }

  public async populateUsers(query: string) {
    this.searchingUsers = true;
    const res = await firstValueFrom(this.userService.getUsers(query, false));
    this.availableUsers = [];
    for (const user of res) {
      this.availableUsers.push(user);
    }
    this.searchingUsers = false;
  }

  public resetForm(): void {
    this.availableUsers = [];
    this.addUserForm.controls.name.setValue('');
    this.addUserForm.controls.possibleUsers.setValue('');
  }

  public selectUsers(): void {
    const membersTmp: User[] = [];
    this.addUserForm.controls.possibleUsers.value.filter((userid: string) => {
      const memberTmp = this.availableUsers.find((user) => {
        return user.userId === userid;
      });

      if (memberTmp) {
        membersTmp.push(memberTmp);
      }
    });

    this.futureAdmins = this.futureAdmins.concat(
      membersTmp.filter((memberTmp) => {
        return (
          this.futureAdmins.find((member) => {
            if (member && memberTmp) {
              return member.userId === memberTmp.userId;
            }
            return true;
          }) === undefined
        );
      })
    );
  }

  hasSelectedUser(): boolean {
    return this.addUserForm.controls.possibleUsers.value !== '';
  }

  public removeFromFutureAdmin(m: User): void {
    const index: number = this.futureAdmins.indexOf(m, 0);
    this.futureAdmins.splice(index, 1);
  }

  public isWizardOk(): boolean {
    if (this.futureAdmins.length > 0) {
      return true;
    }

    return false;
  }

  public async inviteCircabcAdmin() {
    this.processing = true;
    try {
      const userIds: string[] = [];
      this.futureAdmins.forEach((user: User) => {
        if (user.userId) {
          userIds.push(user.userId);
        }
      });
      await firstValueFrom(
        this.circabcService.postCircabcAdministrators(userIds)
      );
      this.ciracbcAdminReloadListenerService.propagateCircabcAdminRefresh();
      this.cancel();
    } catch (_error) {
      console.error('Error during the creation of the category');
    }
    this.processing = false;
  }
}
