import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { provideRouter } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  CategoryService,
  GroupCreationRequest,
} from 'app/core/generated/circabc';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { GroupRequestCreateElementComponent } from './group-request-create-element.component';

const mockRequest: GroupCreationRequest & { interestGroupId?: string } = {
  from: { userId: 'user1', firstname: 'John', lastname: 'Doe' },
  proposedName: 'Test Group',
  justification: 'Testing purposes',
};

describe('GroupRequestCreateElementComponent', () => {
  let component: GroupRequestCreateElementComponent;
  let componentRef: ComponentRef<GroupRequestCreateElementComponent>;
  let fixture: ComponentFixture<GroupRequestCreateElementComponent>;
  let mockDialog: { open: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    mockDialog = { open: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [GroupRequestCreateElementComponent],
      providers: [
        provideRouter([]),
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: CategoryService, useValue: {} },
        { provide: MatDialog, useValue: mockDialog },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(GroupRequestCreateElementComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('request', mockRequest);
    componentRef.setInput('categoryId', 'cat-1');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should open accept dialog and emit reloadGroupRequests on close with truthy response', () => {
    const afterClosed$ = new Subject<boolean>();
    mockDialog.open.mockReturnValue({
      afterClosed: () => afterClosed$.asObservable(),
    } as MatDialogRef<unknown>);

    const emitSpy = vi.spyOn(component.reloadGroupRequests, 'emit');

    component.acceptDialog();

    expect(mockDialog.open).toHaveBeenCalled();
    expect(mockDialog.open.mock.calls[0][1]).toEqual({
      ariaLabel: 'Dialog',
      data: { request: mockRequest, categoryId: 'cat-1' },
    });

    afterClosed$.next(true);
    expect(emitSpy).toHaveBeenCalled();
  });

  it('should not emit reloadGroupRequests when accept dialog closes with falsy response', () => {
    const afterClosed$ = new Subject<boolean>();
    mockDialog.open.mockReturnValue({
      afterClosed: () => afterClosed$.asObservable(),
    } as MatDialogRef<unknown>);

    const emitSpy = vi.spyOn(component.reloadGroupRequests, 'emit');

    component.acceptDialog();
    afterClosed$.next(false);

    expect(emitSpy).not.toHaveBeenCalled();
  });

  it('should open reject dialog and emit reloadGroupRequests on close with truthy response', () => {
    const afterClosed$ = new Subject<boolean>();
    mockDialog.open.mockReturnValue({
      afterClosed: () => afterClosed$.asObservable(),
    } as MatDialogRef<unknown>);

    const emitSpy = vi.spyOn(component.reloadGroupRequests, 'emit');

    component.rejectDialog();

    expect(mockDialog.open).toHaveBeenCalled();

    afterClosed$.next(true);
    expect(emitSpy).toHaveBeenCalled();
  });

  it('should not emit reloadGroupRequests when reject dialog closes with falsy response', () => {
    const afterClosed$ = new Subject<boolean>();
    mockDialog.open.mockReturnValue({
      afterClosed: () => afterClosed$.asObservable(),
    } as MatDialogRef<unknown>);

    const emitSpy = vi.spyOn(component.reloadGroupRequests, 'emit');

    component.rejectDialog();
    afterClosed$.next(false);

    expect(emitSpy).not.toHaveBeenCalled();
  });
});
