import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Data, Router } from '@angular/router';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  GroupConfiguration,
  InterestGroup,
  InterestGroupService,
  Node as ModelNode,
  NodesService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-forum',
  templateUrl: './forum.component.html',
  preserveWhitespaces: true,
})
export class ForumComponent implements OnInit {
  public node!: ModelNode;
  public nodeId!: string;
  public groupId!: string;
  public group!: InterestGroup;
  public loading = false;
  public showConfigurationModal = false;
  public groupConfiguration!: GroupConfiguration;

  public constructor(
    private nodesService: NodesService,
    private route: ActivatedRoute,
    private permEvalService: PermissionEvaluatorService,
    private groupsService: InterestGroupService,
    private loginService: LoginService,
    private router: Router
  ) {}

  public ngOnInit() {
    this.route.data.subscribe(async (value: Data) => {
      this.group = value.group;

      if (this.group.id) {
        this.groupId = this.group.id;
      }

      if (this.group.newsgroupId) {
        await this.getGroupConf();
      }

      this.route.params.subscribe(async (params) => {
        await this.loadForum({ nodeId: params.nodeId });
      });
    });
  }

  private async getGroupConf() {
    if (this.groupId) {
      this.groupConfiguration = await firstValueFrom(
        this.groupsService.getGroupConfiguration(this.groupId)
      );
    }
  }

  public async loadForum(params: { [key: string]: string }) {
    this.loading = true;
    this.nodeId = params.nodeId;
    this.node = await firstValueFrom(this.nodesService.getNode(this.nodeId));
    this.loading = false;
  }

  public async refresh(result: ActionEmitterResult) {
    if (
      result.result === ActionResult.SUCCEED &&
      result.type === ActionType.CREATE_FORUM
    ) {
      if (result && result.node) {
        // eslint-disable-next-line @typescript-eslint/no-floating-promises
        this.router.navigate(['..', result.node.id], {
          relativeTo: this.route,
        });
      }
    } else if (
      result.result === ActionResult.SUCCEED &&
      result.type === ActionType.CREATE_TOPIC
    ) {
      await this.loadForum({ nodeId: this.nodeId });
    }
  }
  public isNewsgroupPost(): boolean {
    return (
      this.permEvalService.isNewsgroupPost(this.node) ||
      this.permEvalService.isNewsgroupModerate(this.node)
    );
  }

  public isNewsgroupModerateAdmin(): boolean {
    return (
      this.permEvalService.isNewsgroupAdmin(this.node) ||
      this.permEvalService.isNewsgroupModerate(this.node) ||
      this.isNewsgroupAdmin()
    );
  }

  public isNewsgroupRoot(): boolean {
    return this.node.type?.endsWith('forums') ?? false;
  }

  public isNewsgroupAdmin(): boolean {
    if (this.group.permissions.newsgroup === 'NwsAdmin') {
      return true;
    }
    return (
      this.permEvalService.isNewsgroupAdmin(this.node) ||
      this.permEvalService.isOwner(
        this.node,
        this.loginService.getCurrentUsername()
      )
    );
  }

  public isLibAdmin(): boolean {
    if (this.group.permissions.library === 'LibAdmin') {
      return true;
    }
    return this.permEvalService.isLibAdmin(this.node);
  }

  public async refreshConf(res: ActionEmitterResult) {
    if (res.result === ActionResult.SUCCEED && this.groupId) {
      this.groupConfiguration = await firstValueFrom(
        this.groupsService.getGroupConfiguration(this.groupId)
      );
    }
  }
}
