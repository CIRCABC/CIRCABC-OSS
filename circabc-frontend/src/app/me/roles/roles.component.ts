import { Component, OnInit } from '@angular/core';

import {
  Category,
  InterestGroup,
  InterestGroupProfile,
  Profile,
  User,
  UserService,
} from 'app/core/generated/circabc';

import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { ActionEmitterResult } from 'app/action-result';
import { LoginService } from 'app/core/login.service';
import { QuitGroupComponent } from 'app/me/quit-group/quit-group.component';
import { CreateUserComponent } from 'app/shared/create-user-wizard/create-user.component';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-roles',
  templateUrl: './roles.component.html',
  styleUrl: './roles.component.scss',
  preserveWhitespaces: true,
  imports: [
    HorizontalLoaderComponent,
    DataCyDirective,
    CreateUserComponent,
    RouterLink,
    QuitGroupComponent,
    SetTitlePipe,
    TranslocoModule,
  ],
})
export class RolesComponent implements OnInit {
  public memberships: InterestGroupProfile[] = [];
  public categories: Category[] = [];
  public loadingMemberships = false;
  public loadingCategories = false;
  public displayQuitGroup = false;
  public selectedGroup!: InterestGroup;
  public username!: string;
  public isAdmin = false;
  public isCicabcAdmin = false;
  public showWizard = false;

  public constructor(
    private loginService: LoginService,
    private userService: UserService,
    private i18nPipe: I18nPipe
  ) {}

  public async ngOnInit() {
    this.showWizard = false;
    const user: User = this.loginService.getUser();
    this.isAdmin =
      user.properties !== undefined && user.properties.isAdmin === 'true';
    this.isCicabcAdmin =
      user.properties !== undefined &&
      user.properties.isCircabcAdmin === 'true';
    this.username = this.loginService.getCurrentUsername();
    await this.loadMemberships(this.getUserId());
    await this.loadCategories(this.getUserId());
  }

  private async loadMemberships(userId: string) {
    this.loadingMemberships = true;
    this.memberships = await firstValueFrom(
      this.userService.getUserMembership(userId)
    );
    this.loadingMemberships = false;
  }

  private async loadCategories(userId: string) {
    this.loadingCategories = true;
    this.categories = await firstValueFrom(
      this.userService.getUserCategories(userId)
    );
    this.loadingCategories = false;
  }

  private getUserId(): string {
    return this.loginService.getCurrentUsername();
  }

  getNameOrTitle(item: InterestGroup | Profile | undefined): string {
    if (item === undefined) {
      return '';
    }

    let result = '';

    if (item.title && Object.keys(item.title).length > 0) {
      result = this.i18nPipe.transform(item.title);
    }

    if (result === '' && item.name) {
      result = item.name;
    }

    return result;
  }

  public showConfirmation(group: InterestGroup | undefined) {
    if (group) {
      this.displayQuitGroup = true;
      this.selectedGroup = group;
    }
  }

  public async refresh() {
    await this.loadMemberships(this.getUserId());
  }

  public async createUserWizardClosed(_result: ActionEmitterResult) {
    this.showWizard = false;
  }
}
