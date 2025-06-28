import {
  Component,
  ElementRef,
  HostListener,
  Input,
  output,
  input,
} from '@angular/core';

import { TranslocoModule } from '@jsverse/transloco';
import { ActionEmitterResult } from 'app/action-result';
import { Node as ModelNode } from 'app/core/generated/circabc';
import { CreateForumComponent } from 'app/group/forum/create-forum/create-forum.component';
import { CreateTopicComponent } from 'app/group/forum/topic/create-topic.component';

@Component({
  selector: 'cbc-forum-dropdown',
  templateUrl: './forum-dropdown.component.html',
  preserveWhitespaces: true,
  imports: [CreateForumComponent, CreateTopicComponent, TranslocoModule],
})
export class ForumDropdownComponent {
  public readonly enableAddTopic = input(true);
  public readonly enableAddForum = input(true);
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input()
  public currentNode!: ModelNode;
  public readonly actionFinished = output<ActionEmitterResult>();
  public readonly clickOutside = output<MouseEvent>();

  public showAddDropdown = false;
  public launchCreateForum = false;
  public launchAddTopic = false;
  private elementRef: ElementRef;

  public constructor(myElement: ElementRef) {
    this.elementRef = myElement;
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public toggleAddDropdown(event: any) {
    // only trigger display or hide when clicking the dropdown-trigger html element
    if (event.target.classList.contains('dropdown-trigger')) {
      this.showAddDropdown = !this.showAddDropdown;
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
      this.showAddDropdown = false;
    }
  }

  public launchCreateForumWizard(): void {
    this.showAddDropdown = false;
    this.launchCreateForum = !this.launchCreateForum;
  }

  public launchCreateTopicWizard(): void {
    this.showAddDropdown = false;
    this.launchAddTopic = !this.launchAddTopic;
  }

  public propagateCreateForumClosure(result: ActionEmitterResult): void {
    this.launchCreateForum = false;
    this.actionFinished.emit(result);
  }

  public propagateCreateTopic(result: ActionEmitterResult): void {
    this.launchAddTopic = false;
    this.actionFinished.emit(result);
  }

  public isOnlyOneAction(): boolean {
    const enableAddForum = this.enableAddForum();
    const enableAddTopic = this.enableAddTopic();
    return (
      (enableAddForum && !enableAddTopic) || (!enableAddForum && enableAddTopic)
    );
  }
}
