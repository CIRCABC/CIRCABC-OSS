import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  linkedSignal,
  resource,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { TranslocoModule } from '@jsverse/transloco';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  Applicant,
  InterestGroupService,
  MembersService,
  ProfileService,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { ReponsiveSubMenuComponent } from 'app/shared/reponsive-sub-menu/reponsive-sub-menu.component';
import { RequestComponent } from './request/request.component';

/**
 * Standalone Angular component that renders the membership-application review
 * screen for a single interest group.
 *
 * It loads the list of pending {@link Applicant}s for the group referenced by
 * the current route, together with the group's assignable {@link Profile}s, and
 * presents them one at a time through an embedded {@link RequestComponent}. The
 * component provides paging (next/previous) across applicants, tracks whether
 * the current user is already a member of the group, and exposes permission
 * checks used by the template to gate administrative actions.
 *
 * Key collaborators:
 * - {@link MembersService} to fetch pending applicants.
 * - {@link InterestGroupService} to resolve the current interest group.
 * - {@link ProfileService} to fetch assignable profiles.
 * - {@link UserService} / {@link LoginService} to determine current-user
 *   membership.
 * - {@link PermissionEvaluatorService} to evaluate directory-admin permissions.
 */
@Component({
  selector: 'cbc-applicants',
  templateUrl: './applicants.component.html',
  styleUrl: './applicants.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    ReponsiveSubMenuComponent,
    RouterLink,
    RequestComponent,
    TranslocoModule,
  ],
})
export class ApplicantsComponent {
  /** Provides access to the current route parameters (notably the group id). */
  private readonly route = inject(ActivatedRoute);
  /** API client used to retrieve the group's pending applicants. */
  private readonly membersService = inject(MembersService);
  /** API client used to resolve the current interest group. */
  private readonly groupsService = inject(InterestGroupService);
  /** API client used to retrieve the group's assignable profiles. */
  private readonly profileService = inject(ProfileService);
  /** Session service exposing the currently authenticated user. */
  private readonly loginService = inject(LoginService);
  /** API client used to look up the current user's group memberships. */
  private readonly userService = inject(UserService);
  /** Evaluates the current user's permissions against the current group. */
  private readonly permEvalService = inject(PermissionEvaluatorService);

  /** The current route params, as a signal. */
  private readonly routeParams = toSignal(this.route.params);

  /** Identifier of the interest group whose applicants are being reviewed. */
  public readonly igId = computed(() => this.routeParams()?.id ?? '');

  /**
   * Resource resolving the interest group referenced by {@link igId}. Errors
   * are swallowed (no dedicated error UI exists for this section) so the
   * screen remains usable.
   */
  private readonly currentGroupResource = resource({
    params: () => this.igId() || undefined,
    loader: async ({ params: id }) => {
      try {
        return await this.groupsService.getInterestGroupAsync({ id });
      } catch (error) {
        console.error('problem retrieving the interest group', error);
        return undefined;
      }
    },
  });

  /** The interest group resolved from the route. */
  public readonly currentGroup = this.currentGroupResource.value;

  /**
   * Resource determining whether the current user is already a member of
   * {@link currentGroup}. Mirrors {@link currentGroupResource}'s
   * loading/error state via `chain`, and is idle for guests.
   */
  private readonly alreadyMemberResource = resource({
    params: ({ chain }) => {
      const group = chain(this.currentGroupResource);
      return group && !this.loginService.isGuest() ? group.id : undefined;
    },
    loader: async ({ params: groupId }) => {
      const userId =
        this.loginService.getUser().userId === undefined
          ? 'guest'
          : this.loginService.getUser().userId;
      if (userId === undefined) {
        return false;
      }
      const currentUserMemberships =
        await this.userService.getUserMembershipAsync({ userId });
      return currentUserMemberships.some(
        (profile) => profile?.interestGroup?.id === groupId
      );
    },
  });

  /** Whether the current user is already a member of {@link currentGroup}. */
  public readonly alreadyMember = computed(
    () => this.alreadyMemberResource.value() ?? false
  );

  /**
   * Resource loading the pending applicants for the group identified by
   * {@link igId}.
   */
  private readonly applicantsResource = resource({
    params: () => this.igId() || undefined,
    loader: ({ params: id }) => this.membersService.getApplicantAsync({ id }),
  });

