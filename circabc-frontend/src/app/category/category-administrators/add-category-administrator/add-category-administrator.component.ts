import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';

import { CategoryService, User, UserService } from 'app/core/generated/circabc';
import { environment } from 'environments/environment';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-add-category-administrator',
  templateUrl: './add-category-administrator.component.html',
  preserveWhitespaces: true,
})
export class AddCategoryAdministratorComponent implements OnInit {
  @Input()
  showModal = false;
  @Input()
  categoryId!: string;
  @Output()
  public readonly modalHide = new EventEmitter<ActionEmitterResult>();

  public processing = false;
  public addUserForm!: FormGroup;
  public availableUsers: User[] = [];
  public existingAdmins: User[] = [];
  public futureMembers: User[] = [];
  public searchingUsers = false;
  public isOSS = false;

  constructor(
    private categoryService: CategoryService,
    private fb: FormBuilder,
    private userService: UserService
  ) {}

  async ngOnInit() {
    this.addUserForm = this.fb.group({
      name: [''],
      possibleUsers: [''],
      filter: [true],
    });

    if (environment.circabcRelease === 'oss') {
      this.isOSS = true;
    }
    if (this.categoryId) {
      this.existingAdmins = await firstValueFrom(
        this.categoryService.getCategoryAdministrators(this.categoryId)
      );
    }
  }

  public async addAdmins() {
    this.processing = true;
    const res: ActionEmitterResult = { type: ActionType.INVITE_CATEGORY_ADMIN };

    try {
      const userIds: string[] = [];
      this.futureMembers.forEach((user: User) => {
        if (user.userId) {
          userIds.push(user.userId);
        }
      });

      await firstValueFrom(
        this.categoryService.postCategoryAdministartors(
          this.categoryId,
          userIds
        )
      );
      res.result = ActionResult.SUCCEED;
      this.showModal = false;
      this.resetForm();
      this.futureMembers = [];
    } catch (error) {
      console.error('impossible to add the users as category admins');
      res.result = ActionResult.FAILED;
    }
    this.processing = false;
    this.modalHide.emit(res);
  }

  public cancel() {
    this.resetForm();
    this.futureMembers = [];
    this.showModal = false;

    const result: ActionEmitterResult = {
      result: ActionResult.CANCELED,
      type: ActionType.INVITE_CATEGORY_ADMIN,
    };

    this.modalHide.emit(result);
  }

  public async searchUsers() {
    if (this.addUserForm.controls.name.value !== '') {
      this.addUserForm.controls.possibleUsers.setValue('');
      await this.populateUsers(
        this.addUserForm.controls.name.value,
        this.addUserForm.controls.filter.value
      );
    }
  }

  public async populateUsers(query: string, filter: boolean) {
    this.searchingUsers = true;
    const res = await firstValueFrom(this.userService.getUsers(query, filter));
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

  public isAlreadyAdmin(user: User): boolean {
    for (const member of this.existingAdmins) {
      if (member && member.userId === user.userId) {
        return true;
      }
    }
    return false;
  }

  public selectUsers(): void {
    const membersTmp: User[] = [];
    this.addUserForm.controls.possibleUsers.value.filter((userId: string) => {
      const memberTmp = this.availableUsers.find((user) => {
        return user.userId === userId;
      });

      if (memberTmp) {
        membersTmp.push(memberTmp);
      }
    });

    this.futureMembers = this.futureMembers.concat(
      membersTmp.filter((memberTmp) => {
        return (
          this.futureMembers.find((member) => {
            if (member && memberTmp) {
              return member.userId === memberTmp.userId;
            } else {
              return true;
            }
          }) === undefined
        );
      })
    );
  }

  hasSelectedUser(): boolean {
    return this.addUserForm.controls.possibleUsers.value !== '';
  }

  public removeFromFutureMember(m: User): void {
    const index: number = this.futureMembers.indexOf(m, 0);
    this.futureMembers.splice(index, 1);
  }
}
