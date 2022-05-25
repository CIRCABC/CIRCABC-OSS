import { Component, Input, OnInit } from '@angular/core';

import {
  GroupPath,
  InterestGroup,
  InterestGroupService,
} from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-group-location',
  templateUrl: './group-location.component.html',
  styleUrls: ['./group-location.component.scss'],
})
export class GroupLocationComponent implements OnInit {
  @Input()
  group!: InterestGroup;
  groupPath!: GroupPath;

  constructor(private groupService: InterestGroupService) {}

  async ngOnInit() {
    if (this.group && this.group.id) {
      this.groupPath = await firstValueFrom(
        this.groupService.getGroupPath(this.group.id)
      );
    }
  }
}
