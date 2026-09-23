import { inject, Service } from '@angular/core';
import { InterestGroup } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';

/**
 * Application-wide singleton service that tracks which interest groups the
 * current user has already visited during the active session.
 *
 * The set of visited group identifiers is kept purely in memory and is not
 * persisted, so it is reset whenever the application is reloaded. This is
 * typically used to drive UI behaviour that should only occur on a user's
 * first visit to a group (for example showing a welcome banner or triggering
 * a one-off action).
 *
 * Key collaborators:
 * - {@link LoginService} — consulted to avoid recording visits for guest
 *   (unauthenticated) users.
 */
@Service()
export class VisitedGroupService {
  /** Service used to determine whether the current user is a guest. */
  private readonly loginService = inject(LoginService);

  /**
   * In-memory collection of interest group identifiers that have been visited
   * during the current session. Not persisted across reloads.
   */
  private readonly visitedGroups: Set<string> = new Set([]);

  /**
   * Records that the given interest group has been visited by the current
   * user.
   *
   * Visits are only recorded for authenticated (non-guest) users; guest
   * visits are ignored. Identifiers that are already present are not added
   * again.
   *
   * @param ig The interest group being visited; its `id` is used as the key.
   */
  public visitGroup(ig: InterestGroup) {
    if (!this.loginService.isGuest()) {
      if (!this.visitedGroups.has(ig.id as string)) {
        this.visitedGroups.add(ig.id as string);
      }
    }
  }

  /**
   * Determines whether an interest group has already been visited during the
   * current session.
   *
   * @param id The identifier of the interest group to check.
   * @returns `true` if the group has been recorded as visited, otherwise
   * `false`.
   */
  public isVisited(id: string): boolean {
    return this.visitedGroups.has(id);
  }
}
