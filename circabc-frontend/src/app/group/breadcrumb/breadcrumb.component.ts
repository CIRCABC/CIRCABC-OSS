import { Component, Input, OnChanges, OnInit, input } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { NgStyle } from '@angular/common';
import { TranslocoModule } from '@jsverse/transloco';
import { Node, NodesService } from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { TaggedToPlainTextPipe } from 'app/shared/pipes/taggedtoplaintext.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-group-breadcrumb',
  templateUrl: './breadcrumb.component.html',
  styleUrl: './breadcrumb.component.scss',
  preserveWhitespaces: true,
  imports: [
    RouterLink,
    NgStyle,
    I18nPipe,
    TaggedToPlainTextPipe,
    TranslocoModule,
  ],
})
export class BreadcrumbComponent implements OnInit, OnChanges {
  public readonly noMarginBottom = input(false);
  public readonly clickable = input(true);
  public readonly node = input<string>();
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  public path!: Node[];
  // if displayName === true, the names of the breadcrumb are displayed directly, if false, the title is tried
  public readonly displayName = input(true);
  public readonly textColor = input<string>();
  public readonly showHomeIcon = input(true);

  public isInDetails = false;
  public isInTopic = false;

  public clickablePath: Node[] = [];
  public lastElement: Node[] = [];

  public constructor(
    private nodesService: NodesService,
    private router: Router
  ) {}

  public ngOnInit() {
    this.isInDetails = this.router.url.indexOf('details') > -1;
    this.isInTopic = this.router.url.indexOf('topic') > -1;
  }

  public async ngOnChanges() {
    const node = this.node();
    if (node === undefined) {
      return;
    }
    try {
      this.path = await firstValueFrom(this.nodesService.getPath(node));
    } catch (e) {
      this.path = [];
      console.error(e);
    }
    if (this.path && this.path.length === 1) {
      this.clickablePath = [];
      this.lastElement = this.path.slice(0, 1);
    } else if (this.path && this.path.length > 1) {
      this.clickablePath = this.path.slice(0, this.path.length - 1);

      if (this.path && this.path.length >= 2) {
        this.lastElement = this.path.slice(-1);
      } else {
        this.lastElement = [];
      }
    }
  }

  public isRoot(part: Node) {
    if (this.path && this.path.length > 0) {
      if (this.path[0] === part) {
        return true;
      }
    }

    return false;
  }

  public getStyle(): object {
    const textColor = this.textColor();
    if (textColor === undefined) {
      return {};
    }
    return {
      color: `#${textColor}`,
    };
  }

  public prepareArrayParts(node: Node) {
    let result = ['../', node.id];

    if (this.isInDetails || this.isInTopic) {
      result = ['../../', node.id];
    }

    return result;
  }

  isForumNewsgroupsName(name: string, type: string) {
    return name === 'Newsgroups' && type.includes('forums');
  }
}
