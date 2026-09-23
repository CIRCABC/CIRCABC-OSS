import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { SizePipe } from 'app/shared/pipes/size.pipe';

/**
 * Presentational component that displays metadata for a single file selected
 * during the "add content" flow of the document library.
 *
 * Rendered via the `cbc-file-details` selector, its template shows details of
 * the provided {@link File} (such as its name and size, the latter formatted
 * through {@link SizePipe}) with labels translated using Transloco.
 *
 * The component is purely display-oriented: it takes a required `File` input
 * and does not emit any outputs or mutate the file.
 */
@Component({
  selector: 'cbc-file-details',
  templateUrl: './file-details.component.html',
  styleUrl: './file-details.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [SizePipe, TranslocoModule],
})
export class FileDetailsComponent {
  /**
   * Required signal input holding the browser {@link File} whose details are
   * rendered by the component. Must be supplied by the parent component.
   */
  public readonly file = input.required<File>();
}
