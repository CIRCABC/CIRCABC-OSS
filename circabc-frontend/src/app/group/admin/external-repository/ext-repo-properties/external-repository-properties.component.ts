import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ExternalRepositoryService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-external-repository-properties',
  templateUrl: './external-repository-properties.component.html',
  styleUrls: ['./external-repository-properties.component.scss'],
  preserveWhitespaces: true,
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
      let requestedOn;
      const externalRepo = repos.find((r) => r.name === name);
      if (externalRepo && externalRepo.registrationDate) {
        enabled = true;
        requestedOn = new Date(externalRepo.registrationDate);
      }
      this.addRepo(name, enabled, requestedOn);
    });
  }

  private addRepo(name: string, enabled: boolean, requestedOn?: Date) {
    const repos = this.form.controls.repos as FormArray;
    repos.push(
      this.fb.group({
        name: name,
        requestedOn: requestedOn,
        enabled: enabled,
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
}
