import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';

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
 * `ci:originalNodeRef` property (exposed by the backend
 * `GET /nodes/resolve/{originalId}` endpoint). Each uuid segment of the URL that
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
    const parsed = this.parseUrl(url);
    if (!parsed) {
      return false;
    }

    const { segments, query } = parsed;
    if (!this.isInterestGroupScoped(segments)) {
      return false;
    }

    const { newSegments, changed, resolvedChildNodeId } =
      await this.resolveSegments(segments);

    if (!changed) {
      return false;
    }

    await this.tryResolveInterestGroupId(
      segments,
      newSegments,
      resolvedChildNodeId
    );

    const newUrl = `/${newSegments.join('/')}${query}`;
    const currentUrl = `/${segments.join('/')}${query}`;

    if (newUrl === currentUrl) {
      return false;
    }

    await this.router.navigateByUrl(newUrl, { replaceUrl: true });
    return true;
  }

  private parseUrl(
    url: string
  ): { segments: string[]; query: string } | undefined {
    if (!url) {
      return undefined;
    }

    const queryIdx = url.indexOf('?');
    const query = queryIdx >= 0 ? url.substring(queryIdx) : '';
    const path = queryIdx >= 0 ? url.substring(0, queryIdx) : url;
    const segments = path.split('/').filter((s) => s.length > 0);

    return { segments, query };
  }

  private isInterestGroupScoped(segments: string[]): boolean {
    return segments.length >= 2 && segments[0] === 'group';
  }

  private async resolveSegments(segments: string[]): Promise<{
    newSegments: string[];
    changed: boolean;
    resolvedChildNodeId?: string;
  }> {
    const newSegments = [...segments];
    let changed = false;
    let resolvedChildNodeId: string | undefined;

    for (let i = 1; i < segments.length; i++) {
      const seg = segments[i];

      if (!UUID_RE.test(seg)) {
        continue;
      }

      const node = await this.resolve(seg);
      if (!node?.id) {
        continue;
      }

      newSegments[i] = node.id;
      changed = true;

      if (i > 1) {
        resolvedChildNodeId = node.id;
      }
    }

    return { newSegments, changed, resolvedChildNodeId };
  }

  private async tryResolveInterestGroupId(
    segments: string[],
    newSegments: string[],
    resolvedChildNodeId?: string
  ): Promise<void> {
    const interestGroupSegment = segments[1];

    const shouldResolveInterestGroup =
      UUID_RE.test(interestGroupSegment) &&
      newSegments[1] === interestGroupSegment &&
      resolvedChildNodeId;

    if (!shouldResolveInterestGroup) {
      return;
    }

    const ig = await this.group(resolvedChildNodeId);
    if (ig?.id) {
      newSegments[1] = ig.id;
    }
  }

  private async resolve(originalId: string): Promise<ModelNode | undefined> {
    try {
      return await this.nodesService.resolveByOriginalNodeRefAsync({
        originalId,
      });
    } catch {
      // 404 (no migrated node) or 403 (no read access) -> cannot redirect.
      return undefined;
    }
  }

  private async group(nodeId: string): Promise<InterestGroup | undefined> {
    try {
      return await this.nodesService.getGroupAsync({ id: nodeId });
    } catch {
      return undefined;
    }
  }
}