  /** Pending applicants for the current group. */
  public readonly applicants = computed(
    () => this.applicantsResource.value() ?? []
  );

  /** Whether an asynchronous load of applicants is in progress. */
  public readonly loading = this.applicantsResource.isLoading;

  /**
   * Resource loading the assignable profiles for the group identified by
   * {@link igId}, filtering out the `guest` and `EVERYONE` profiles. Errors
   * are logged and swallowed so the screen remains usable.
   */
  private readonly profilesResource = resource({
    params: () => this.igId() || undefined,
    loader: async ({ params: id }) => {
      try {
        const profiles = await this.profileService.getProfilesAsync({ id });
        return profiles.filter(
          (profile) => profile.name !== 'guest' && profile.name !== 'EVERYONE'
        );
      } catch (error) {
        console.error(error);
        console.error('problem retrieving the profiles');
        return [];
      }
    },
  });

  /** Assignable profiles for the current group (excluding guest/EVERYONE). */
  public readonly profiles = computed(
    () => this.profilesResource.value() ?? []
  );

  /** 1-based index of the currently displayed applicant within {@link applicants}. */
  public readonly currentIndex = linkedSignal({
    source: this.igId,
    computation: () => 1,
  });

  /** Applicant currently displayed for review. */
  public readonly currentApplicant = linkedSignal(() => {
    const applicants = this.applicants();
    return applicants.length > 0
      ? applicants[this.currentIndex() - 1]
      : undefined;
  });

  /**
   * Paging animation/direction state: `'init'` on first selection, `'inc'`
   * when moving forward, `'dec'` when moving backward.
   */
  public readonly state = linkedSignal({
    source: this.igId,
    computation: () => 'init' as 'init' | 'inc' | 'dec',
  });

  /**
   * Advances to the next applicant, wrapping back to the first applicant when
   * the end of the list is passed, and updates {@link state} to reflect the
   * paging direction.
   */
  nextPage() {
    this.currentIndex.update((index) => index + 1);
    if (this.currentIndex() > this.applicants().length) {
      this.currentIndex.set(1);
      this.state.set('dec');
    } else {
      this.state.set('inc');
    }
    this.currentApplicant.set(this.applicants()[this.currentIndex() - 1]);
  }

  /**
   * Moves to the previous applicant, wrapping to the last applicant when moving
   * before the first, and updates {@link state} to reflect the paging
   * direction.
   */
  previousPage() {
    this.currentIndex.update((index) => index - 1);
    if (this.currentIndex() <= 0) {
      this.currentIndex.set(this.applicants().length);
      this.state.set('inc');
    } else {
      this.state.set('dec');
    }

    this.currentApplicant.set(this.applicants()[this.currentIndex() - 1]);
  }

  /**
   * Handles completion of an applicant's request (accept/reject) emitted by the
   * embedded {@link RequestComponent}. Removes the processed applicant from
   * {@link applicants} and adjusts {@link currentIndex}/{@link currentApplicant}
   * to the next available applicant.
   *
   * @param _applicant The applicant whose request was processed (unused).
   */
  onRequestProcessed(_applicant: Applicant) {
    const remaining = [...this.applicants()];
    remaining.splice(this.currentIndex() - 1, 1);
    this.applicantsResource.value.set(remaining);
    if (this.currentIndex() > this.applicants().length) {
      this.currentIndex.set(this.applicants().length - 1);
    }
    this.currentApplicant.set(this.applicants()[this.currentIndex() - 1]);
  }

  /**
   * Indicates whether the current user is already a member of the current
   * group.
   *
   * @returns `true` when the current user is a member, otherwise `false`.
   */
  isMember() {
    return this.alreadyMember();
  }

  /**
   * Evaluates whether the current user has directory-administration rights over
   * {@link currentGroup}.
   *
   * @returns `true` when the user is a directory admin, otherwise `false`.
   */
  public isDirAdmin(): boolean {
    const group = this.currentGroup();
    return group ? this.permEvalService.isDirAdmin(group) : false;
  }

  /**
   * Evaluates whether the current user is allowed to manage members of
   * {@link currentGroup}.
   *
   * @returns `true` when the user can manage members, otherwise `false`.
   */
  public isDirManageMembers(): boolean {
    const group = this.currentGroup();
    return group ? this.permEvalService.isDirManageMembers(group) : false;
  }
}
