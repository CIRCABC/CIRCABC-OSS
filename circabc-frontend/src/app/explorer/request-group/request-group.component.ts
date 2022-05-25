import { Component, Input, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import {
  Category,
  CategoryService,
  GroupCreationRequest,
  Header,
  HeaderService,
  User,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-request-group',
  templateUrl: './request-group.component.html',
  styleUrls: ['./request-group.component.scss'],
  preserveWhitespaces: true,
})
export class RequestGroupComponent implements OnInit {
  @Input()
  showModal = false;

  public form!: FormGroup;
  public processing = false;
  public categories: Category[] = [];
  public selectedCategory!: Category;
  public headers: Header[] = [];
  public searchingUsers = false;
  public step1 = true;
  public step2 = false;

  public groupLeadersForm!: FormGroup;

  public availableUsers: User[] = [];
  public futureMembers: User[] = [];

  constructor(
    private fb: FormBuilder,
    private categoryService: CategoryService,
    private headerService: HeaderService,
    private i18nPipe: I18nPipe,
    private userService: UserService,
    private loginService: LoginService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  async ngOnInit() {
    this.form = this.fb.group(
      {
        header: [undefined, Validators.required],
        category: [undefined, Validators.required],
        name: ['', Validators.required],
        title: [],
        description: [],
        comment: ['', Validators.required],
      },
      {
        updateOn: 'change',
      }
    );

    this.groupLeadersForm = this.fb.group(
      {
        search: [],
        possibleUsers: [],
      },
      {
        updateOn: 'change',
      }
    );

    await this.loadHeaders();

    this.listenHeaderChange();
    this.listenCategoryChange();

    this.route.queryParams.subscribe((params) => {
      if (params.header) {
        this.form.controls.header.setValue(params.header);
      }

      if (params.category) {
        this.form.controls.category.setValue(params.category);
      }
    });
  }

  listenHeaderChange() {
    const headerControl = this.form.get('header');
    if (headerControl) {
      headerControl.valueChanges.forEach(
        (
          value: string // eslint-disable-line
        ) => this.loadCategories(value)
      );
    }
  }

  listenCategoryChange() {
    const categoryControl = this.form.get('category');
    if (categoryControl) {
      categoryControl.valueChanges.forEach((value: string) => {
        // eslint-disable-line
        for (const categ of this.categories) {
          if (categ.id === value) {
            this.selectedCategory = categ;
          }
        }
      });
    }
  }

  async loadHeaders() {
    this.headers = await firstValueFrom(this.headerService.getHeaders());
  }

  async loadCategories(id: string) {
    if (id) {
      this.categories = await firstValueFrom(
        this.headerService.getCategoriesByHeaderId(id)
      );

      if (this.form.controls.category.value !== '') {
        this.categories.forEach((category) => {
          if (category.id === this.form.controls.category.value) {
            this.selectedCategory = category;
          }
        });
      }
    }
  }

  getNameOrTitle(category: Category): string {
    if (category.title && Object.keys(category.title).length > 0) {
      return this.i18nPipe.transform(category.title);
    } else {
      return category.name;
    }
  }

  async requestGroup() {
    this.processing = true;

    const request: GroupCreationRequest = {
      from: this.loginService.getUser(),
      proposedName: this.form.value.name,
      proposedTitle: { en: this.form.value.title },
      proposedDescription: { en: this.form.value.description },
      justification: this.form.value.comment,
      categoryRef: this.selectedCategory.id,
      leaders: this.futureMembers,
    };

    if (this.selectedCategory && this.selectedCategory.id) {
      await firstValueFrom(
        this.categoryService.postRequestInterestGroup(
          this.selectedCategory.id,
          request
        )
      );
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      this.router.navigate(['..', { relativeTo: this.route }]);
    }

    this.processing = false;
  }

  resetSearchForm() {
    this.groupLeadersForm.reset();
  }

  async searchUsers() {
    if (
      this.groupLeadersForm.controls.search.value &&
      this.groupLeadersForm.controls.search.value !== ''
    ) {
      this.searchingUsers = true;
      this.availableUsers = await firstValueFrom(
        this.userService.getUsers(this.groupLeadersForm.controls.search.value)
      );
      this.searchingUsers = false;
    }
  }

  public resetForm(): void {
    this.availableUsers = [];
    this.groupLeadersForm.controls.search.setValue('');
    this.groupLeadersForm.controls.possibleUsers.setValue('');
  }

  public selectUsers(): void {
    const membersTmp: User[] = [];
    this.groupLeadersForm.controls.possibleUsers.value.filter(
      (userid: string) => {
        const memberTmp = this.availableUsers.find((user) => {
          return user.userId === userid;
        });
        if (memberTmp) {
          membersTmp.push(memberTmp);
        }
      }
    );

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

  public removeFromFutureMember(m: User): void {
    const index: number = this.futureMembers.indexOf(m, 0);
    this.futureMembers.splice(index, 1);
  }

  confirm() {
    this.step1 = false;
    this.step2 = true;
  }

  revert() {
    this.step2 = false;
    this.step1 = true;
  }

  get headerControl(): AbstractControl {
    return this.form.controls.header;
  }

  get categoryControl(): AbstractControl {
    return this.form.controls.category;
  }

  get nameControl(): AbstractControl {
    return this.form.controls.name;
  }

  get commentControl(): AbstractControl {
    return this.form.controls.comment;
  }
}
