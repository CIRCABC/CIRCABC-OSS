import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  HostListener,
  inject,
  input,
  output,
} from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';

/**
 * Standalone dropdown component (`cbc-add-permission-dropdown`) used in the
 * group permissions area to trigger the creation of new permission entries.
 *
 * It renders a dropdown trigger that, when clicked, toggles a small menu
 * offering two actions: launching the "add permission" wizard or the
 * "add share" wizard. The menu automatically closes when the user clicks
 * anywhere outside of the component (detected via a document-level click
 * listener).
 */
@Component({
  selector: 'cbc-add-permission-dropdown',
  templateUrl: './dropdown.component.html',
  styleUrl: './dropdown.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslocoModule],
})
export class DropdownComponent {
  /**
   * When `true`, the dropdown trigger is disabled and clicking it will not
   * open the menu. Defaults to `false`.
   */
  public readonly disabled = input(false);
  /**
   * Emitted when the user chooses one of the wizard actions.
   * The payload is `false` for the "add permission" wizard and `true` for
   * the "add share" wizard.
   */
  public readonly launchCreate = output<boolean>();
  /**
   * Emitted when a click is detected outside of this component's element,
   * carrying the originating {@link MouseEvent}. Allows parent components to
   * react to outside clicks (the dropdown also closes itself in this case).
   */
  public readonly clickOutside = output<MouseEvent>();

  /** Reference to the component's host element, used for outside-click detection. */
  private readonly elementRef: ElementRef;

  /** Whether the create menu is currently visible. */
  public showCreateDropdown = false;

  /**
   * Creates the component and captures the host {@link ElementRef} required
   * to determine whether document clicks occurred inside or outside the
   * component.
   */
  public constructor() {
    const myElement = inject(ElementRef);

    this.elementRef = myElement;
  }

  /**
   * Document-level click handler that closes the create menu when the click
   * happens outside of this component, emitting {@link clickOutside}.
   *
   * @param event The originating mouse click event.
   * @param targetElement The element that was clicked, or `null` if unavailable.
   */
  @HostListener('document:click', ['$event', '$event.target'])
  public onClick(event: MouseEvent, targetElement: EventTarget | null): void {
    if (!targetElement) {
      return;
    }

    const clickedInside = this.elementRef.nativeElement.contains(targetElement);
    if (!clickedInside) {
      this.clickOutside.emit(event);
      this.showCreateDropdown = false;
    }
  }

  /**
   * Toggles the visibility of the create menu, but only when the component is
   * not disabled and the click originated on the dropdown trigger element
   * (identified by the `dropdown-trigger` CSS class).
   *
   * @param event The DOM click event; its `target` is inspected to ensure the
   * toggle only fires for the trigger element.
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public toggleAddDropdown(event: any) {
    // only trigger display or hide when clicking the dropdown-trigger html element
    if (
      !this.disabled() &&
      event.target.classList.contains('dropdown-trigger')
    ) {
      this.showCreateDropdown = !this.showCreateDropdown;
    }
  }

  /**
   * Closes the create menu and emits {@link launchCreate} with `false` to
   * request opening the "add permission" wizard.
   */
  public launchAddPermissionWizard(): void {
    this.showCreateDropdown = false;
    this.launchCreate.emit(false);
  }

  /**
   * Closes the create menu and emits {@link launchCreate} with `true` to
   * request opening the "add share" wizard.
   */
  public launchAddShareWizard(): void {
    this.showCreateDropdown = false;
    this.launchCreate.emit(true);
  }
}
