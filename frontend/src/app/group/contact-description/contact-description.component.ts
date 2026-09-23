import { I18nSelectPipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
} from '@angular/core';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { type InterestGroup } from 'app/core/generated/circabc';

/**
 * Renders the contact information section for an interest group.
 *
 * The component displays the group's contact details, resolving the
 * appropriate multilingual value based on the currently active Transloco
 * language (falling back to the default language when a translation is
 * missing). It exposes helper methods used by its template to decide
 * whether contact content should be shown and which language variant to
 * display.
 *
 * @remarks Collaborates with {@link TranslocoService} to determine the
 * active and default languages for multilingual value resolution.
 */
@Component({
  selector: 'cbc-contact-description',
  templateUrl: './contact-description.component.html',
  styleUrl: './contact-description.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [I18nSelectPipe, TranslocoModule],
})
export class ContactDescriptionComponent {
  /** Transloco service used to resolve the active and default UI languages. */
  private readonly translateService = inject(TranslocoService);

  /**
   * Required input holding the interest group whose contact information is
   * rendered. Provides both the multilingual `contact` and `description`
   * values consumed by this component.
   */
  public group = input.required<InterestGroup>();

  /**
   * Determines whether the group has a displayable contact value.
   *
   * @returns `true` when the group's `contact` field exists and contains a
   * non-empty value for the resolved language; otherwise `false`.
   */
  hasContact(): boolean {
    const contact = this.group().contact;
    if (contact) {
      return this.hasMLValue(contact);
    }
    return false;
  }

  /**
   * Checks whether a multilingual map contains a usable value for the
   * currently resolved language.
   *
   * @param obj A map of language codes to their translated string values.
   * @returns `true` when the map holds a defined, non-empty value for the
   * resolved language; otherwise `false`.
   */
  hasMLValue(obj: { [key: string]: string }): boolean {
    if (obj) {
      const lang = this.getLang();
      if (obj[lang] !== undefined && obj[lang] !== '') {
        return true;
      }
    }

    return false;
  }

  /**
   * Resolves the language code to use when reading multilingual values.
   *
   * Prefers the active Transloco language when the group's `description`
   * contains a translation for it, otherwise falls back to the default
   * language.
   *
   * @returns The language code to use for multilingual lookups.
   */
  getLang(): string {
    const description = this.group().description;
    if (
      description &&
      Object.keys(description).includes(this.translateService.getActiveLang())
    ) {
      return this.translateService.getActiveLang();
    }
    return this.translateService.getDefaultLang();
  }
}
