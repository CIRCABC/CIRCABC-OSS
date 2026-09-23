import {
  ChangeDetectionStrategy,
  Component,
  computed,
  ElementRef,
  HostListener,
  inject,
  input,
  output,
  resource,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';

import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { ActionEmitterResult } from 'app/action-result';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import { Node as ModelNode, SpaceService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { AddContentComponent } from 'app/group/library/add-content/add-content.component';
import { AddSharedSpaceLinkComponent } from 'app/group/library/add-shared-space-link/add-shared-space-link.component';
import { AddSpaceComponent } from 'app/group/library/add-space/add-space.component';
import { AddUrlComponent } from 'app/group/library/add-url/add-url.component';
import { ImportComponent } from 'app/group/library/import/import.component';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { IfOrRolesPipe } from 'app/shared/pipes/if-or-roles.pipe';

/**
 * Dropdown menu component for the document library that groups together the
 * various "add" actions available on the currently selected node/space.
 *
 * It renders a toggleable dropdown that, depending on the caller-provided
 * feature flags and the user's permissions, exposes entry points to:
 * create a folder (space), upload content, add a URL, add a shared-space
 * link and import content. Each action opens its dedicated child component
 * (wizard or modal) and the results are propagated back to the parent via
 * the {@link AddDropdownComponent.actionFinished} output.
 *
 * The component also listens for document-level clicks so it can close
 * itself when the user clicks outside of it.
 *
 * Key collaborators:
 * - {@link SpaceService} to load the shared spaces exported from the current space.
 * - {@link PermissionEvaluatorService} to determine whether the current user
 *   may manage content on the current node.
 * - {@link LoginService} to detect guest users.
 */
@Component({
  selector: 'cbc-add-dropdown',
  templateUrl: './add-dropdown.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    DataCyDirective,
    RouterLink,
    AddSpaceComponent,
    AddContentComponent,
    AddUrlComponent,
    AddSharedSpaceLinkComponent,
    ImportComponent,
    IfOrRolesPipe,
    TranslocoModule,
  ],
})
export class AddDropdownComponent {
  /** Provides access to the current route parameters (used to read the space id). */
  private readonly route = inject(ActivatedRoute);
  /** Backend client used to retrieve the shared spaces exported by the current space. */
  private readonly spaceService = inject(SpaceService);
  /** Evaluates the current user's permissions on the current node. */
  private readonly permEvalService = inject(PermissionEvaluatorService);
  /** Provides information about the authenticated user (e.g. guest detection). */
  private readonly loginService = inject(LoginService);

  /** When `true`, the "add file / upload content" action is available. Defaults to `true`. */
  public readonly enableAddFile = input(true);
  /** When `true`, the "add folder / create space" action is available. Defaults to `true`. */
  public readonly enableAddFolder = input(true);
  /** When `true`, the "add URL" action is available. Defaults to `true`. */
  public readonly enableAddUrl = input(true);
  /** When `true`, the "add shared space link" action is available. Defaults to `true`. */
  public readonly enableAddSharedSpaceLink = input(true);
  /** When `true`, the "import" action is available. Defaults to `true`. */
  public readonly enableAddImport = input(true);

  /** Required input: the node/space the add actions operate on. */
  public readonly currentNode = input.required<ModelNode>();
  /** Emits once one of the add actions completes, carrying its result to the parent. */
  public readonly actionFinished = output<ActionEmitterResult>();
  /** Emits when a click is detected outside of this component. */
  public readonly clickOutside = output<MouseEvent>();

  /** Whether the dropdown panel is currently visible. */
  public showAddDropdown = false;
  /** Whether the create-space wizard is currently open. */
  public launchCreateSpace = false;
  /** Whether the add-content wizard is currently open. */
  public launchAddContent = false;
  /** Whether the add-URL modal is currently open. */
  public launchAddUrl = false;
  /** Whether the add-shared-space-link modal is currently open. */
  public launchAddSharedSpaceLink = false;
  /** The current route params, as a signal. */
  private readonly routeParams = toSignal(this.route.params);
  /** Identifier of the current space, read from the route parameters. */
  public readonly spaceId = computed(() => this.routeParams()?.id ?? '');
  /**
   * Resource loading the shared spaces exported by the current space,
   * identified by {@link spaceId}. Idle (loader not called) while the space
   * id is not yet known.
   */
  private readonly sharedSpaceItemsResource = resource({
    params: () => this.spaceId() || undefined,
    loader: ({ params: id }) =>
      this.spaceService.getExportedSharedSpacesAsync({ id }),
  });
  /** Shared spaces exported by the current space, loaded from the backend. */
  public readonly sharedSpaceItems = computed(
    () => this.sharedSpaceItemsResource.value() ?? []
  );
  /** Reference to this component's host element, used for outside-click detection. */
  private readonly elementRef: ElementRef;
  /** Whether the import modal is currently open. */
  public showModalImport = false;
  /** Whether the current user is a guest (unauthenticated) user. */
  public readonly isGuest = this.loginService.isGuest();

  /**
   * Creates the component and captures a reference to its host element so that
   * outside-click detection can be performed in {@link AddDropdownComponent.onClick}.
   */
  public constructor() {
    const myElement = inject(ElementRef);

    this.elementRef = myElement;
  }

  /**
   * Document-level click handler that closes the dropdown when the user clicks
   * outside of this component, emitting {@link AddDropdownComponent.clickOutside}.
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
      this.showAddDropdown = false;
    }
  }

  /**
   * Indicates whether any shared space items are currently available.
   *
   * @returns `true` if at least one shared space item has been loaded, otherwise `false`.
   */
  public hasSharedSpaceItems() {
    return this.sharedSpaceItems().length > 0;
  }

  /**
   * Toggles the visibility of the dropdown, but only when the triggering element
   * is the dropdown trigger itself (identified by the `dropdown-trigger` CSS class).
   *
   * @param event The click event; its target is inspected to decide whether to toggle.
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public toggleAddDropdown(event: any) {
    // only trigger display or hide when clicking the dropdown-trigger html element
    if (event.target.classList.contains('dropdown-trigger')) {
      this.showAddDropdown = !this.showAddDropdown;
    }
  }

  /**
   * Closes the dropdown and toggles the create-space wizard open/closed.
   */
  public launchCreateSpaceWizard(): void {
    this.showAddDropdown = false;
    this.launchCreateSpace = !this.launchCreateSpace;
  }

  /**
   * Closes the dropdown and toggles the add-content (upload) wizard open/closed.
   */
  public launchAddWizardStep1(): void {
    this.showAddDropdown = false;
    this.launchAddContent = !this.launchAddContent;
  }

  /**
   * Closes the dropdown and opens the add-URL modal.
   */
  public launchAddUrlModal(): void {
    this.showAddDropdown = false;
    this.launchAddUrl = true;
  }

  /**
   * Closes the dropdown and opens the add-shared-space-link modal.
   */
  public launchSharedSpaceLinkModal(): void {
    this.showAddDropdown = false;
    this.launchAddSharedSpaceLink = true;
  }

  /**
   * Closes the create-space wizard and forwards its result to the parent
   * via {@link AddDropdownComponent.actionFinished}.
   *
   * @param result The outcome of the create-space action.
   */
  public propagateCreateSpaceClosure(result: ActionEmitterResult): void {
    this.launchCreateSpace = false;
    this.actionFinished.emit(result);
  }

  /**
   * Closes the add-content wizard and forwards its result to the parent
   * via {@link AddDropdownComponent.actionFinished}.
   *
   * @param result The outcome of the upload-files action.
   */
  public propagateUploadFilesClosure(result: ActionEmitterResult): void {
    this.launchAddContent = false;
    this.actionFinished.emit(result);
  }

  /**
   * Closes the add-URL modal and forwards its result to the parent
   * via {@link AddDropdownComponent.actionFinished}.
   *
   * @param result The outcome of the add-URL action.
   */
  public propagateAddUrlClosure(result: ActionEmitterResult): void {
    this.launchAddUrl = false;
    this.actionFinished.emit(result);
  }

  /**
   * Closes the add-shared-space-link modal and forwards its result to the parent
   * via {@link AddDropdownComponent.actionFinished}.
   *
   * @param result The outcome of the add-link action.
   */
  public propagateAddLinkClosure(result: ActionEmitterResult): void {
    this.launchAddSharedSpaceLink = false;
    this.actionFinished.emit(result);
  }

  /**
   * Closes the import modal and forwards its result to the parent
   * via {@link AddDropdownComponent.actionFinished}.
   *
   * @param result The outcome of the import action.
   */
  public propagateAfterImportClosure(result: ActionEmitterResult): void {
    this.showModalImport = false;
    this.actionFinished.emit(result);
  }

  /**
   * Closes the dropdown and opens the import modal.
   */
  public showImport() {
    this.showAddDropdown = false;
    this.showModalImport = true;
  }

  /**
   * Determines whether the current user is allowed to manage their own content
   * (or has a higher permission level) on the current node.
   *
   * @returns `true` if the user has "manage own" or higher permissions, otherwise `false`.
   */
  public isLibManageOwn(): boolean {
    return this.permEvalService.isLibManageOwnOrHigher(this.currentNode());
  }
}
