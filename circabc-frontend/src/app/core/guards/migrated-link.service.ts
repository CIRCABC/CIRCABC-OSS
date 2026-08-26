import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import {
  InterestGroup,
  Node as ModelNode,
  NodesService,
} from 'app/core/generated/circabc';

const UUID_RE =
  /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/;

/**
 * Resolves "old" CIRCABC deep links (e.g. links created on another/previous server,
 * or before a migration) whose node/interest-group ids no longer exist on this server,
 * and redirects the browser to the equivalent "new" URL.
 *
 * It relies on the migrated nodes carrying their source NodeRef in the
 * {@code ci:originalNodeRef} property (exposed by the backend
 * {@code GET /nodes/resolve/{originalId}} endpoint). Each uuid segment of the URL that
 * matches a migrated node's original ref is swapped for the migrated node's new id,
 * keeping the service segment, trailing path and query string intact so the user lands
 * on the same page (e.g. a document details page).
 *
 * This is only ever invoked from the route guards' "not found" branch, so it adds no
 * cost to normal navigation, and the rebuilt URL is re-checked by the same guards
 * (permissions are therefore enforced normally on the resolved node).
 */
@Injectable({ providedIn: 'root' })
export class MigratedLinkService {
  private readonly nodesService = inject(NodesService);
  private readonly router = inject(Router);

  /**
   * Attempt to redirect an unresolved group/node URL to its migrated equivalent.
   *
   * @param url the full router url that failed to resolve (typically state.url)
   * @returns true if a redirect was triggered (the caller should cancel the current
   *          navigation), false otherwise
   */
  public async tryRedirectFromOriginal(url: string): Promise<boolean> {
    if (!url) {
      return false;
    }

    // Split off the query string so only the path segments are rewritten.
    const queryIdx = url.indexOf('?');
    const query = queryIdx >= 0 ? url.substring(queryIdx) : '';
    const path = queryIdx >= 0 ? url.substring(0, queryIdx) : url;

    const segments = path.split('/').filter((s) => s.length > 0);
    // Only handle interest-group scoped links for now.
    if (segments.length < 2 || segments[0] !== 'group') {
      return false;
    }

    const newSegments = [...segments];
    let changed = false;
    let resolvedChildNodeId: string | undefined;

    // Resolve every uuid-shaped segment against its original node ref.
    for (let i = 1; i < segments.length; i++) {
      const seg = segments[i];
      if (!UUID_RE.test(seg)) {
        continue;
      }
      const node = await this.resolve(seg);
      if (node?.id) {
        newSegments[i] = node.id;
        changed = true;
        if (i > 1) {
          resolvedChildNodeId = node.id;
        }
      }
    }

    // If the interest-group segment (index 1) could not be resolved directly (e.g. the
    // IG root itself was not stamped with an original ref) but a child node was, derive
    // the new interest-group id from that resolved node.
    if (
      UUID_RE.test(segments[1]) &&
      newSegments[1] === segments[1] &&
      resolvedChildNodeId
    ) {
      const ig = await this.group(resolvedChildNodeId);
      if (ig?.id) {
        newSegments[1] = ig.id;
        changed = true;
      }
    }

    if (!changed) {
      return false;
    }

    const newUrl = `/${newSegments.join('/')}${query}`;
    const currentUrl = `/${segments.join('/')}${query}`;
    if (newUrl === currentUrl) {
      return false;
    }

    await this.router.navigateByUrl(newUrl, { replaceUrl: true });
    return true;
  }

  private async resolve(originalId: string): Promise<ModelNode | undefined> {
    try {
      return await firstValueFrom(
        this.nodesService.resolveByOriginalNodeRef(originalId)
      );
    } catch {
      // 404 (no migrated node) or 403 (no read access) -> cannot redirect.
      return undefined;
    }
  }

  private async group(nodeId: string): Promise<InterestGroup | undefined> {
    try {
      return await firstValueFrom(this.nodesService.getGroup(nodeId));
    } catch {
      return undefined;
    }
  }
}
