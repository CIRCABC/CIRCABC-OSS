import { Component, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { TranslocoService } from '@ngneat/transloco';

import {
  Category,
  CategoryService,
  Header,
  HeaderService,
  InterestGroup,
  Node as ModelNode,
  NodesService,
  User,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { IndexedInterestGroup } from 'app/core/ui-model/index';
import { sortI18nProperty } from 'app/core/util';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { AnalyticsService } from 'app/core//analytics.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-explorer',
  templateUrl: './explorer.component.html',
  styleUrls: ['./explorer.component.scss'],
  preserveWhitespaces: true,
})
export class ExplorerComponent implements OnInit {
  public headers: Header[] = [];
  public categories: Category[] = [];
  public interestGroups: InterestGroup[] = [];
  public groupsWithIndex!: {
    [key: string]: IndexedInterestGroup[];
  };

  public uiMessage: string | undefined;

  public loadingHeaders!: boolean;
  public loadingCategories!: boolean;
  public loadingIGs!: boolean;
  public preparing = false;
  public searchForm!: FormGroup;
  public currentHeader: Header | undefined;
  public currentCategory: ModelNode | undefined;
  public showRequestModal = false;

  public step = 'headers';

  public constructor(
    private router: Router,
    private route: ActivatedRoute,
    private headerService: HeaderService,
    private categoryService: CategoryService,
    private uiMessageService: UiMessageService,
    private translateService: TranslocoService,
    private loginService: LoginService,
    private i18nPipe: I18nPipe,
    private nodesService: NodesService,
    private analyticsService: AnalyticsService
  ) {}

  public async ngOnInit() {
    try {
      this.preparing = true;
      const unsortedHeaders = await firstValueFrom(
        this.headerService.getHeaders(undefined, true)
      );
      // eslint-disable-next-line max-len
      unsortedHeaders.sort((a: Header, b: Header) =>
        sortI18nProperty(
          a.description,
          b.description,
          this.getCurrentLang(),
          this.getDefaultLang(),
          a.name,
          b.name
        )
      );
      this.headers = unsortedHeaders;

      const headerFromSession = sessionStorage.getItem('currentHeader');
      let headerFromQuery;
      let categoryFromQuery;
      let reset = false;

      this.route.queryParamMap.subscribe((queryParams) => {
        if (queryParams.has('start')) {
          reset = true;
        }

        if (queryParams.has('headerId')) {
          headerFromQuery = queryParams.get('headerId');
        }

        if (queryParams.has('categoryId')) {
          categoryFromQuery = queryParams.get('categoryId');
        }
      });

      if (headerFromQuery !== undefined && headerFromQuery !== '') {
        this.currentHeader = await firstValueFrom(
          this.headerService.getHeader(headerFromQuery)
        );
        this.step = 'categories';
        sessionStorage.removeItem('currentCategory');
      } else if (
        headerFromSession !== undefined &&
        headerFromSession !== null &&
        !reset
      ) {
        this.currentHeader = JSON.parse(headerFromSession);
        this.step = 'categories';
      }

      const categoryFromSession = sessionStorage.getItem('currentCategory');

      if (categoryFromQuery !== undefined && categoryFromQuery !== '') {
        this.currentCategory = await firstValueFrom(
          this.categoryService.getCategory(categoryFromQuery)
        );
        this.step = 'groups';
      } else if (
        categoryFromSession !== undefined &&
        categoryFromSession !== null &&
        !reset
      ) {
        this.currentCategory = JSON.parse(categoryFromSession);
        this.step = 'groups';
      }

      this.preparing = false;

      if (this.step === 'groups' && this.currentCategory !== undefined) {
        if (this.currentCategory.id && this.currentCategory.name) {
          const categ: Category = {
            id: this.currentCategory.id,
            name: this.currentCategory.name,
          };
          await this.listInterestGroups(categ);
        }
      } else if (
        this.step === 'categories' &&
        this.currentHeader !== undefined
      ) {
        await this.listCategories(this.currentHeader);
      }
    } catch (err) {
      this.uiMessageService.addErrorMessage(err);
    }
  }

