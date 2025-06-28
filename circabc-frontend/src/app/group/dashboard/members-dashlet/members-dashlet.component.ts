import { Component, OnInit, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  type InterestGroup,
  MemberCount,
  MembersService,
} from 'app/core/generated/circabc';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-members-dashlet',
  templateUrl: './members-dashlet.component.html',
  styleUrl: './members-dashlet.component.scss',
  preserveWhitespaces: true,
  imports: [HorizontalLoaderComponent, RouterLink, TranslocoModule],
})
export class MembersDashletComponent implements OnInit {
  readonly group = input.required<InterestGroup>();

  public loading = false;
  public restCallError = false;
  public nbMembers: number | undefined = 0;
  public nbApplicants = 0;

  constructor(
    private membersService: MembersService,
    private permEvalService: PermissionEvaluatorService
  ) {}

  async ngOnInit() {
    this.loading = true;
    this.restCallError = false;
    try {
      const group = this.group();
      if (group.id) {
        const memberCount: MemberCount = await firstValueFrom(
          this.membersService.getMemberCount(group.id)
        );
        this.nbMembers = memberCount.count;

        if (this.isDirAdmin()) {
          const applicants = await firstValueFrom(
            this.membersService.getApplicant(group.id)
          );
          this.nbApplicants = applicants.length;
        }
      }
    } catch (_error) {
      console.error('problem retrieving the number of members');
      this.restCallError = true;
    }

    this.loading = false;
  }

  public isDirAdmin(): boolean {
    return this.permEvalService.isDirAdmin(this.group());
  }
}
