import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { DocumentLifecycleComponent } from './document-lifecycle.component';

describe('DocumentLifecycleComponent', () => {
  let component: DocumentLifecycleComponent;
  let paramsSubject: Subject<Record<string, string>>;

  beforeEach(() => {
    paramsSubject = new Subject<Record<string, string>>();

    TestBed.configureTestingModule({
      imports: [DocumentLifecycleComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
        },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(DocumentLifecycleComponent, {
      set: {
        imports: [TranslocoModule, SetTitlePipe],
        schemas: [NO_ERRORS_SCHEMA],
      },
    });

    const fixture = TestBed.createComponent(DocumentLifecycleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should default to showing expired items', () => {
    expect(component.showExpiredItems()).toBe(true);
    expect(component.showDeletedItems()).toBe(false);
  });

  it('should set groupId from route params on init', () => {
    paramsSubject.next({ id: 'group123' });
    expect(component.groupId()).toBe('group123');
  });

  it('should call showExpired when params.expired is "1"', () => {
    component.showDeletedItems.set(true);
    component.showExpiredItems.set(false);
    paramsSubject.next({ id: 'group123', expired: '1' });
    expect(component.showDeletedItems()).toBe(false);
    expect(component.showExpiredItems()).toBe(true);
  });

  it('should not change flags when params.expired is absent', () => {
    component.showDeletedItems.set(true);
    component.showExpiredItems.set(false);
    paramsSubject.next({ id: 'group123' });
    expect(component.showDeletedItems()).toBe(true);
    expect(component.showExpiredItems()).toBe(false);
  });

  it('showDeleted should toggle flags correctly', () => {
    component.showDeleted();
    expect(component.isShowDeleted()).toBe(true);
    expect(component.isShowExpired()).toBe(false);
  });

  it('showExpired should toggle flags correctly', () => {
    component.showDeleted();
    component.showExpired();
    expect(component.isShowDeleted()).toBe(false);
    expect(component.isShowExpired()).toBe(true);
  });
});
