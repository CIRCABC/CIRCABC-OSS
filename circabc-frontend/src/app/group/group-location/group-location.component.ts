import { Component, OnInit, input } from '@angular/core';

import { I18nSelectPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import {
  GroupPath,
  type InterestGroup,
  InterestGroupService,
} from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-group-location',
  templateUrl: './group-location.component.html',
  styleUrl: './group-location.component.scss',
  imports: [RouterLink, I18nSelectPipe, I18nPipe],
})
export class GroupLocationComponent implements OnInit {
  readonly group = input.required<InterestGroup>();
  groupPath!: GroupPath;

  constructor(private groupService: InterestGroupService) {}

  async ngOnInit() {
    const group = this.group();
    if (group?.id) {
      this.groupPath = await firstValueFrom(
        this.groupService.getGroupPath(group.id)
      );
    }
  }
}
