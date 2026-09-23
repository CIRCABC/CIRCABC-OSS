import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ExternalRepositoryComponent } from './external-repository.component';

describe('ExternalRepositoryComponent', () => {
  function createComponent() {
    TestBed.configureTestingModule({
      imports: [ExternalRepositoryComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(ExternalRepositoryComponent, {
      set: {
        imports: [TranslocoModule, SetTitlePipe],
        schemas: [NO_ERRORS_SCHEMA],
      },
    });

    const fixture = TestBed.createComponent(ExternalRepositoryComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('should create', () => {
    expect(createComponent().componentInstance).toBeDefined();
  });

  it('should show properties by default', () => {
    const comp = createComponent().componentInstance;
    expect(comp.isShowProperties()).toBe(true);
    expect(comp.isShowHistory()).toBe(false);
  });

  it('should switch to history view', () => {
    const comp = createComponent().componentInstance;
    comp.showHistory();
    expect(comp.isShowHistory()).toBe(true);
    expect(comp.isShowProperties()).toBe(false);
  });

  it('should switch back to properties view', () => {
    const comp = createComponent().componentInstance;
    comp.showHistory();
    comp.showProperties();
    expect(comp.isShowProperties()).toBe(true);
    expect(comp.isShowHistory()).toBe(false);
  });
});
