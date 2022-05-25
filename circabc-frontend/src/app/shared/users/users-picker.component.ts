/* eslint-disable @typescript-eslint/no-explicit-any */
import {
  Component,
  EventEmitter,
  forwardRef,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import {
  ControlValueAccessor,
  FormControl,
  FormGroup,
  NG_VALUE_ACCESSOR,
} from '@angular/forms';

import {
  InterestGroup,
  InterestGroupService,
  MembersService,
  PagedUserProfile,
  Profile,
  ProfileService,
  User,
  UserProfile,
} from 'app/core/generated/circabc';

import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { firstValueFrom } from 'rxjs';

type UserOrProfile = User | Profile;

type UserOrProfileOrString = User | Profile | string;

interface SelectableUserOrProfile {
  item: UserOrProfile;
  selected: boolean;
}
interface Types {
  value: number;
  text: string;
}

@Component({
  selector: 'cbc-users-picker',
  templateUrl: './users-picker.component.html',
  styleUrls: ['./users-picker.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      multi: true,
      // eslint-disable-next-line @angular-eslint/no-forward-ref
      useExisting: forwardRef(() => UsersPickerComponent),
    },
  ],
  preserveWhitespaces: true,
})
export class UsersPickerComponent implements OnInit, ControlValueAccessor {
  @Input()
  public displayGuestRegistered = false;
  @Input()
  public igId: string | undefined;
  @Input()
  public profilesTip!: string;
  @Input()
  public usersTip!: string;
  @Input()
  public showSelectedList = true;
  @Output()
  public readonly afterSelectionMade = new EventEmitter();
  @Output()
  public readonly userOrProfileQueryError = new EventEmitter();

  public availableTypes: Types[] = [
    { value: 0, text: 'label.user' },
    { value: 1, text: 'label.profile' },
  ];
  public selectedTypeValue = '0';
  public availableUsersOrProfiles!: SelectableUserOrProfile[];
  public selectedUsersOrProfiles: UserOrProfileOrString[] = [];
  public form!: FormGroup;
  public searchText = '';
  public currentGroup!: InterestGroup;

  private static isUser(userOrProfile: UserOrProfile): userOrProfile is User {
    return (userOrProfile as User).userId !== undefined;
  }

  private static isProfile(
    userOrProfile: UserOrProfile
  ): userOrProfile is Profile {
    return (userOrProfile as Profile).name !== undefined;
  }

  private static areEquals(a: UserOrProfile, b: UserOrProfile): boolean {
    if (UsersPickerComponent.isUser(a) && UsersPickerComponent.isUser(b)) {
      return a.userId === b.userId;
    } else if (
      UsersPickerComponent.isProfile(a) &&
      UsersPickerComponent.isProfile(b)
    ) {
      return a.name === b.name;
    } else {
      return false;
    }
  }

  // impement ControlValueAccessor interface
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  // eslint-disable-next-line no-empty,@typescript-eslint/no-empty-function
  onChange = (_: any) => {};

  public getCode(userOrProfile: UserOrProfile): string {
    let result = '';
    if (UsersPickerComponent.isUser(userOrProfile)) {
      result = userOrProfile.userId as string;
    } else if (UsersPickerComponent.isProfile(userOrProfile)) {
      result = userOrProfile.name as string;
    }
    return result;
  }

  public getName(userOrProfile: UserOrProfile): string {
    let result = '';
    if (UsersPickerComponent.isUser(userOrProfile)) {
      result = `${userOrProfile.firstname}  ${userOrProfile.lastname}`;
    } else if (UsersPickerComponent.isProfile(userOrProfile)) {
      result = userOrProfile.name as string;
    }
    return result;
  }

