import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
} from '@angular/core';
import { ActivatedRoute, Data, Router } from '@angular/router';
import {
  HeaderService,
  InterestGroup,
  Node as ModelNode,
  NodesService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';

/**
 * Routing dispatcher component for CIRCABC nodes.
 *
 * `BrowseComponent` has no visible UI of its own: it acts as a redirection
 * hub that resolves a {@link ModelNode} (provided through the route's
 * `data.node` resolver) and forwards the user to the appropriate application
 * view. Depending on the resolved node it navigates to an interest group
 * dashboard, one of a group's services (library, agenda, information, forum,
 * members), an explore view for a category or header, or an access-denied
 * page.
 *
 * Key collaborators:
 * - {@link Router} / {@link ActivatedRoute} to read the resolved node and
 *   perform navigation.
 * - {@link NodesService} to look up the interest group owning the node.
 * - {@link HeaderService} to resolve headers when the node has no name.
 * - {@link LoginService} to detect guest sessions.
 * - {@link RedirectionService} to trigger a login redirect for guests.
 */
@Component({
  selector: 'cbc-browse',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './browse.component.html',
})
export class BrowseComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly nodesService = inject(NodesService);
  private readonly loginService = inject(LoginService);
  private readonly redirectionService = inject(RedirectionService);
  private readonly headerService = inject(HeaderService);

  /** The node resolved from the route data that drives the navigation logic. */
  private node!: ModelNode;

  /**
   * Angular lifecycle hook that subscribes to the route data to obtain the
   * resolved {@link node} and dispatches the user to the correct view.
   *
   * Resolution flow:
   * - If the node has no name, delegates to {@link nodeNameIsUndefined} to
   *   treat it as a header (or deny access).
   * - Otherwise, if the node has an id, tries to fetch its owning interest
   *   group. When a group is found it delegates to {@link groupNavigation};
   *   when no group is found but the node is a category master group, it
   *   navigates to the explore view for that category.
   *
   * @returns A promise that resolves once the initial dispatch has been set up.
   */
  ngOnInit() {
    this.route.data.subscribe(async (value: Data) => {
      this.node = value.node;
      if (this.node.name === undefined) {
        return await this.nodeNameIsUndefined();
      }

      if (this.node.id) {
        let group = null;
        try {
          group = await this.nodesService.getGroupAsync({ id: this.node.id });
        } catch (error) {
          console.error("Can't get group", error);
        }
        if (group !== null) {
          this.groupNavigation(group);
        } else if (
          this.node?.properties?.circaCategoryMasterGroup !== undefined
        ) {
          this.router.navigate(['/explore'], {
            queryParams: { categoryId: this.node.id },
          });
        }
      }
    });
  }

  /**
   * Handles nodes that have no name by attempting to resolve them as a header.
   *
   * If a header is found, navigates to the explore view scoped to that header.
   * If no header is found, guests are sent through the redirection service and
   * the user is routed to the access-denied page.
   *
   * @returns A promise that resolves once navigation has been triggered.
   */
  private async nodeNameIsUndefined() {
    let header = null;
    try {
      header = await this.headerService.getHeaderAsync({
        id: this.node.id as string,
      });
    } catch (error) {
      console.error("Can't get header", error);
    }

    if (header === null) {
      if (this.loginService.isGuest()) {
        this.redirectionService.mustRedirect();
      }
      this.router.navigate(['/denied']);
    } else {
      this.router.navigate(['/explore'], {
        queryParams: { headerId: this.node.id },
      });
    }
  }

  /**
   * Navigates within an interest group based on how the resolved node relates
   * to the group.
   *
   * When the node is the group itself it opens the group dashboard; when it
   * matches one of the group's well-known service ids (event, information,
   * newsgroup, library) it opens that service directly. Otherwise it falls
   * back to {@link navigateByService} to resolve the target from the node's
   * service and type.
   *
   * @param group The interest group owning the resolved node.
   */
  private groupNavigation(group: InterestGroup) {
    const nodeId = this.node.id;
    const groupId = group.id;

    if (!groupId) return;

    if (groupId === nodeId) {
      this.router.navigate(['/group', groupId]);
      return;
    }

    if (group.eventId === nodeId) {
      this.router.navigate(['/group', groupId, 'agenda']);
    } else if (group.informationId === nodeId) {
      this.router.navigate(['/group', groupId, 'information']);
    } else if (group.newsgroupId === nodeId) {
      this.router.navigate(['/group', groupId, 'forum']);
    } else if (group.libraryId === nodeId) {
      this.router.navigate(['/group', groupId, 'library', group.libraryId]);
    } else {
      this.navigateByService(groupId);
    }
  }

  /**
   * Navigates to a group view derived from the node's `service` and `type`.
   *
   * Dispatches to the library, agenda (events), information, forum
   * (newsgroups) or members view depending on the node's service, delegating
   * to {@link navigateLibrary} and {@link navigateNewsgroups} for the more
   * detailed cases. Falls back to the group dashboard when no specific service
   * matches.
   *
   * @param groupId The id of the interest group to navigate within.
   */
  private navigateByService(groupId: string) {
    const service = this.node.service;
    const nodeId = this.node.id;

    if (!nodeId) return;

    if (service === 'library') {
      this.navigateLibrary(groupId, nodeId);
    } else if (service === 'events') {
      this.router.navigate(['/group', groupId, 'agenda', nodeId, 'details']);
    } else if (service === 'information') {
      this.router.navigate(['/group', groupId, 'information'], {
        queryParams: { filterId: nodeId },
      });
    } else if (service === 'newsgroups') {
      this.navigateNewsgroups(groupId, nodeId);
    } else if (this.node.type?.endsWith('circaDirectoryRoot')) {
      this.router.navigate(['/group', groupId, 'members']);
    } else {
      this.router.navigate(['/group', groupId]);
    }
  }

  /**
   * Navigates to a library node, either a content item's details page or a
   * folder listing, based on the node's type.
   *
   * For content nodes, a `download=true` query parameter is forwarded when it
   * is present on the current route.
   *
   * @param groupId The id of the interest group owning the library.
   * @param nodeId The id of the library content or folder node.
   */
  private navigateLibrary(groupId: string, nodeId: string) {
    if (this.node.type?.endsWith('content')) {
      const hasDownload = this.route.snapshot.queryParams.download === 'true';
      const path = ['/group', groupId, 'library', nodeId, 'details'];
      this.router.navigate(
        path,
        hasDownload ? { queryParams: { download: 'true' } } : {}
      );
    } else if (this.node.type?.endsWith('folder')) {
      this.router.navigate(['/group', groupId, 'library', nodeId]);
    }
  }

  /**
   * Navigates to a newsgroup (forum) node, opening either the forum listing
   * for a forum node or a specific topic for a topic node.
   *
   * @param groupId The id of the interest group owning the forum.
   * @param nodeId The id of the forum or topic node.
   */
  private navigateNewsgroups(groupId: string, nodeId: string) {
    if (this.node.type?.endsWith('forum')) {
      this.router.navigate(['/group', groupId, 'forum', nodeId]);
    } else if (this.node.type?.endsWith('topic')) {
      this.router.navigate(['/group', groupId, 'forum', 'topic', nodeId]);
    }
  }
}
