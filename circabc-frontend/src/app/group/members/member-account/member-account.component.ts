import { Location } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { User, UserService } from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-member-account',
  templateUrl: './member-account.component.html',
  styleUrls: ['./member-account.component.scss'],
  preserveWhitespaces: true,
})
export class MemberAccountComponent implements OnInit {
  public user!: User;
  public loading = false;

  constructor(
    private route: ActivatedRoute,
    private userService: UserService,
    private location: Location
  ) {}

  ngOnInit() {
    if (this.route.params) {
      this.route.params.subscribe(async (params) =>
        this.loadMember(params.userid)
      );
    }
  }

  async loadMember(userid: string) {
    if (userid) {
      this.loading = true;
      this.user = await firstValueFrom(this.userService.getUser(userid));
      this.loading = false;
    }
  }

  public goBack() {
    this.location.back();
  }
}