  public filterGroups(search: string) {
    if (search) {
      let i = 1;
      this.groupsWithIndex = {};
      for (const ig of this.interestGroups) {
        const indexGroup = { ...ig, ...{ index: i } };

        if (
          (ig.name &&
            ig.name.toLowerCase().indexOf(search.toLowerCase()) !== -1) ||
          (ig.title && this.containsValueInMap(ig.title, search)) ||
          (ig.description && this.containsValueInMap(ig.description, search))
        ) {
          this.addGroupToMap(indexGroup);
          i += 1;
        }
      }
      this.analyticsService.trackSiteSearch(search, 'group-explorer', i);
    } else {
      let i = 1;
      this.groupsWithIndex = {};
      for (const ig of this.interestGroups) {
        const indexGroup = { ...ig, ...{ index: i } };
        this.addGroupToMap(indexGroup);
        i += 1;
      }
    }
  }

  private addGroupToMap(group: InterestGroup | IndexedInterestGroup) {
    const letter = this.getGroupFirstLetter(group).toUpperCase();
    if (Object.keys(this.groupsWithIndex).indexOf(letter) !== -1) {
      this.groupsWithIndex[letter].push(group);
    } else {
      this.groupsWithIndex[letter] = [group];
    }
  }

  public getKeysOfGroups(): string[] {
    if (this.groupsWithIndex) {
      return Object.keys(this.groupsWithIndex).sort();
    } else {
      return [];
    }
  }

  public getGroupInIndex(letterKey: string): IndexedInterestGroup[] {
    if (this.groupsWithIndex) {
      return this.groupsWithIndex[letterKey];
    } else {
      return [];
    }
  }

  private containsValueInMap(
    map: { [key: string]: string },
    search: string
  ): boolean {
    const keys = Object.keys(map);
    let result = false;
    for (const key of keys) {
      if (map[key].toLowerCase().indexOf(search.toLowerCase()) !== -1) {
        result = true;
        break;
      }
    }

    return result;
  }

  public async listCategories(header: Header | undefined) {
    if (header === undefined) {
      return;
    }
    this.step = 'categories';
    this.currentHeader = header;
    sessionStorage.setItem('currentHeader', JSON.stringify(header));
    sessionStorage.removeItem('currentCategory');

    if (this.loadingIGs || this.loadingCategories) {
      return;
    }
    this.loadingCategories = true;
    this.uiMessage = undefined;

    try {
      if (this.currentHeader.id) {
        const unsortedCategories = await firstValueFrom(
          this.headerService.getCategoriesByHeaderId(
            this.currentHeader.id,
            this.getCurrentLang()
          )
        );
        unsortedCategories.sort((a: Category, b: Category) =>
          sortI18nProperty(
            a.title,
            b.title,
            this.getCurrentLang(),
            this.getDefaultLang(),
            a.name,
            b.name
          )
        );
        this.categories = unsortedCategories;

        this.interestGroups = [];
      }
    } catch (err) {
      this.uiMessageService.addErrorMessage(err);
    }
    this.loadingCategories = false;
  }

  public async listInterestGroups(category: ModelNode | undefined) {
    if (category === undefined) {
      return;
    }

    this.step = 'groups';
    this.currentCategory = category;
    sessionStorage.setItem('currentCategory', JSON.stringify(category));

    if (this.loadingIGs) {
      return;
    }
    this.uiMessage = undefined;
    this.loadingIGs = true;

    try {
      if (category.id) {
        this.currentCategory = await firstValueFrom(
          this.nodesService.getNode(category.id)
        );

        const unsortedInterestGroups = await firstValueFrom(
          this.categoryService.getInterestGroupsByCategoryId(
            category.id,
            this.getCurrentLang()
          )
        );
        unsortedInterestGroups.sort((a: InterestGroup, b: InterestGroup) =>
          sortI18nProperty(
            a.title,
            b.title,
            this.getDefaultLang(),
            this.getCurrentLang(),
            a.name,
            b.name
          )
        );
        this.interestGroups = unsortedInterestGroups;

        let i = 1;
        this.groupsWithIndex = {};
        for (const ig of this.interestGroups) {
          const indexGroup = { ...ig, ...{ index: i } };
          this.addGroupToMap(indexGroup);
          i += 1;
        }
      }
    } catch (err) {
      this.uiMessageService.addErrorMessage(err);
    }
    this.loadingIGs = false;
  }

