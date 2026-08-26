import {
  Component,
  ElementRef,
  HostListener,
  Input,
  OnInit,
  output,
  input,
} from '@angular/core';

import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { ActionEmitterResult } from 'app/action-result';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  Node as ModelNode,
  ShareSpaceItem,
  SpaceService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { AddContentComponent } from 'app/group/library/add-content/add-content.component';
import { AddSharedSpaceLinkComponent } from 'app/group/library/add-shared-space-link/add-shared-space-link.component';
import { AddSpaceComponent } from 'app/group/library/add-space/add-space.component';
import { AddUrlComponent } from 'app/group/library/add-url/add-url.component';
import { ImportComponent } from 'app/group/library/import/import.component';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { IfOrRolesPipe } from 'app/shared/pipes/if-or-roles.pipe';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-add-dropdown',
  templateUrl: './add-dropdown.component.html',
  preserveWhitespaces: true,
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
export class AddDropdownComponent implements OnInit {
  public readonly enableAddFile = input(true);
  public readonly enableAddFolder = input(true);
  public readonly enableAddUrl = input(true);
  public readonly enableAddSharedSpaceLink = input(true);
  public readonly enableAddImport = input(true);
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input()
  public currentNode!: ModelNode;
  public readonly actionFinished = output<ActionEmitterResult>();
  public readonly clickOutside = output<MouseEvent>();

  public showAddDropdown = false;
  public launchCreateSpace = false;
  public launchAddContent = false;
  public launchAddUrl = false;
  public launchAddSharedSpaceLink = false;
  public sharedSpaceItems!: ShareSpaceItem[];
  public spaceId!: string;
  private elementRef: ElementRef;
  public showModalImport = false;
  public isGuest = false;

  public constructor(
    myElement: ElementRef,
    private route: ActivatedRoute,
    private spaceService: SpaceService,
    private permEvalService: PermissionEvaluatorService,
    private loginService: LoginService
  ) {
    this.elementRef = myElement;
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

  async ngOnInit() {
    this.isGuest = this.loginService.isGuest();

    this.route.params.subscribe(
      async (params) => await this.loadShares(params)
    );
  }

  public async loadShares(params: { [key: string]: string }) {
    this.spaceId = params.id;
    if (this.spaceId) {
      this.sharedSpaceItems = await firstValueFrom(
        this.spaceService.getExportedSharedSpaces(this.spaceId)
      );
    }
  }

  public hasSharedSpaceItems() {
    return (
      this.sharedSpaceItems !== undefined && this.sharedSpaceItems.length > 0
    );
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public toggleAddDropdown(event: any) {
    // only trigger display or hide when clicking the dropdown-trigger html element
    if (event.target.classList.contains('dropdown-trigger')) {
      this.showAddDropdown = !this.showAddDropdown;
    }
  }

  public launchCreateSpaceWizard(): void {
    this.showAddDropdown = false;
    this.launchCreateSpace = !this.launchCreateSpace;
  }

  public launchAddWizardStep1(): void {
    this.showAddDropdown = false;
    this.launchAddContent = !this.launchAddContent;
  }

  public launchAddUrlModal(): void {
    this.showAddDropdown = false;
    this.launchAddUrl = true;
  }

  public launchSharedSpaceLinkModal(): void {
    this.showAddDropdown = false;
    this.launchAddSharedSpaceLink = true;
  }

  public propagateCreateSpaceClosure(result: ActionEmitterResult): void {
    this.launchCreateSpace = false;
    this.actionFinished.emit(result);
  }

  public propagateUploadFilesClosure(result: ActionEmitterResult): void {
    this.launchAddContent = false;
    this.actionFinished.emit(result);
  }

  public propagateAddUrlClosure(result: ActionEmitterResult): void {
    this.launchAddUrl = false;
    this.actionFinished.emit(result);
  }

  public propagateAddLinkClosure(result: ActionEmitterResult): void {
    this.launchAddSharedSpaceLink = false;
    this.actionFinished.emit(result);
  }

  public propagateAfterImportClosure(result: ActionEmitterResult): void {
    this.showModalImport = false;
    this.actionFinished.emit(result);
  }

  public showImport() {
    this.showAddDropdown = false;
    this.showModalImport = true;
  }

  public isLibManageOwn(): boolean {
    return this.permEvalService.isLibManageOwnOrHigher(this.currentNode);
  }
}
