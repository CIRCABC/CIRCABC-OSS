import { Inject, Optional, Component, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Params } from '@angular/router';
import {
  AuditActivity,
  AuditService,
  BASE_PATH,
  LogSearchResult,
  MembersService,
  PagedUserProfile,
} from 'app/core/generated/circabc';
import { SaveAsService } from 'app/core/save-as.service';
import { IdName } from 'app/core/ui-model';
import { ValidationService } from 'app/core/validation.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-log',
  templateUrl: 'log.component.html',
  preserveWhitespaces: true,
})
export class LogComponent implements OnInit {
  public form!: FormGroup;
  public groupId!: string;
  private pagedUserProfile!: PagedUserProfile;
  private allActivities!: AuditActivity[];
  public users!: IdName[];
  public activities: IdName[] = [];
  public services!: IdName[];
  public searchResults!: LogSearchResult[];
  public loading = false;
  public preloading = false;

  private basePath!: string;
  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private membersService: MembersService,
    private auditService: AuditService,
    private saveAsService: SaveAsService,
    @Optional()
    @Inject(BASE_PATH)
    basePath: string
  ) {
    if (basePath) {
      this.basePath = basePath;
    }
  }

  ngOnInit() {
    this.form = this.fb.group(
      {
        users: [''],
        services: [''],
        activities: [''],
        dateFrom: [
          this.get7DaysAgo(),
          [Validators.required, ValidationService.futureDateValidator],
        ],
        dateTo: [
          new Date(),
          [Validators.required, ValidationService.futureDateValidator],
        ],
      },
      {
        validators: ValidationService.dateLessThan('dateFrom', 'dateTo'),
        updateOn: 'change',
      }
    );
    this.route.params.subscribe(async (params) => {
      this.preloading = true;
      await this.loadPageData(params);
      this.preloading = false;
    });
  }

  private async loadPageData(params: Params) {
    this.groupId = params.id;
    this.pagedUserProfile = await this.loadUsers();
    this.createUsers();
    this.allActivities = await this.loadActivities();
    this.createServices();
    this.createActivities();
  }

  private createActivities(service?: string) {
    let duplicateActivities: string[] = [];
    if (service) {
      duplicateActivities = this.allActivities
        .filter((activity) => activity.service === service)
        .map((activity) => activity.name);
    } else {
      duplicateActivities = this.allActivities.map((activity) => activity.name);
    }

    const uniqueActivities = duplicateActivities.filter((elem, pos) => {
      return duplicateActivities.indexOf(elem) === pos;
    });

    uniqueActivities.sort();
    this.activities = uniqueActivities.map((activity) => {
      return { id: activity, name: activity };
    });
    this.activities.unshift({ id: '', name: 'All' });
  }

  private createServices() {
    const duplicateServices = this.allActivities.map(
      (activity) => activity.service
    );
    const uniqueServices = duplicateServices.filter((elem, pos) => {
      return duplicateServices.indexOf(elem) === pos;
    });
    uniqueServices.sort();
    this.services = uniqueServices.map((value) => {
      return { id: value, name: value };
    });
    this.services.unshift({ id: '', name: 'All' });
  }

  private createUsers() {
    if (this.pagedUserProfile.data && this.pagedUserProfile.data.length > 0) {
      this.users = this.pagedUserProfile.data.map((userProfile) => {
        if (
          userProfile.user &&
          userProfile.user.userId &&
          userProfile.user.firstname &&
          userProfile.user.lastname
        ) {
          return {
            id: userProfile.user.userId,
            name: `${userProfile.user.firstname} ${userProfile.user.lastname}`,
          };
        } else {
          return { id: '', name: '' };
        }
      });
      this.users.unshift({ id: '', name: 'All' });
    }
  }

  private get7DaysAgo(): Date {
    const milliseconds = Date.now() - 7 * 24 * 60 * 60 * 1000;
    return new Date(milliseconds);
  }

  private async loadUsers() {
    return firstValueFrom(
      this.membersService.getMembers(
        this.groupId,
        undefined,
        undefined,
        undefined,
        undefined,
        undefined,
        ''
      )
    );
  }
  private async loadActivities() {
    return firstValueFrom(
      this.auditService.getAllAuditActivities(this.groupId)
    );
  }

  public serviceClick(_id: string) {
    const service = this.form.controls.services.value;
    this.createActivities(service);
    this.form.controls.activities.setValue('');
  }

  public export(exportCode: string) {
    this.loading = true;
    const from = new Date(
      new Date(this.form.controls.dateFrom.value).setHours(0, 0, 0, 0)
    ).toISOString();
    const to = new Date(
      new Date(this.form.controls.dateTo.value).setHours(23, 59, 59, 999)
    ).toISOString();
    const userId = this.form.controls.users.value;
    const service = this.form.controls.services.value;
    const activity = this.form.controls.activities.value;
    const url = `${this.basePath}/audit/${this.groupId}?userId=${userId}&service=${service}&activity=${activity}&from=${from}&to=${to}&format=${exportCode}`;
    // eslint-disable-next-line no-console
    console.log(url);
    const name = `AuditLog.${exportCode}`;
    this.saveAsService.saveUrlAs(url, name);
    this.loading = false;
  }

  public async search() {
    if (this.form.valid) {
      this.loading = true;
      const from = new Date(
        new Date(this.form.controls.dateFrom.value).setHours(0, 0, 0, 0)
      );
      const to = new Date(
        new Date(this.form.controls.dateTo.value).setHours(23, 59, 59, 999)
      );
      this.searchResults = await firstValueFrom(
        this.auditService.getAudits(
          this.groupId,
          from.toISOString(),
          to.toISOString(),
          this.form.controls.users.value,
          this.form.controls.services.value,
          this.form.controls.activities.value
        )
      );
      this.loading = false;
    }
  }

  get dateFrom(): AbstractControl {
    return this.form.controls.dateFrom;
  }

  get dateTo(): AbstractControl {
    return this.form.controls.dateTo;
  }
}
