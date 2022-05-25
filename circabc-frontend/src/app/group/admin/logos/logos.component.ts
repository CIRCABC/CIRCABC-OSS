import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ActionEmitterResult, ActionResult } from 'app/action-result';
import {
  InterestGroup,
  InterestGroupService,
  Node as ModelNode,
} from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-logos',
  templateUrl: './logos.component.html',
  styleUrls: ['./logos.component.scss'],
  preserveWhitespaces: true,
})
export class LogosComponent implements OnInit {
  public group!: InterestGroup;
  public logos: ModelNode[] = [];
  public showUploadModal = false;

  constructor(
    private route: ActivatedRoute,
    private groupsService: InterestGroupService
  ) {}

  ngOnInit() {
    this.route.params.subscribe(async (params) => {
      if (params.id) {
        await this.loadGroup(params.id);
      }
    });
  }

  private async loadGroup(id: string) {
    if (id) {
      this.group = await firstValueFrom(
        this.groupsService.getInterestGroup(id)
      );
      this.logos = await firstValueFrom(this.groupsService.getGroupLogos(id));
    }
  }

  public isSelected(id: string | undefined): boolean {
    if (id && this.group && this.group.logoUrl) {
      if (this.group.logoUrl.indexOf(id) !== -1) {
        return true;
      }
    }

    return false;
  }

  public async select(id: string | undefined) {
    if (id && this.group && this.group.id) {
      await firstValueFrom(
        this.groupsService.selectGroupLogo(this.group.id, id)
      );
      await this.loadGroup(this.group.id);
    }
  }

  public async delete(id: string | undefined) {
    if (id && this.group && this.group.id) {
      if (this.isSelected(id)) {
        await firstValueFrom(
          this.groupsService.selectGroupLogo(this.group.id, id)
        );
      }
      await firstValueFrom(
        this.groupsService.deleteGroupLogo(this.group.id, id)
      );
      await this.loadGroup(this.group.id);
    }
  }

  public async refresh(res: ActionEmitterResult) {
    if (res.result === ActionResult.SUCCEED && this.group.id) {
      await this.loadGroup(this.group.id);
    }
    this.showUploadModal = false;
  }
}
