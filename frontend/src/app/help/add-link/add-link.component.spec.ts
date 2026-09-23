import { SimpleChange } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult } from 'app/action-result';
import { HelpService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { AddLinkComponent } from './add-link.component';

describe('AddLinkComponent', () => {
  let component: AddLinkComponent;
  let fixture: ComponentFixture<AddLinkComponent>;
  let mockHelpService: {
    getHelpLinkAsync: ReturnType<typeof vi.fn>;
    createHelpLink: ReturnType<typeof vi.fn>;
    createHelpLinkAsync: ReturnType<typeof vi.fn>;
    updateHelpLink: ReturnType<typeof vi.fn>;
    updateHelpLinkAsync: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    mockHelpService = {
      getHelpLinkAsync: vi.fn().mockResolvedValue({
        id: '1',
        title: { en: 'Test' },
        href: 'https://example.com',
      }),
      createHelpLink: vi.fn().mockReturnValue(of({})),
      createHelpLinkAsync: vi.fn().mockResolvedValue({}),
      updateHelpLink: vi.fn().mockReturnValue(of({})),
      updateHelpLinkAsync: vi.fn().mockResolvedValue({}),
    };

    await TestBed.configureTestingModule({
      imports: [AddLinkComponent, ReactiveFormsModule],
      providers: [
        { provide: HelpService, useValue: mockHelpService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AddLinkComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('linkId', undefined);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize form with empty values', () => {
    expect(component.newLinkForm.get('title')?.value).toBe('');
    expect(component.newLinkForm.get('href')?.value).toBe('');
  });

  it('should be invalid when form is empty', () => {
    expect(component.isValid()).toBe(false);
  });

  it('should be valid when title and valid href are provided', () => {
    component.newLinkForm.patchValue({
      title: 'Test',
      href: 'https://example.com',
    });
    expect(component.isValid()).toBe(true);
  });

  it('should be invalid with an invalid URL', () => {
    component.newLinkForm.patchValue({ title: 'Test', href: 'not-a-url' });
    expect(component.isValid()).toBe(false);
  });

  it('should expose hrefControl', () => {
    expect(component.hrefControl).toBe(component.newLinkForm.controls['href']);
  });

  describe('createLink', () => {
    it('should call helpService.createHelpLink and emit linkCreated', async () => {
      const emitSpy = vi.spyOn(component.linkCreated, 'emit');
      component.newLinkForm.patchValue({
        title: 'New',
        href: 'https://new.com',
      });

      await component.createLink();

      expect(mockHelpService.createHelpLinkAsync).toHaveBeenCalled();
      const callArg = mockHelpService.createHelpLinkAsync.mock.calls[0][0];
      expect(callArg.helpLink.href).toBe('https://new.com');
      expect(emitSpy).toHaveBeenCalledWith({ result: ActionResult.SUCCEED });
      expect(component.creating()).toBe(false);
    });

    it('should handle errors gracefully', async () => {
      mockHelpService.createHelpLinkAsync.mockRejectedValue(new Error('fail'));
      const emitSpy = vi.spyOn(component.linkCreated, 'emit');

      await component.createLink();

      expect(emitSpy).not.toHaveBeenCalled();
      expect(component.creating()).toBe(false);
    });
  });

  describe('updateLink', () => {
    it('should call helpService.updateHelpLink and emit linkCreated', async () => {
      fixture.componentRef.setInput('linkId', '123');
      component.newLinkForm.patchValue({
        title: 'Updated',
        href: 'https://updated.com',
      });
      const emitSpy = vi.spyOn(component.linkCreated, 'emit');

      await component.updateLink();

      expect(mockHelpService.updateHelpLinkAsync).toHaveBeenCalled();
      const callArg = mockHelpService.updateHelpLinkAsync.mock.calls[0][0];
      expect(callArg.id).toBe('123');
      expect(callArg.helpLink.id).toBe('123');
      expect(callArg.helpLink.href).toBe('https://updated.com');
      expect(emitSpy).toHaveBeenCalledWith({ result: ActionResult.SUCCEED });
      expect(component.editMode()).toBe(false);
      expect(component.creating()).toBe(false);
    });

    it('should not call service when linkId is undefined', async () => {
      fixture.componentRef.setInput('linkId', undefined);

      await component.updateLink();

      expect(mockHelpService.updateHelpLinkAsync).not.toHaveBeenCalled();
    });
  });

  describe('ngOnChanges', () => {
    it('should fetch link and enter edit mode when linkId changes', async () => {
      const changes = {
        linkId: new SimpleChange(undefined, 'abc', false),
      };

      await component.ngOnChanges(changes);

      expect(mockHelpService.getHelpLinkAsync).toHaveBeenCalledWith('abc');
      expect(component.editMode()).toBe(true);
      expect(component.linkToEdit).toEqual({
        id: '1',
        title: { en: 'Test' },
        href: 'https://example.com',
      });
    });

    it('should not fetch when linkId changes to falsy', async () => {
      const changes = {
        linkId: new SimpleChange('abc', undefined, false),
      };

      await component.ngOnChanges(changes);

      expect(mockHelpService.getHelpLinkAsync).not.toHaveBeenCalled();
    });
  });

  describe('cancel', () => {
    it('should reset state and emit events', () => {
      const showModalChangeSpy = vi.spyOn(component.showModalChange, 'emit');
      const linkIdChangeSpy = vi.spyOn(component.linkIdChange, 'emit');
      component.editMode.set(true);
      component.newLinkForm.patchValue({ title: 'x', href: 'https://x.com' });

      component.cancel();

      expect(component.showModal()).toBe(false);
      expect(component.editMode()).toBe(false);
      expect(component.linkToEdit).toBeUndefined();
      expect(component.linkId()).toBeUndefined();
      expect(linkIdChangeSpy).toHaveBeenCalled();
      expect(showModalChangeSpy).toHaveBeenCalledWith(false);
    });
  });
});