  public openInterestGroup(ig: InterestGroup): void {
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    this.router.navigate(['/group', ig.id]);
  }

  private getCurrentLang(): string {
    return this.translateService.getActiveLang();
  }

  private getDefaultLang(): string {
    return this.translateService.getDefaultLang();
  }

  public isAdminOrCircabcAdmin(): boolean {
    const user: User = this.loginService.getUser();
    if (
      user.properties === null ||
      user.userId === '' ||
      user.userId === 'guest'
    ) {
      return false;
    }
    return (
      user.properties !== undefined &&
      (user.properties.isAdmin === 'true' ||
        user.properties.isCircabcAdmin === 'true')
    );
  }

  isLoggedIn(): boolean {
    return !this.loginService.isGuest();
  }

  public getCategoryGroupDescription(category: ModelNode): string {
    if (category.name === undefined) {
      return '';
    }

    let result = category.name;

    if (category.title) {
      const title = this.i18nPipe.transform(category.title);
      if (title !== '' && title !== undefined) {
        result = title;
      }
    }

    return result;
  }
  public getInterestGroupDescription(interestGroup: InterestGroup): string {
    let result = '';
    if (interestGroup.title !== undefined) {
      result = this.i18nPipe.transform(interestGroup.title);
      if (result === '') {
        result = this.i18nPipe.transform(interestGroup.title);
      }
    }
    if (result === '') {
      result = interestGroup.name;
    }
    return result;
  }

  isCategoryAdmin(): boolean {
    if (
      this.currentCategory &&
      this.currentCategory.permissions &&
      this.currentCategory.permissions.CircaCategoryAdmin === 'ALLOWED'
    ) {
      return true;
    }
    return false;
  }

  startRequestModal() {
    this.showRequestModal = true;
  }

  public isStepHeaders(): boolean {
    return this.step === 'headers';
  }

  public isStepCategories(): boolean {
    return this.step === 'categories';
  }

  public isStepGroups(): boolean {
    return this.step === 'groups';
  }

  public reset() {
    this.step = 'headers';
    sessionStorage.removeItem('currentCategory');
    sessionStorage.removeItem('currentHeader');
    this.currentCategory = undefined;
    this.currentHeader = undefined;
  }

  public getHeaderImgName(header: Header) {
    if (
      header.name.indexOf('Justice') !== -1 &&
      header.name.indexOf('European') !== -1
    ) {
      return 'img/court-of-justice-128.png';
    } else if (
      header.name.indexOf('Commission') !== -1 &&
      header.name.indexOf('European') !== -1
    ) {
      return 'img/LOGO-CE_Vertical_EN_128.png';
    } else if (
      header.name.indexOf('Parliament') !== -1 &&
      header.name.indexOf('European') !== -1
    ) {
      return 'img/EP-logo-128.png';
    } else if (
      header.name.indexOf('Programmes') !== -1 &&
      header.name.indexOf('European') !== -1
    ) {
      return 'img/euro-institutions-128.png';
    }

    return 'img/LOGO_circabc_header-128.png';
  }

  public hasCategoryLogo(category: Category): boolean {
    return !(category.logoRef === undefined || category.logoRef === '');
  }

  public hasModelNodeLogo(category: ModelNode): boolean {
    if (category && category.properties) {
      return !(
        category.properties.logoRef === undefined ||
        category.properties.logoRef === ''
      );
    }
    return false;
  }

  public getLogoRef(category: ModelNode): string {
    if (category && category.properties) {
      const parts = category.properties.logoRef.split('/');
      return parts[parts.length - 1];
    }
    return '';
  }

  getGroupFirstLetter(group: InterestGroup | IndexedInterestGroup): string {
    if (group.title && Object.keys(group.title).length > 0) {
      return this.i18nPipe.transform(group.title).substring(0, 1);
    } else {
      return group.name.substring(0, 1);
    }
  }

  public shouldDisplayMenu() {
    return (
      this.isAdminOrCircabcAdmin() ||
      (this.isCategoryAdmin() && this.currentCategory)
    );
  }
}
