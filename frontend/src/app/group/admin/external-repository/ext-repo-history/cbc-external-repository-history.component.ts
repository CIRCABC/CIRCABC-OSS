import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  resource,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { AresBridgeHelperService } from 'app/core/ares-bridge-helper.service';
import { LoginService } from 'app/core/login.service';
import { environment } from 'environments/environment';

/**
 * Standalone Angular component that renders the external repository (ARES bridge)
 * history for a given interest group.
 *
 * It derives the group id from the current route as a signal and loads the
 * group's external repository log entries through a {@link resource} wrapping
 * {@link AresBridgeHelperService}, reactively re-fetching whenever the route's
 * `id` param changes. It also builds outbound links to the corresponding ARES
 * documents and to the matching CIRCABC library documents, and exposes whether
 * the current user is an external (non-internal) user so the template can adapt
 * accordingly.
 *
 * Key collaborators:
 * - {@link ActivatedRoute} — provides the `id` route parameter (group id).
 * - {@link AresBridgeHelperService} — fetches the external repository log.
 * - {@link LoginService} — determines the current user's domain.
 */
@Component({
  selector: 'cbc-external-repository-history',
  templateUrl: './cbc-external-repository-history.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslocoModule],
})
export class ExternalRepositoryHistoryComponent {
  /** Route service used to read the `id` (group id) parameter. */
  private readonly route = inject(ActivatedRoute);
  /** Helper service used to retrieve the group's external repository log. */
  private readonly aresBridgeHelperService = inject(AresBridgeHelperService);
  /** Service used to obtain the currently authenticated user. */
  private readonly loginService = inject(LoginService);

  /** The current route params, as a signal. */
  private readonly routeParams = toSignal(this.route.params);

  /** Identifier of the group whose external repository history is displayed. */
  private readonly groupId = computed(
    () => this.routeParams()?.id || undefined
  );

  /**
   * Resource that loads the external repository log for the current group.
   * Idle (loader not called) while no group `id` is present in the route.
   */
  private readonly groupLogResource = resource({
    params: () => this.groupId(),
    loader: async ({ params: id }) => {
      try {
        return await this.aresBridgeHelperService.groupLog(id);
      } catch (e) {
        console.error(e);
        return [];
      }
    },
    defaultValue: [],
  });

  /** The external repository log entries loaded for the current group. */
  public readonly groupLog = this.groupLogResource.value;

  /** True when the current user belongs to the `external` domain. */
  public readonly isExternalUser = computed(
    () => this.loginService.getUser().properties?.domain === 'external'
  );

  /**
   * Builds the absolute URL to the ARES document viewer for a given document.
   *
   * @param id - The ARES document identifier, or `undefined` if unavailable.
   * @returns The ARES bridge URL pointing to the document's detail view.
   */
  public aresDocumentLink(id: string | undefined): string {
    return `${environment.aresBridgeServer}/Ares/document/show.do?documentId=${id}`;
  }

  /**
   * Builds the CIRCABC library details URL for a node within the current group.
   *
   * @param nodeId - The identifier of the CIRCABC library node.
   * @returns The URL to the node's details page in the current group's library.
   */
  public circabcDocumentLink(nodeId: string): string {
    return `${environment.serverURL}${environment.baseHref.substring(1)}group/${
      this.groupId() ?? ''
    }/library/${nodeId}/details`;
  }
}
