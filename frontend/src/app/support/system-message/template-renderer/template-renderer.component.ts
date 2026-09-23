import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  output,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { type AppMessage, AppMessageService } from 'app/core/generated/circabc';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';

/**
 * Renders a single system message template ({@link AppMessage}) and exposes
 * the actions that can be performed on it.
 *
 * The component displays the details of a stored app message template and lets
 * an administrator either delete the template or promote it to be the current
 * "old message" that is shown across the platform. Delete and promote actions
 * are delegated to the {@link AppMessageService} REST client, and a `delete`
 * output notifies the parent component so it can refresh its view.
 *
 * @remarks
 * Uses OnPush change detection and preserves whitespace so the rendered
 * template markup matches the stored content faithfully.
 */
@Component({
  selector: 'cbc-template-renderer',
  templateUrl: './template-renderer.component.html',
  styleUrl: './template-renderer.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, InlineDeleteComponent, DatePipe, TranslocoModule],
})
export class TemplateRendererComponent {
  /**
   * REST client used to delete a template and to set a template as the current
   * old message.
   */
  private readonly appMessageService = inject(AppMessageService);

  /**
   * Required input holding the system message template to render and act upon.
   */
  template = input.required<AppMessage>();

  /**
   * Emitted after the current template has been successfully deleted so the
   * parent component can refresh its list.
   */
  readonly delete = output();

  /**
   * Deletes the current template via {@link AppMessageService} and, on success,
   * emits the {@link delete} output.
   *
   * @remarks
   * The deletion is only attempted when the template has an `id`. Any error
   * raised by the service is caught and logged, in which case the {@link delete}
   * output is not emitted.
   *
   * @returns A promise that resolves once the delete attempt has completed.
   */
  public async onDelete() {
    if (this.template().id) {
      try {
        await this.appMessageService.deleteAppMessageTemplateAsync({
          id: `${this.template().id}`,
        });
      } catch (_error) {
        console.error('problem when deleting a template');
      }

      this.delete.emit();
    }
  }

  /**
   * Promotes the current template to be the platform's "old message" via
   * {@link AppMessageService}.
   *
   * @remarks
   * Any error raised by the service is caught and logged.
   *
   * @returns A promise that resolves once the operation has completed.
   */
  public async useAsOldMessage() {
    try {
      await this.appMessageService.setOldMessageAsync({
        appMessage: this.template(),
      });
    } catch (error) {
      console.error(error);
    }
  }
}
