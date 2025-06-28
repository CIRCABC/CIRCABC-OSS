import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { AddCategoryAdministratorComponent } from 'app/category/category-administrators/add-category-administrator/add-category-administrator.component';
import { CategoryService, User } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getErrorTranslation, getSuccessTranslation } from 'app/core/util';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { DownloadPipe } from 'app/shared/pipes/download.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-category-administrators',
  templateUrl: './category-administrators.component.html',
  styleUrl: './category-administrators.component.scss',
  preserveWhitespaces: true,
  imports: [
    InlineDeleteComponent,
    AddCategoryAdministratorComponent,
    DownloadPipe,
    SecurePipe,
    TranslocoModule,
  ],
})
export class CategoryAdministratorsComponent implements OnInit {
  public loading!: boolean;
  public administrators: User[] = [];
  public categoryId!: string;
  public showModal = false;

  constructor(
    private route: ActivatedRoute,
    private categoryService: CategoryService,
    private translateService: TranslocoService,
    private uiMessageService: UiMessageService,
    private loginService: LoginService
  ) {}

  ngOnInit() {
    this.route.params.subscribe(async (params) => {
      await this.initCategory(params.id);
    });
  }

  private async initCategory(id: string) {
    this.loading = true;
    this.categoryId = id;
    try {
      if (id) {
        this.administrators = await firstValueFrom(
          this.categoryService.getCategoryAdministrators(id)
        );
      }
    } catch (_error) {
      console.error('impossible to get the category');
    }
    this.loading = false;
  }

  public async uninviteUser(admin: User) {
    if (admin?.userId) {
      try {
        await firstValueFrom(
          this.categoryService.deleteCategoryAdministartor(
            this.categoryId,
            admin.userId
          )
        );
        await this.initCategory(this.categoryId);
        const text = this.translateService.translate(
          getSuccessTranslation(ActionType.DELETE_CATEGORY_ADMIN)
        );
        if (text) {
          this.uiMessageService.addSuccessMessage(text, true);
        }
      } catch (_error) {
        const text = this.translateService.translate(
          getErrorTranslation(ActionType.DELETE_CATEGORY_ADMIN)
        );
        if (text) {
          this.uiMessageService.addErrorMessage(text, true);
        }
      }
    }
  }

  public async refresh(res: ActionEmitterResult) {
    if (res.result === ActionResult.SUCCEED) {
      await this.initCategory(this.categoryId);
      this.showModal = false;
    } else if (res.result === ActionResult.CANCELED) {
      this.showModal = false;
    }
  }

  public canDeleteAdmin(admin: User): boolean {
    let result = false;
    const found = this.loginService.getCurrentUsername() === admin.userId;

    if (this.administrators.length > 1 && !found) {
      result = true;
    }

    return result;
  }
}
