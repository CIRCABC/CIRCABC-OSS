import { Component, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  AdminContactRequest,
  Category,
  CategoryService,
  Header,
  HeaderService,
} from 'app/core/generated/circabc';
import { HeaderComponent } from 'app/shared/header/header.component';
import { NavigatorComponent } from 'app/shared/navigator/navigator.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { SharedModule } from 'primeng/api';
import { EditorModule } from 'primeng/editor';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-contact-category',
  templateUrl: './contact-category.component.html',
  styleUrl: './contact-category.component.scss',
  preserveWhitespaces: true,
  imports: [
    HeaderComponent,
    NavigatorComponent,
    ReactiveFormsModule,
    EditorModule,
    SharedModule,
    SpinnerComponent,
    RouterLink,
    TranslocoModule,
  ],
})
export class ContactCategoryComponent implements OnInit {
  public form!: FormGroup;
  public processing = false;
  public categories: Category[] = [];
  public selectedCategory!: Category;
  public headers: Header[] = [];

  constructor(
    private fb: FormBuilder,
    private categoryService: CategoryService,
    private headerService: HeaderService,
    private i18nPipe: I18nPipe,
    private route: ActivatedRoute
  ) {}

  async ngOnInit() {
    this.form = this.fb.group(
      {
        header: [undefined, Validators.required],
        category: [undefined, Validators.required],
        messageContent: ['', Validators.required],
        sendCopy: false,
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
    }
  }

  getNameOrTitle(category: Category): string {
    if (category.title && Object.keys(category.title).length > 0) {
      return this.i18nPipe.transform(category.title);
    }
    return category.name;
  }

  async contact() {
    this.processing = true;
    try {
      const body: AdminContactRequest = {
        content: this.form.value.messageContent,
        sendCopy: this.form.value.sendCopy,
      };

      await firstValueFrom(
        this.categoryService.contactCategoryAdmins(
          this.form.value.category,
          body
        )
      );
    } catch (error) {
      console.error(error);
    }
    this.processing = false;
  }
}
