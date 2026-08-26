import {
  Component,
  ElementRef,
  HostListener,
  OnInit,
  output,
  input,
} from '@angular/core';

import { User, UserService } from 'app/core/generated/circabc';
import { DownloadPipe } from 'app/shared/pipes/download.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-user-card',
  templateUrl: './user-card.component.html',
  styleUrl: './user-card.component.scss',
  preserveWhitespaces: true,
  imports: [DownloadPipe, SecurePipe],
})
export class UserCardComponent implements OnInit {
  readonly userId = input<string | null>();
  readonly rightSide = input(false);
  readonly disabled = input(false);
  public readonly clickOutside = output<MouseEvent>();

  public user!: User;
  public visible = false;
  private elementRef: ElementRef;
  public fullName = '';

  constructor(
    private userService: UserService,
    myElement: ElementRef
  ) {
    this.elementRef = myElement;
  }

  async ngOnInit() {
    const userId = this.userId();
    if (userId === undefined || userId === null) {
      this.fullName = 'unknown';
      return;
    }
    if (userId !== 'System') {
      try {
        this.user = await firstValueFrom(this.userService.getUser(userId));
        if (this.user.firstname !== '' && this.user.lastname !== '') {
          this.fullName = `${this.user.firstname} ${this.user.lastname}`;
        } else {
          this.fullName = `[${userId}]`;
        }
      } catch (error) {
        console.error(
          `Error getting info about user '${userId}'. User card will not be displayed.`
        );
        console.error(error);
        if (userId) {
          this.fullName = userId;
        }
      }
    } else {
      this.fullName = userId;
    }
  }

  @HostListener('document:click', ['$event', '$event.target'])
  public onClick(event: MouseEvent, targetElement: HTMLElement): void {
    if (!targetElement) {
      return;
    }

    const clickedInside = this.elementRef.nativeElement.contains(targetElement);
    if (!clickedInside) {
      this.clickOutside.emit(event);
      this.visible = false;
    }
  }

  public toggleVisible() {
    if (!this.disabled()) {
      this.visible = !this.visible;
    }
  }
}
