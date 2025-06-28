import { Component, OnInit } from '@angular/core';
import {
  FormArray,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
} from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { ExternalRepositoryService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { firstValueFrom } from 'rxjs';

import { DatePipe, NgFor } from '@angular/common';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

@Component({
  selector: 'cbc-external-repository-properties',
  templateUrl: './external-repository-properties.component.html',
  styleUrl: './external-repository-properties.component.scss',
  preserveWhitespaces: true,
  imports: [
    ReactiveFormsModule,
    NgFor,
    MatSlideToggleModule,
    SpinnerComponent,
    DatePipe,
    TranslocoModule,
  ],
})
export class ExternalRepositoryPropertiesComponent implements OnInit {
  public igId!: string;
  public saving = false;

  public form!: FormGroup;
  public isExternalUser = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private externalRepositoryService: ExternalRepositoryService,
    private loginService: LoginService
  ) {}

  async ngOnInit() {
    this.isExternalUser =
      this.loginService.getUser().properties?.domain === 'external';
    this.form = this.fb.group({
      repos: this.fb.array([]),
    });
    this.route.params.subscribe(async (params) => {
      this.igId = params.id;
      await this.load();
    });
  }

  private async load() {
    const availableRepos = await firstValueFrom(
      this.externalRepositoryService.getAvailableExternalRepositories()
    );
    const repos = await firstValueFrom(
      this.externalRepositoryService.getExternalRepositories(this.igId)
    );
    const reposFormArray = this.form.controls.repos as FormArray;
    reposFormArray.clear();
    availableRepos.forEach((repo: string) => {
      const name = repo;
      let enabled = false;
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      let requestedOn: any;
      const externalRepo = repos.find((r) => r.name === name);
      if (externalRepo?.registrationDate) {
        enabled = true;
        requestedOn = new Date(externalRepo.registrationDate);
      }
      this.addRepo(name, enabled, requestedOn);
    });
  }

  private addRepo(name: string, enabled: boolean, requestedOn?: Date) {
    const repos = this.form.controls.repos as FormArray;
    const enabledControl = this.fb.control(enabled);
    if (this.isExternalUser) {
      enabledControl.disable();
    }
    repos.push(
      this.fb.group({
        name: name,
        requestedOn: requestedOn,
        enabled: enabledControl,
      })
    );
  }

  public async save() {
    this.saving = true;
    const repos = await firstValueFrom(
      this.externalRepositoryService.getExternalRepositories(this.igId)
    );
    const reposModel = this.form.value.repos as {
      name: string;
      requestedOn?: Date;
      enabled: boolean;
    }[];
    reposModel.forEach(async (repo) => {
      const externalRepo = repos.find((r) => r.name === repo.name);
      if (repo.enabled && externalRepo === undefined) {
        await firstValueFrom(
          this.externalRepositoryService.addExternalRepositories(
            this.igId,
            repo.name
          )
        );
      }
      if (!repo.enabled && externalRepo !== undefined) {
        await firstValueFrom(
          this.externalRepositoryService.deleteExternalRepository(
            this.igId,
            repo.name
          )
        );
      }
      await this.load();
    });
    this.saving = false;
  }

  public async cancel() {
    await this.load();
  }

  public toggleChange(i: number) {
    const reposFormArray = this.form.controls.repos as FormArray;
    reposFormArray.at(i).value.enabled = !reposFormArray.at(i).value.enabled;
  }
}
