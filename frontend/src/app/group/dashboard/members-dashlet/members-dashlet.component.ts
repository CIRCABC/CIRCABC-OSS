import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
  resource,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import { type InterestGroup, MembersService } from 'app/core/generated/circabc';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';

/**
 * Dashboard dashlet that summarises the membership of an interest group.
 *
 * Renders the total number of members for the current group and, when the
 * signed-in user is a directory administrator, the number of pending
 * membership applicants. The template shows a horizontal loader while the
 * counts are being fetched and exposes router links to the relevant member
 * management views.
 *
 * Collaborators:
 * - {@link MembersService} to retrieve the member count and applicant list.
 * - {@link PermissionEvaluatorService} to determine whether the current user
 *   is a directory administrator of the group.
 */
@Component({
  selector: 'cbc-members-dashlet',
  templateUrl: './members-dashlet.component.html',
  styleUrl: './members-dashlet.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [HorizontalLoaderComponent, RouterLink, TranslocoModule],
})
export class MembersDashletComponent {
  /** Generated API client used to fetch member counts and applicants. */
  private readonly membersService = inject(MembersService);
  /** Service used to evaluate the current user's permissions on the group. */
  private readonly permEvalService = inject(PermissionEvaluatorService);

  /**
   * Required input carrying the interest group whose membership figures are
   * displayed by this dashlet.
   */
  readonly group = input.required<InterestGroup>();

  /**
   * Membership figures for the current group, loaded reactively. Fetches the
   * total member count and, when the current user is a directory administrator,
   * the number of pending applicants. Stays idle while the group has no id and
   * re-runs whenever {@link group} changes.
   */
  private readonly countsResource = resource({
    params: () => this.group().id || undefined,
    loader: async ({ params: id }) => {
      const memberCount = await this.membersService.getMemberCountAsync({ id });
      let applicants = 0;
      if (this.isDirAdmin()) {
        const list = await this.membersService.getApplicantAsync({ id });
        applicants = list.length;
      }
      return { members: memberCount.count, applicants };
    },
  });

  /** Whether the membership figures are currently being loaded. */
  public readonly loading = this.countsResource.isLoading;
  /** Whether a REST call failed while retrieving the membership figures. */
  public readonly restCallError = computed(
    () => this.countsResource.status() === 'error'
  );
  /** Total number of members of the group, or `undefined` if unknown. */
  public readonly nbMembers = computed<number | undefined>(() =>
    this.countsResource.hasValue()
      ? this.countsResource.value().members
      : undefined
  );
  /** Number of pending membership applicants (only relevant for directory admins). */
  public readonly nbApplicants = computed(() =>
    this.countsResource.hasValue() ? this.countsResource.value().applicants : 0
  );

  /**
   * Indicates whether the current user is a directory administrator of the
   * group displayed by this dashlet.
   *
   * @returns `true` if the current user has directory administrator rights on
   * the group, otherwise `false`.
   */
  public isDirAdmin(): boolean {
    return this.permEvalService.isDirAdmin(this.group());
  }
}
