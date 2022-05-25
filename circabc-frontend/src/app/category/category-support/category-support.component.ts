import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Category, CategoryService, User } from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-category-support',
  templateUrl: './category-support.component.html',
  styleUrls: ['./category-support.component.scss'],
  preserveWhitespaces: true,
})
export class CategorySupportComponent implements OnInit {
  public updating = false;
  public category!: Category;
  public categoryForm!: FormGroup;
  public administrators: User[] = [];

  constructor(
    private route: ActivatedRoute,
    private categoryService: CategoryService,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.categoryForm = this.fb.group({
      useSingleContact: [false],
      contactEmail: ['', Validators.required],
      contactEmails: [''],
    });

    this.route.params.subscribe(async (params) => {
      await this.initCategory(params.id);
    });
  }

  private async initCategory(id: string) {
    try {
      this.category = await firstValueFrom(
        this.categoryService.getCategory(id)
      );
      this.administrators = await firstValueFrom(
        this.categoryService.getCategoryAdministrators(id)
      );

      if (this.categoryForm) {
        this.categoryForm.controls.useSingleContact.setValue(
          this.category.useSingleContact
        );
        if (this.category.useSingleContact && this.category.contactEmails) {
          this.categoryForm.controls.contactEmail.setValue(
            this.category.contactEmails[0]
          );
        } else if (
          !this.category.useSingleContact &&
          this.category.contactEmails
        ) {
          const emails = this.arrayToString(this.category.contactEmails);
          this.categoryForm.controls.contactEmails.setValue(emails);
        }
      }
    } catch (error) {
      console.error('impossible to get the category');
    }
  }

  public async cancel() {
    if (this.category.id) {
      await this.initCategory(this.category.id);
    }
  }

  public async update() {
    this.updating = true;
    if (this.category && this.category.id && this.isFormValid()) {
      try {
        this.category.useSingleContact =
          this.categoryForm.value.useSingleContact;
        if (!this.categoryForm.value.useSingleContact) {
          this.category.contactEmails = this.stringToArray(
            this.categoryForm.value.contactEmails
          );
        } else {
          this.category.contactEmails = [this.categoryForm.value.contactEmail];
        }

        await firstValueFrom(
          this.categoryService.putCategory(this.category.id, this.category)
        );
        await this.initCategory(this.category.id);
      } catch (error) {
        console.error(error);
      }
    }
    this.updating = false;
  }

  public toggleSelect(email: string | undefined) {
    if (email === undefined) {
      return;
    }
    const formValue = `${this.categoryForm.value.contactEmails}`;
    const emails: string[] = formValue.split(',');

    const indexEmail = emails.indexOf(email);
    if (indexEmail === -1) {
      emails.push(email);
    } else {
      emails.splice(indexEmail, 1);
    }

    const stringEmails = this.arrayToString(emails);
    this.categoryForm.controls.contactEmails.setValue(stringEmails);
  }

  public isEmailSelected(email: string | undefined): boolean {
    if (email === undefined) {
      return false;
    }
    const formValue = `${this.categoryForm.value.contactEmails}`;
    const emails: string[] = formValue.split(',');
    return emails.indexOf(email) !== -1;
  }

  private arrayToString(array: string[]): string {
    let result = '';

    for (const str of array) {
      if (str !== '') {
        result = `${result}${str},`;
      }
    }
    return result;
  }

  private stringToArray(str: string): string[] {
    const result = [];

    for (const part of str.split(',')) {
      if (part !== '') {
        result.push(part);
      }
    }
    return result;
  }

  public isFormValid() {
    let result = false;
    if (this.categoryForm.value.useSingleContact) {
      result = this.categoryForm.controls.contactEmail.valid;
    } else {
      const formValue = `${this.categoryForm.value.contactEmails}`;
      // > 1, because split returns at least 1 empty string cell
      result = formValue.split(',').length > 1;
    }

    return result;
  }
}
