import { DatePipe } from '@angular/common';
import { Component, Input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { type AppMessage, AppMessageService } from 'app/core/generated/circabc';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-template-renderer',
  templateUrl: './template-renderer.component.html',
  styleUrl: './template-renderer.component.scss',
  preserveWhitespaces: true,
  imports: [RouterLink, InlineDeleteComponent, DatePipe, TranslocoModule],
})
export class TemplateRendererComponent {
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input()
  template!: AppMessage;
  readonly delete = output();

  constructor(private appMessageService: AppMessageService) {}

  public async onDelete() {
    if (this.template?.id) {
      try {
        await firstValueFrom(
          this.appMessageService.deleteAppMessageTemplate(`${this.template.id}`)
        );
      } catch (_error) {
        console.error('problem when deleting a template');
      }

      this.delete.emit();
    }
  }

  public async useAsOldMessage() {
    try {
      await firstValueFrom(this.appMessageService.setOldMessage(this.template));
    } catch (error) {
      console.error(error);
    }
  }
}
