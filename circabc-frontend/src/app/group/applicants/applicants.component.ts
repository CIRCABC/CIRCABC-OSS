import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import {
  animate,
  state,
  style,
  transition,
  trigger,
} from '@angular/animations';

import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  Applicant,
  InterestGroup,
  InterestGroupService,
  MembersService,
  Profile,
  ProfileService,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-applicants',
  templateUrl: './applicants.component.html',
  styleUrls: ['./applicants.component.scss'],
  preserveWhitespaces: true,
  animations: [
    trigger('state', [
      state('init', style({ transform: 'translateX(0)' })),
      transition('* => dec', [
        animate(300, style({ transform: 'translateX(-100%)' })),
      ]),
      transition('* => inc', [
        animate(300, style({ transform: 'translateX(100%)' })),
      ]),
    ]),
  ],
})
export class ApplicantsComponent implements OnInit {
  public igId!: string;
  public applicants: Applicant[] = [];
  public profiles: Profile[] = [];
  public currentApplicant!: Applicant;
  public currentIndex = 1;
  public loading = false;
  public currentGroup!: InterestGroup;
  public alreadyMember = false;
  public state: 'init' | 'inc' | 'dec' = 'init';

  constructor(
    private route: ActivatedRoute,
    private membersService: MembersService,
    private groupsService: InterestGroupService,
    private profileService: ProfileService,
    private loginService: LoginService,
    private userService: UserService,
    private permEvalService: PermissionEvaluatorService
  ) {}

  async ngOnInit() {
    this.route.params.subscribe(async (params) => {
      this.currentGroup = await firstValueFrom(
        this.groupsService.getInterestGroup(params.id)
      );

      await this.getCurrentUserMemberships();

      await this.loadApplicants(params);
      await this.loadProfiles(params);
    });
  }

  async loadApplicants(params: { [id: string]: string }) {
    this.loading = true;
    this.igId = params.id;
    if (this.igId) {
      this.applicants = await firstValueFrom(
        this.membersService.getApplicant(this.igId)
      );
      if (this.applicants.length > 0) {
        this.selectFirstApplicant();
      }
    }
    this.loading = false;
  }

  async loadProfiles(params: { [id: string]: string }) {
    this.igId = params.id;
    if (this.igId) {
      try {
        this.profiles = await firstValueFrom(
          this.profileService.getProfiles(this.igId)
        );

        this.profiles = this.profiles.filter(
          (profile) => profile.name !== 'guest' && profile.name !== 'EVERYONE'
        );
      } catch (error) {
        console.error('problem retrieving the profiles');
      }
    }
  }

  selectFirstApplicant() {
    this.state = 'init';
    this.currentIndex = 1;
    this.currentApplicant = this.applicants[this.currentIndex - 1];
  }

  nextPage() {
    this.currentIndex += 1;
    if (this.currentIndex > this.applicants.length) {
      this.currentIndex = 1;
      this.state = 'dec';
    } else {
      this.state = 'inc';
    }
    this.currentApplicant = this.applicants[this.currentIndex - 1];
  }

  previousPage() {
    this.currentIndex -= 1;
    if (this.currentIndex <= 0) {
      this.currentIndex = this.applicants.length;
      this.state = 'inc';
    } else {
      this.state = 'dec';
    }

    this.currentApplicant = this.applicants[this.currentIndex - 1];
  }

  onRequestProcessed(_applicant: Applicant) {
    this.applicants.splice(this.currentIndex - 1, 1);
    if (this.currentIndex > this.applicants.length) {
      this.currentIndex = this.applicants.length - 1;
    }
    this.currentApplicant = this.applicants[this.currentIndex - 1];
  }

  isMember() {
    return this.alreadyMember;
  }

  private async getCurrentUserMemberships() {
    if (!this.loginService.isGuest()) {
      const userId =
        this.loginService.getUser().userId !== undefined
          ? this.loginService.getUser().userId
          : 'guest';
      if (userId !== undefined) {
        const currentUserMemberships = await firstValueFrom(
          this.userService.getUserMembership(userId)
        );
        for (const profile of currentUserMemberships) {
          if (
            profile &&
            profile.interestGroup &&
            profile.interestGroup.id === this.currentGroup.id
          ) {
            this.alreadyMember = true;
          }
        }
      }
    }
  }

  public isDirAdmin(): boolean {
    return this.permEvalService.isDirAdmin(this.currentGroup);
  }

  public isDirManageMembers(): boolean {
    return this.permEvalService.isDirManageMembers(this.currentGroup);
  }
}
