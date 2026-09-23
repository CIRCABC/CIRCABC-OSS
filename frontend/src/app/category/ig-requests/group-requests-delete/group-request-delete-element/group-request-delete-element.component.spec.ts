import { DatePipe } from '@angular/common';
import type { ComponentRef } from '@angular/core';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import type { GroupDeletionRequest } from 'app/core/generated/circabc';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { GroupRequestDeleteElementComponent } from './group-request-delete-element.component';

const mockRequest: GroupDeletionRequest = {
  from: { userId: 'user1', firstname: 'John', lastname: 'Doe' },
  justification: 'Test&nbsp;justification',
};

describe('GroupRequestDeleteElementComponent', () => {
  let component: GroupRequestDeleteElementComponent;
  let componentRef: ComponentRef<GroupRequestDeleteElementComponent>;
  let fixture: ComponentFixture<GroupRequestDeleteElementComponent>;
  let mockDialog: { open: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    mockDialog = { open: vi.fn() };

    TestBed.configureTestingModule({
      imports: [GroupRequestDeleteElementComponent],
      providers: [
        { provide: MatDialog, useValue: mockDialog },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).overrideComponent(GroupRequestDeleteElementComponent, {
      set: {
        imports: [TranslocoModule, DatePipe],
        schemas: [NO_ERRORS_SCHEMA],
      },
    });

    fixture = TestBed.createComponent(GroupRequestDeleteElementComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('request', mockRequest);
    componentRef.setInput('categoryId', 'cat1');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('cleanSpace', () => {
    it('should replace &nbsp; with spaces', () => {
      expect(component.cleanSpace('hello&nbsp;world')).toBe('hello world');
    });

    it('should return empty string for undefined', () => {
      expect(component.cleanSpace(undefined)).toBe('');
    });

    it('should return empty string for empty string', () => {
      expect(component.cleanSpace('')).toBe('');
    });
  });

  describe('acceptDialog', () => {
    it('should open dialog and emit reloadGroupRequests on truthy close', () => {
      const afterClosed$ = new Subject<boolean>();
      mockDialog.open.mockReturnValue({ afterClosed: () => afterClosed$ });

      const emitSpy = vi.spyOn(component.reloadGroupRequests, 'emit');

      component.acceptDialog();

      expect(mockDialog.open).toHaveBeenCalled();

      afterClosed$.next(true);
      expect(emitSpy).toHaveBeenCalled();
    });

    it('should not emit reloadGroupRequests on falsy close', () => {
      const afterClosed$ = new Subject<boolean>();
      mockDialog.open.mockReturnValue({ afterClosed: () => afterClosed$ });

      const emitSpy = vi.spyOn(component.reloadGroupRequests, 'emit');

      component.acceptDialog();
      afterClosed$.next(false);

      expect(emitSpy).not.toHaveBeenCalled();
    });
  });

  describe('rejectDialog', () => {
    it('should open dialog and emit reloadGroupRequests on truthy close', () => {
      const afterClosed$ = new Subject<boolean>();
      mockDialog.open.mockReturnValue({ afterClosed: () => afterClosed$ });

      const emitSpy = vi.spyOn(component.reloadGroupRequests, 'emit');

      component.rejectDialog();

      expect(mockDialog.open).toHaveBeenCalled();

      afterClosed$.next(true);
      expect(emitSpy).toHaveBeenCalled();
    });

    it('should not emit reloadGroupRequests on falsy close', () => {
      const afterClosed$ = new Subject<boolean>();
      mockDialog.open.mockReturnValue({ afterClosed: () => afterClosed$ });

      const emitSpy = vi.spyOn(component.reloadGroupRequests, 'emit');

      component.rejectDialog();
      afterClosed$.next(false);

      expect(emitSpy).not.toHaveBeenCalled();
    });
  });
});
