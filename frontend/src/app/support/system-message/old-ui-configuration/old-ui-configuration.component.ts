import {
  ChangeDetectionStrategy,
  Component,
  effect,
  inject,
  OnInit,
  resource,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@jsverse/transloco';
import { AppMessageService } from 'app/core/generated/circabc';

/**
 * Support/admin component that renders a small reactive form for configuring
 * the "old UI" system message.
 *
 * The form exposes two toggles:
 * - `enableOld`: whether the old-UI message feature is enabled.
 * - `display`: whether the old-UI message is currently displayed to users.
 *
 * The current configuration is loaded from the backend via
 * {@link AppMessageService} through {@link configResource}. Once loaded, an
 * {@link effect} seeds the (imperative, non-signal) form controls with the
 * fetched values. Value-change listeners, wired up in the constructor, then
 * persist any subsequent change made in the form back through the service.
 *
 * Key collaborators:
 * - {@link AppMessageService} — reads and writes the old-UI message settings.
 * - {@link FormBuilder} — builds the reactive form group.
 */
@Component({
  selector: 'cbc-old-ui-configuration',
  templateUrl: './old-ui-configuration.component.html',
  styleUrl: './old-ui-configuration.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, TranslocoModule],
})
export class OldUiConfigurationComponent implements OnInit {
  /** Generated API service used to read and persist the old-UI message settings. */
  private readonly appMessageService = inject(AppMessageService);
  /** Reactive forms builder used to create the configuration {@link FormGroup}. */
  private readonly fb = inject(FormBuilder);

  /**
   * Reactive form backing the configuration UI.
   *
   * Contains two boolean controls:
   * - `enableOld` — toggles whether the old-UI message feature is enabled.
   * - `display` — toggles whether the old-UI message is displayed.
   *
   * Built eagerly in the constructor; {@link configResource} later seeds its
   * initial values once the backend configuration has loaded.
   */
  public form: FormGroup = this.fb.group({
    enableOld: false,
    display: true,
  });

  /**
   * Loads the current "display" and "enable" old-UI message settings from
   * the backend. Both calls have no interdependency, so they are combined
   * into a single resource that resolves once both have completed.
   */
  private readonly configResource = resource({
    loader: async () => {
      const [config, configOldMessage] = await Promise.all([
        this.appMessageService.getDisplayOldMessageAsync(),
        this.appMessageService.getEnableOldMessageAsync(),
      ]);
      return {
        display: config.display,
        enableOld: configOldMessage.enable,
      };
    },
  });

  constructor() {
    // Seed the (imperative, non-signal) form controls once the backend
    // configuration has loaded. This is a one-way sync from the resource
    // into the reactive form, not a signal-to-signal copy. `emitEvent: false`
    // avoids immediately re-persisting the value that was just loaded.
    effect(() => {
      const config = this.configResource.value();
      if (config) {
        this.form.controls.display.setValue(config.display, {
          emitEvent: false,
        });
        this.form.controls.enableOld.setValue(config.enableOld, {
          emitEvent: false,
        });
      }
    });
  }

  ngOnInit() {
    this.form.controls.display.valueChanges.forEach((value) => {
      void this.updateSettings(value);
    });

    this.form.controls.enableOld.valueChanges.forEach((value) => {
      void this.toggleOldMessage(value);
    });
  }

  /**
   * Persists the "display old message" setting to the backend.
   *
   * Called whenever the `display` control changes. Guards against `undefined`
   * values so no request is issued for indeterminate control states.
   *
   * @param value Whether the old-UI message should be displayed.
   * @returns A promise that resolves once the setting has been saved.
   */
  async updateSettings(value: boolean) {
    if (value !== undefined) {
      await this.appMessageService.setDisplayOldMessageAsync({ body: value });
    }
  }

  /**
   * Persists the "enable old message" setting to the backend.
   *
   * Called whenever the `enableOld` control changes. Guards against `undefined`
   * values so no request is issued for indeterminate control states.
   *
   * @param value Whether the old-UI message feature should be enabled.
   * @returns A promise that resolves once the setting has been saved.
   */
  async toggleOldMessage(value: boolean) {
    if (value !== undefined) {
      await this.appMessageService.setEnableOldMessageAsync({ body: value });
    }
  }
}
