import {
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  OnInit,
  Output,
} from '@angular/core';

import { User, UserService } from 'app/core/generated/circabc';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-user-card',
  templateUrl: './user-card.component.html',
  styleUrls: ['./user-card.component.scss'],
  preserveWhitespaces: true,
})
export class UserCardComponent implements OnInit {
  @Input()
  userId: string | undefined | null;
  @Input()
  rightSide = false;
  @Input()
  disabled = false;
  @Output()
  public readonly clickOutside = new EventEmitter<MouseEvent>();

  public user!: User;
  public visible = false;
  private elementRef: ElementRef;
  public fullName = '';

  constructor(private userService: UserService, myElement: ElementRef) {
    this.elementRef = myElement;
  }

  async ngOnInit() {
    if (this.userId === undefined || this.userId === null) {
      this.fullName = 'unknown';
      return;
    }
    if (this.userId !== 'System') {
      try {
        this.user = await firstValueFrom(this.userService.getUser(this.userId));
        if (this.user.firstname !== '' && this.user.lastname !== '') {
          this.fullName = `${this.user.firstname} ${this.user.lastname}`;
        } else {
          this.fullName = `[${this.userId}]`;
        }
      } catch (error) {
        console.error(
          `Error getting info about user '${this.userId}'. User card will not be displayed.`
        );
        console.error(error);
        if (this.userId) {
          this.fullName = this.userId;
        }
      }
    } else {
      this.fullName = this.userId;
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
    if (!this.disabled) {
      this.visible = !this.visible;
    }
  }
}
