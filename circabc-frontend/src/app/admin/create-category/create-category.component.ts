import { Component, model, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import {
  CategoryService,
  Header,
  HeaderService,
  User,
  UserService,
} from 'app/core/generated/circabc';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { MultilingualInputComponent } from 'app/shared/input/multilingual-input.component';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-create-category',
  templateUrl: './create-category.component.html',
  styleUrl: './create-category.component.scss',
  preserveWhitespaces: true,
  imports: [
    ModalComponent,
    DataCyDirective,
    ReactiveFormsModule,
    ControlMessageComponent,
    MultilingualInputComponent,
    SpinnerComponent,
    TranslocoModule,
  ],
})
export class CreateCategoryComponent implements OnInit {
  public showModal = model<boolean>(false);

  public addUserForm!: FormGroup;
  public categoryForm!: FormGroup;
  public headers!: Header[];
  public step = 'details';
  public futureAdmins: User[] = [];
  public availableUsers: User[] = [];
  public searchingUsers = false;
  public processing = false;

  constructor(
    private fb: FormBuilder,
    private headerService: HeaderService,
    private categoryService: CategoryService,
    private userService: UserService
  ) {}

  async ngOnInit() {
    this.categoryForm = this.fb.group({
      name: ['', Validators.required],
      title: [''],
      header: ['', Validators.required],
    });

    this.addUserForm = this.fb.group({
      name: [''],
      possibleUsers: [''],
    });

    this.headers = await firstValueFrom(this.headerService.getHeaders());
  }

  public cancel() {
    this.addUserForm.reset();
    this.categoryForm.reset({
      name: '',
      title: '',
      header: '',
    });

    this.availableUsers = [];
    this.futureAdmins = [];

    this.showModal.set(false);
  }

  get nameControl(): AbstractControl {
    return this.categoryForm.controls.name;
  }

  get titleControl(): AbstractControl {
    return this.categoryForm.controls.title;
  }

  get headerControl(): AbstractControl {
    return this.categoryForm.controls.header;
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
    if (this.categoryForm.valid && this.futureAdmins.length > 0) {
      return true;
    }

    return false;
  }

  public async createCategory() {
    this.processing = true;
    try {
      const category = this.categoryForm.value;
      const newCategory = await firstValueFrom(
        this.categoryService.postCategory(
          this.categoryForm.value.header,
          category
        )
      );

      if (newCategory?.id) {
        const userIds: string[] = [];
        this.futureAdmins.forEach((user: User) => {
          if (user.userId) {
            userIds.push(user.userId);
          }
        });
        await firstValueFrom(
          this.categoryService.postCategoryAdministartors(
            newCategory.id,
            userIds
          )
        );

        this.cancel();
      }
    } catch (_error) {
      console.error('Error during the creation of the category');
    }
    this.processing = false;
  }
}
