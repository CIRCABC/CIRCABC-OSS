import { NgStyle } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnChanges,
  OnInit,
  signal,
} from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { Node, NodesService } from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { TaggedToPlainTextPipe } from 'app/shared/pipes/taggedtoplaintext.pipe';

/**
 * Breadcrumb navigation component for the group section.
 *
 * Renders the hierarchical path (breadcrumb trail) leading to a given node,
 * as resolved from the backend via {@link NodesService.getPath}. Each ancestor
 * segment (the "clickable path") is rendered as a router link so the user can
 * navigate back up the hierarchy, while the final segment (the "last element")
 * is rendered as plain text representing the current location.
 *
 * The component adapts its link routing depending on whether the current
 * route is a details or topic view, and can optionally display node titles
 * instead of names, a home icon, and a custom text color.
 *
 * @collaborators {@link NodesService} to fetch the node path, {@link Router}
 * to inspect the current URL and build relative navigation links.
 */
@Component({
  selector: 'cbc-group-breadcrumb',
  templateUrl: './breadcrumb.component.html',
  styleUrl: './breadcrumb.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RouterLink,
    NgStyle,
    I18nPipe,
    TaggedToPlainTextPipe,
    TranslocoModule,
  ],
})
export class BreadcrumbComponent implements OnInit, OnChanges {
  /** Service used to resolve the full ancestor path for the target node. */
  private readonly nodesService = inject(NodesService);
  /** Router used to inspect the current URL and to build relative links. */
  private readonly router = inject(Router);

  /**
   * When `true`, removes the bottom margin of the breadcrumb, allowing it to
   * sit flush with adjacent content. Defaults to `false`.
   */
  public readonly noMarginBottom = input(false);
  /**
   * When `true`, the ancestor segments are rendered as navigable router links.
   * Defaults to `true`.
   */
  public readonly clickable = input(true);
  /**
   * Identifier of the node whose ancestor path should be displayed. Changing
   * this input triggers a fresh path lookup in {@link ngOnChanges}.
   */
  public readonly node = input<string>();
  /** The resolved ancestor-to-current path for the target node. */
  private readonly path = signal<Node[]>([]);
  /**
   * When `true`, the raw names of the breadcrumb nodes are displayed directly;
   * when `false`, the localized title is used instead. Defaults to `true`.
   */
  // if displayName === true, the names of the breadcrumb are displayed directly, if false, the title is tried
  public readonly displayName = input(true);
  /**
   * Optional hex color (without the leading `#`) applied to the breadcrumb
   * text via {@link getStyle}.
   */
  public readonly textColor = input<string>();
  /** When `true`, a home icon is shown at the start of the breadcrumb. Defaults to `true`. */
  public readonly showHomeIcon = input(true);

  /** Whether the current route is a details view (affects link building). */
  public isInDetails = false;
  /** Whether the current route is a topic view (affects link building). */
  public isInTopic = false;

  /** Ancestor segments rendered as clickable links (all but the last element). */
  public readonly clickablePath = signal<Node[]>([]);
  /** The final segment of the path, representing the current location. */
  public readonly lastElement = signal<Node[]>([]);

  /**
   * Angular lifecycle hook. Determines whether the current route is a details
   * or topic view by inspecting the router URL, which affects how relative
   * navigation links are constructed.
   */
  public ngOnInit() {
    this.isInDetails = this.router.url.includes('details');
    this.isInTopic = this.router.url.includes('topic');
  }

  /**
   * Angular lifecycle hook triggered when inputs change. When {@link node} is
   * defined, fetches the node's ancestor path from the backend and splits it
   * into {@link clickablePath} (navigable ancestors) and {@link lastElement}
   * (the current node). On failure, the path is reset to empty and the error
   * is logged.
   *
   * @returns A promise that resolves once the path has been fetched and processed.
   */
  public ngOnChanges() {
    void this.loadPath();
  }

  private async loadPath() {
    const node = this.node();
    if (node === undefined) {
      return;
    }
    let path: Node[];
    try {
      path = await this.nodesService.getPathAsync({ id: node });
    } catch (e) {
      path = [];
      console.error(e);
    }
    this.path.set(path);
    if (path.length === 1) {
      this.clickablePath.set([]);
      this.lastElement.set(path.slice(0, 1));
    } else if (path.length > 1) {
      this.clickablePath.set(path.slice(0, -1));

      if (path.length >= 2) {
        this.lastElement.set(path.slice(-1));
      } else {
        this.lastElement.set([]);
      }
    }
  }

  /**
   * Determines whether the given node is the root (first) element of the path.
   *
   * @param part The node to test against the start of the resolved path.
   * @returns `true` if `part` is the first element of the path, otherwise `false`.
   */
  public isRoot(part: Node) {
    const path = this.path();
    if (path.length > 0) {
      if (path[0] === part) {
        return true;
      }
    }

    return false;
  }

  /**
   * Builds the inline style object for the breadcrumb text based on the
   * {@link textColor} input.
   *
   * @returns An empty object when no text color is set, otherwise an object
   * with a `color` property using the hex value (prefixed with `#`).
   */
  public getStyle(): object {
    const textColor = this.textColor();
    if (textColor === undefined) {
      return {};
    }
    return {
      color: `#${textColor}`,
    };
  }

  /**
   * Builds the relative router link segments used to navigate to the given
   * node. The number of parent-directory hops depends on whether the current
   * route is a details or topic view.
   *
   * @param node The target node to navigate to.
   * @returns An array of route segments (relative path prefix followed by the
   * node id) suitable for use with `routerLink`.
   */
  public prepareArrayParts(node: Node) {
    let result = ['../', node.id];

    if (this.isInDetails || this.isInTopic) {
      result = ['../../', node.id];
    }

    return result;
  }

  /**
   * Determines whether a node represents the special "Newsgroups" entry within
   * a forums context, which is rendered differently in the breadcrumb.
   *
   * @param name The node name to check.
   * @param type The node type string to inspect for a forums context.
   * @returns `true` if the name is `'Newsgroups'` and the type indicates a
   * forums context, otherwise `false`.
   */
  isForumNewsgroupsName(name: string, type: string) {
    return name === 'Newsgroups' && type.includes('forums');
  }
}