  public getNameEmail(userOrProfile: UserOrProfileOrString): string {
    let result = '';
    if (typeof userOrProfile === 'string') {
      return userOrProfile;
    }
    if (UsersPickerComponent.isUser(userOrProfile)) {
      if (this.isDirAdmin() || this.isDirManageMembers()) {
        result = `${userOrProfile.firstname} ${userOrProfile.lastname} (${userOrProfile.email})`;
      } else {
        result = `${userOrProfile.firstname} ${userOrProfile.lastname}`;
        if (userOrProfile.properties && userOrProfile.properties.ecMoniker) {
          result = `${result} (${userOrProfile.properties.ecMoniker})`;
        }
      }
    } else if (UsersPickerComponent.isProfile(userOrProfile)) {
      result = userOrProfile.name as string;
      const profileTitle = this.i18nPipe.transform(userOrProfile.title);
      if (profileTitle !== '' && profileTitle !== undefined) {
        result = profileTitle;
      }
    }
    return result;
  }

  // eslint-disable-next-line no-empty, @typescript-eslint/no-empty-function
  onTouched = () => {};

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  async writeValue(value: any) {
    if (value === null) {
      this.selectedUsersOrProfiles = [];
      this.form.reset();
      this.availableUsersOrProfiles = [];
    } else if (value) {
      await this.buildCompleteUsersArray(value);
    }
  }
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  registerOnChange(fn: (_: any) => void) {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => {}): void {
    this.onTouched = fn;
  }

  constructor(
    private profileService: ProfileService,
    private uiMessageService: UiMessageService,
    private membersService: MembersService,
    private i18nPipe: I18nPipe,
    private permEvalService: PermissionEvaluatorService,
    private groupService: InterestGroupService
  ) {}

  public async ngOnInit() {
    this.availableUsersOrProfiles = [];
    this.selectedUsersOrProfiles = [];

    const selectedUsersOrProfilesFormControl = new FormControl(
      this.selectedUsersOrProfiles
    );
    const searchTextFormControl = new FormControl(this.searchText);
    this.form = new FormGroup(
      {
        selectedUsersOrProfiles: selectedUsersOrProfilesFormControl,
        searchText: searchTextFormControl,
      },
      {
        updateOn: 'change',
      }
    );
    selectedUsersOrProfilesFormControl.valueChanges.subscribe((value) => {
      this.selectedUsersOrProfiles = value;
      if (this.onChange) {
        this.onChange(value);
      }
    });
    searchTextFormControl.valueChanges.subscribe((value) => {
      this.searchText = value;
    });

    await this.loadGroup();
  }

  private async loadGroup() {
    if (this.igId !== undefined) {
      this.currentGroup = await firstValueFrom(
        this.groupService.getInterestGroup(this.igId)
      );
    }
  }

  public isDirAdmin(): boolean {
    return this.permEvalService.isDirAdmin(this.currentGroup);
  }

  public isDirManageMembers(): boolean {
    return this.permEvalService.isDirManageMembers(this.currentGroup);
  }

  private async buildCompleteUsersArray(
    usersOrProfilesArray: (User | Profile | string)[]
  ) {
    if (this.igId === undefined) {
      return;
    }

    try {
      const pagedUserProfile: PagedUserProfile = await firstValueFrom(
        this.membersService.getMembers(
          this.igId,
          undefined,
          undefined,
          undefined,
          undefined,
          undefined,
          ''
        )
      );

      // collect the user details for the audience to be displayed on the screen
      for (const userOrProfile of usersOrProfilesArray) {
        if (!(userOrProfile as User).firstname) {
          // in case the user is not complete, only usedId (modify users)
          if (
            ((userOrProfile as User).userId !== undefined &&
              !(userOrProfile as string).includes('@')) ||
            typeof userOrProfile === 'string'
          ) {
            // internal users
            if (pagedUserProfile.data) {
              for (const userProfile of pagedUserProfile.data) {
                if (userProfile.user !== undefined) {
                  const user: User = userProfile.user;
                  if (
                    userOrProfile === user.userId &&
                    !this.selectedUsersOrProfiles.includes(user)
                  ) {
                    this.selectedUsersOrProfiles.push(user);
                  }
                }
              }
              continue;
            }
          }
        }
        // in case the user is already complete (add new users) or it is a profile
        this.selectedUsersOrProfiles.push(userOrProfile);
      }
    } catch (error) {
      this.userOrProfileQueryError.emit();
    }
  }

