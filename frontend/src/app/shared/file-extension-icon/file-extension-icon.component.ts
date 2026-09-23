import { ChangeDetectionStrategy, Component, input } from '@angular/core';

import { knownExtensionsList } from 'app/shared/file-extension-icon/known-extensions';
import { environment } from 'environments/environment';

/**
 * Standalone presentational component (`cbc-file-extension-icon`) that renders a
 * small icon image representing the file type of a document.
 *
 * The icon is chosen from a static set of 24px grey file-format images shipped
 * with the application. The file extension is derived from the provided
 * {@link FileExtensionIconComponent.filename} and matched against the
 * {@link knownExtensionsList}; when no match is found a generic "blank" icon is
 * used as a fallback.
 *
 * @remarks
 * Uses OnPush change detection and preserves whitespace in its template.
 */
@Component({
  selector: 'cbc-file-extension-icon',
  templateUrl: './file-extension-icon.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  preserveWhitespaces: true,
})
export class FileExtensionIconComponent {
  /**
   * Optional input holding the name of the file whose extension icon should be
   * displayed. The extension is extracted from the substring following the last
   * dot in the name.
   */
  readonly filename = input<string>();
  /**
   * Optional input carrying the MIME type of the file.
   *
   * @remarks
   * Currently informational only; the displayed icon is resolved from the
   * filename extension rather than this value.
   */
  readonly mimtype = input<string>();

  /**
   * Derives the file extension used to select the icon image.
   *
   * Extracts the text after the last dot in {@link filename} and returns it only
   * when it is part of {@link knownExtensionsList}. When there is no filename, no
   * dot, or the extension is not recognised, the fallback value `'blank'` is
   * returned.
   *
   * @returns The recognised lowercase-agnostic file extension, or `'blank'` when
   * none can be determined.
   */
  getExtension(): string {
    const filename = this.filename();
    if (filename) {
      const dotIndex = filename.lastIndexOf('.');
      if (dotIndex !== -1) {
        const ext = filename.substring(dotIndex + 1);
        if (knownExtensionsList.includes(ext)) {
          return ext;
        }
      }
    }
    return 'blank';
  }

  /**
   * Builds the fully-qualified path to the icon image for the current file.
   *
   * The path is composed from the application's configured `baseHref`, the
   * `img/file-extensions/24-grey/` folder and the extension resolved by
   * {@link getExtension}.
   *
   * @returns The relative URL of the 24px grey file-format icon to display.
   */
  getFilePath(): string {
    return `${
      environment.baseHref
    }img/file-extensions/24-grey/${this.getExtension()}-file-format.png`;
  }
}
