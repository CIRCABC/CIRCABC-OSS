import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  effect,
  forwardRef,
  Injector,
  inject,
  input,
  OnDestroy,
  output,
  runInInjectionContext,
  viewChild,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import type { EmitterSource } from 'quill';
import Quill from 'quill';
import { Delta } from 'quill/core';

/**
 * Payload emitted by {@link RichTextEditorComponent} whenever the editor
 * content changes.
 *
 * It bundles the different representations of the current editor state so
 * consumers can pick whichever form they need (rendered HTML, plain text or
 * the low-level Quill delta).
 */
export interface RichTextChangeEvent {
  /** Current editor content serialized as semantic HTML. */
  htmlValue: string;
  /** Current editor content as plain text (markup stripped). */
  textValue: string;
  /** Quill {@link Delta} describing the change that just occurred. */
  delta: Delta;
  /** Origin of the change (e.g. `'user'`, `'api'` or `'silent'`). */
  source: EmitterSource;
}

/**
 * Rich text editor component (`cbc-rich-text-editor`) backed by the Quill
 * editor.
 *
 * Renders a Quill "snow" theme editor into the component's template, wiring
 * an optional toolbar (any element matching `.editor-toolbar` inside the
 * component) into Quill's toolbar module. The component implements
 * {@link ControlValueAccessor}, so it can be used directly with Angular
 * forms (`ngModel`, `formControl`, `formControlName`); the form value is the
 * editor content serialized as semantic HTML.
 *
 * In addition to the standard form-control value propagation, it exposes a
 * {@link textChange} output carrying richer change details via
 * {@link RichTextChangeEvent}.
 */
@Component({
  selector: 'cbc-rich-text-editor',
  templateUrl: './rich-text-editor.component.html',
  styleUrl: './rich-text-editor.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => RichTextEditorComponent),
      multi: true,
    },
  ],
})
export class RichTextEditorComponent
  implements AfterViewInit, OnDestroy, ControlValueAccessor
{
  /** Reference to the host element, used to locate the optional toolbar. */
  private readonly elementRef = inject(ElementRef);
  /** Injector used to create a reactive {@link effect} outside the constructor. */
  private readonly injector = inject(Injector);

  /**
   * Height of the editor area (e.g., '320px').
   * @input
   */
  readonly editorHeight = input('320px');

  /**
   * Whether the editor is read-only.
   * @input
   */
  readonly readOnly = input(false);

  /**
   * Emitted on every text change for consumers that need the event.
   * @output
   */
  readonly textChange = output<RichTextChangeEvent>();

  /** Container element into which the Quill instance is mounted. */
  private readonly editorContainer =
    viewChild.required<ElementRef>('editorContainer');

  /** The underlying Quill editor instance (created in {@link ngAfterViewInit}). */
  private quill!: Quill;
  /** Current HTML value backing the form control. */
  private value = '';
  /** Whether the editor is disabled via the form control. */
  private disabled = false;

  /** Callback registered by Angular forms to propagate value changes. */
  private onChange: (value: string) => void = () => {};
  /** Callback registered by Angular forms to mark the control as touched. */
  private onTouched: () => void = () => {};

  /**
   * Lifecycle hook that instantiates Quill once the view is ready.
   *
   * Wires up the optional toolbar, applies any pending initial value,
   * subscribes to Quill's `text-change` (to propagate the value and emit
   * {@link textChange}) and `selection-change` (to mark the control as
   * touched) events, applies the initial disabled state, and sets up a
   * reactive effect that enables/disables the editor based on
   * {@link readOnly}.
   */
  ngAfterViewInit(): void {
    const toolbarElement =
      this.elementRef.nativeElement.querySelector('.editor-toolbar');

    this.quill = new Quill(this.editorContainer().nativeElement, {
      theme: 'snow',
      modules: {
        toolbar: toolbarElement ?? true,
      },
    });

    // Set initial value
    if (this.value) {
      this.quill.clipboard.dangerouslyPasteHTML(this.value);
    }

    // Sync editor changes back to form control
    this.quill.on('text-change', (delta, _oldDelta, source) => {
      const htmlValue = this.quill.getSemanticHTML();
      const textValue = this.quill.getText();

      this.onChange(htmlValue);
      this.textChange.emit({
        htmlValue,
        textValue,
        delta,
        source,
      });
    });

    // Mark as touched on selection/focus
    this.quill.on('selection-change', (range) => {
      if (range) {
        this.onTouched();
      }
    });

    if (this.disabled) {
      this.quill.disable();
    }

    // Handle readonly input reactively
    runInInjectionContext(this.injector, () => {
      effect(() => {
        const isReadOnly = this.readOnly();
        if (this.quill) {
          this.quill.enable(!isReadOnly);
        }
      });
    });
  }

  /**
   * Lifecycle hook that tears down Quill event listeners on destruction.
   *
   * Quill does not expose a `destroy` method, so removing the registered
   * `text-change` and `selection-change` handlers is sufficient cleanup.
   */
  ngOnDestroy(): void {
    // Quill doesn't have a destroy method; clearing listeners is sufficient
    if (this.quill) {
      this.quill.off('text-change');
      this.quill.off('selection-change');
    }
  }

  // ControlValueAccessor implementation

  /**
   * Writes a new value from the form model into the editor.
   *
   * Stores the value and, if Quill is already initialized and its current
   * HTML differs, replaces the editor content with the provided HTML.
   * @param value The HTML value to display (falsy values become an empty string).
   */
  writeValue(value: string): void {
    this.value = value || '';
    if (this.quill) {
      const currentHtml = this.quill.getSemanticHTML();
      if (currentHtml !== this.value) {
        this.quill.clipboard.dangerouslyPasteHTML(this.value);
      }
    }
  }

  /**
   * Registers the callback invoked when the editor value changes.
   * @param fn Callback provided by Angular's forms API.
   */
  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  /**
   * Registers the callback invoked when the control is touched.
   * @param fn Callback provided by Angular's forms API.
   */
  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  /**
   * Sets the disabled state of the editor from the form model.
   *
   * Records the state and, when Quill is initialized, enables or disables
   * the editor accordingly.
   * @param isDisabled `true` to disable the editor, `false` to enable it.
   */
  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
    if (this.quill) {
      if (isDisabled) {
        this.quill.disable();
      } else {
        this.quill.enable();
      }
    }
  }
}