  public onTypeChange(value: string): void {
    this.selectedTypeValue = value;
  }

  // searches for the users/profiles to display
  public async doSearch() {
    try {
      if (this.igId) {
        this.searchText =
          this.searchText === undefined || this.searchText === null
            ? ''
            : this.searchText;
        if (this.selectedTypeValue === '0') {
          // search for users

          const pagedUserProfile: PagedUserProfile = await firstValueFrom(
            this.membersService.getMembers(
              this.igId,
              undefined,
              undefined,
              undefined,
              undefined,
              undefined,
              this.searchText
            )
          );
          if (pagedUserProfile.data) {
            const availableUserProfiles: UserProfile[] = pagedUserProfile.data;

            // build the array of Selectable Users from the array of UserProfile returned by the service
            // eslint-disable-next-line arrow-body-style
            this.availableUsersOrProfiles = availableUserProfiles.map(
              (userProfile) => {
                // eslint-disable-next-line @typescript-eslint/consistent-type-assertions
                return {
                  item: userProfile.user,
                  selected: false,
                } as SelectableUserOrProfile;
              }
            );
          }
        } else {
          // search for profiles

          let profiles: Profile[] = await firstValueFrom(
            this.profileService.getProfiles(
              this.igId,
              undefined,
              this.searchText,
              false
            )
          );

          if (!this.displayGuestRegistered) {
            profiles = profiles.filter(
              (profile: Profile) =>
                profile.name !== 'guest' && profile.name !== 'EVERYONE'
            );
          }

          // build the array of Selectable Profiles from the array of UserProfile returned by the service
          this.availableUsersOrProfiles = profiles.map((profile) => {
            // eslint-disable-next-line @typescript-eslint/consistent-type-assertions
            return {
              item: profile,
              selected: false,
            } as SelectableUserOrProfile;
          });
        }
      }
    } catch (error) {
      this.userOrProfileQueryError.emit();
      const jsonError = JSON.parse(error._body);
      if (jsonError) {
        this.uiMessageService.addErrorMessage(jsonError.message);
      }
    }
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public setSelected(multiSelectElement: any): void {
    let i = 0;
    for (const optionElement of multiSelectElement.options) {
      if (optionElement.selected === true) {
        this.availableUsersOrProfiles[i].selected = true;
      } else {
        this.availableUsersOrProfiles[i].selected = false;
      }
      i += 1;
    }
  }

  public addToSelectedUsersOrProfiles(): void {
    // concat selectedUsersOrProfiles with elements that have been selected from availableUsersOrProfiles
    // and are also not found in selectedUsersOrProfiles (not already added to selectedUsersOrProfiles)
    if (
      this.selectedUsersOrProfiles === undefined ||
      this.selectedUsersOrProfiles === null
    ) {
      this.selectedUsersOrProfiles = [];
    }

    this.selectedUsersOrProfiles = this.selectedUsersOrProfiles.concat(
      this.availableUsersOrProfiles
        .filter((availableItem) => availableItem.selected)
        .map((selectableItem) => selectableItem.item)
        .filter((selectedItem) => {
          return (
            this.selectedUsersOrProfiles.find((currentItem) => {
              return UsersPickerComponent.areEquals(
                currentItem as User | Profile,
                selectedItem
              );
            }) === undefined
          );
        })
    );
    // propagate the changes to be catched by the for component to be updated
    this.onChange(this.selectedUsersOrProfiles);
    this.afterSelectionMade.emit();
  }

  // unselect items (fired when X is clicked)
  public removeFromSelectedUsersOrProfiles(
    selectedItem: UserOrProfileOrString
  ): void {
    const index: number = this.selectedUsersOrProfiles.indexOf(
      selectedItem as UserOrProfile,
      0
    );
    this.selectedUsersOrProfiles.splice(index, 1);
    // propagate the changes to be catched by the for component to be updated
    this.onChange(this.selectedUsersOrProfiles);
  }

  public clearAvailableUsersOrProfiles(): void {
    this.availableUsersOrProfiles = [];
  }
}
