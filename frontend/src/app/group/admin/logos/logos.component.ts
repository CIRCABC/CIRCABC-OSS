import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { ActionEmitterResult, ActionResult } from 'app/action-result';
import {
  InterestGroup,
  InterestGroupService,
  Node as ModelNode,
} from 'app/core/generated/circabc';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { DownloadPipe } from 'app/shared/pipes/download.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { AddGroupLogoComponent } from './add-group-logo/add-group-logo.component';

/**
 * Group logo administration component.
 *
 * Renders the logo management view for an interest group: it lists the logos
 * currently uploaded for the group, highlights the one that is active,
 * and lets an administrator select a different logo, delete a logo, or upload
 * a new one (via the embedded {@link AddGroupLogoComponent} modal and the
 * inline delete control from {@link InlineDeleteComponent}).
 *
 * The group identifier is read from the activated route parameters, and all
 * logo/group operations are delegated to the {@link InterestGroupService}.
 */
@Component({
  selector: 'cbc-logos',
  templateUrl: './logos.component.html',
  styleUrl: './logos.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    InlineDeleteComponent,
    AddGroupLogoComponent,
    DownloadPipe,
    SecurePipe,
    SetTitlePipe,
    TranslocoModule,
  ],
})
export class LogosComponent implements OnInit {
  /** Provides access to the current route parameters (notably the group id). */
  private readonly route = inject(ActivatedRoute);
  /** Backend service used to load the group and to read/select/delete its logos. */
  private readonly groupsService = inject(InterestGroupService);

  /** The interest group currently being administered, once loaded. */
  public readonly group = signal<InterestGroup | undefined>(undefined);
  /** The list of logo nodes available for the current group. */
  public readonly logos = signal<ModelNode[]>([]);
  /** Controls the visibility of the "add logo" upload modal. */
  public readonly showUploadModal = signal(false);

  /**
   * Angular lifecycle hook. Subscribes to route parameter changes and loads
   * the group (and its logos) whenever an `id` parameter is present.
   */
  ngOnInit() {
    this.route.params.subscribe(async (params) => {
      if (params.id) {
        await this.loadGroup(params.id);
      }
    });
  }

  /**
   * Loads the interest group and its logos for the given identifier and
   * stores them on the component. Does nothing when no id is provided.
   *
   * @param id The identifier of the interest group to load.
   * @returns A promise that resolves once the group and its logos are loaded.
   */
  private async loadGroup(id: string) {
    if (id) {
      this.group.set(await this.groupsService.getInterestGroupAsync({ id }));
      this.logos.set(await this.groupsService.getGroupLogosAsync({ id }));
    }
  }

  /**
   * Determines whether the given logo is the one currently selected for the
   * group, by checking if the group's `logoUrl` references the logo id.
   *
   * @param id The identifier of the logo to test.
   * @returns `true` if the logo is the active logo for the group; otherwise `false`.
   */
  public isSelected(id: string | undefined): boolean {
    const group = this.group();
    if (id && group?.logoUrl) {
      if (group.logoUrl.includes(id)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Sets the given logo as the active logo for the current group and reloads
   * the group state to reflect the change. Does nothing if either the logo id
   * or the group id is missing.
   *
   * @param id The identifier of the logo to select.
   * @returns A promise that resolves once the logo has been selected and the
   * group reloaded.
   */
  public async select(id: string | undefined) {
    const group = this.group();
    if (id && group?.id) {
      await this.groupsService.selectGroupLogoAsync({
        id: group.id,
        logoId: id,
      });
      await this.loadGroup(group.id);
    }
  }

  /**
   * Deletes the given logo from the current group. If the logo being deleted
   * is the currently selected one, it is first deselected. The group state is
   * reloaded afterwards. Does nothing if either the logo id or the group id is
   * missing.
   *
   * @param id The identifier of the logo to delete.
   * @returns A promise that resolves once the logo has been deleted and the
   * group reloaded.
   */
  public async delete(id: string | undefined) {
    const group = this.group();
    if (id && group?.id) {
      if (this.isSelected(id)) {
        await this.groupsService.selectGroupLogoAsync({
          id: group.id,
          logoId: id,
        });
      }
      await this.groupsService.deleteGroupLogoAsync({
        id: group.id,
        logoId: id,
      });
      await this.loadGroup(group.id);
    }
  }

  /**
   * Handles the result emitted by the add-logo modal. On a successful action
   * the group and its logos are reloaded. The upload modal is closed in all
   * cases.
   *
   * @param res The action result emitted by the upload modal.
   * @returns A promise that resolves once any required reload has completed.
   */
  public async refresh(res: ActionEmitterResult) {
    const group = this.group();
    if (res.result === ActionResult.SUCCEED && group?.id) {
      await this.loadGroup(group.id);
    }
    this.showUploadModal.set(false);
  }
}
