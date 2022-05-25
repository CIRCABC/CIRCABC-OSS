import { Component, Input, OnChanges, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { Node, NodesService } from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-group-breadcrumb',
  templateUrl: './breadcrumb.component.html',
  styleUrls: ['./breadcrumb.component.scss'],
  preserveWhitespaces: true,
})
export class BreadcrumbComponent implements OnInit, OnChanges {
  @Input()
  public noMarginBottom = false;
  @Input()
  public clickable = true;
  @Input()
  public node: string | undefined;
  @Input()
  public path!: Node[];
  // if displayName === true, the names of the breadcrumb are displayed directly, if false, the title is tried
  @Input()
  public displayName = true;
  @Input()
  public textColor!: string;
  @Input()
  public showHomeIcon = true;

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
    if (this.node === undefined) {
      return;
    }
    this.path = await firstValueFrom(this.nodesService.getPath(this.node));

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
    if (this.textColor === undefined) {
      return {};
    }
    return {
      color: `#${this.textColor}`,
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
