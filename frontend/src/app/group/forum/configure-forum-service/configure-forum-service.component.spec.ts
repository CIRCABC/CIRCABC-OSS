import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import {
  GroupConfiguration,
  InterestGroupService,
} from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ConfigureForumServiceComponent } from './configure-forum-service.component';

const mockConfiguration: GroupConfiguration = {
  newsgroups: {
    enableFlagNewTopic: true,
    enableFlagNewForum: false,
    ageFlagNewTopic: 15,
    ageFlagNewForum: 7,
  },
};

const mockGroupsService = {
  getGroupConfigurationAsync: vi.fn().mockResolvedValue(mockConfiguration),
  putGroupConfiguration: vi.fn().mockReturnValue(of(mockConfiguration)),
  putGroupConfigurationAsync: vi.fn().mockResolvedValue(mockConfiguration),
};

describe('ConfigureForumServiceComponent', () => {
  let component: ConfigureForumServiceComponent;
  let componentRef: ComponentRef<ConfigureForumServiceComponent>;
  let fixture: ComponentFixture<ConfigureForumServiceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConfigureForumServiceComponent],
      providers: [
        { provide: InterestGroupService, useValue: mockGroupsService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ConfigureForumServiceComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('groupId', '123');
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeDefined();
  });

  it('should load configuration on init', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockGroupsService.getGroupConfigurationAsync).toHaveBeenCalledWith({
      id: '123',
    });
    expect(component.configuration()).toEqual(mockConfiguration);
  });

  it('should patch form with loaded configuration', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.configurationForm.getRawValue()).toEqual({
      enableFlagNewTopic: true,
      enableFlagNewForum: false,
      ageFlagNewTopic: 15,
      ageFlagNewForum: 7,
    });
  });

  it('should enable ageFlagNewTopic when enableFlagNewTopic is true', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    component.configurationForm.controls['enableFlagNewTopic'].setValue(true);
    expect(
      component.configurationForm.controls['ageFlagNewTopic'].enabled
    ).toBe(true);
  });

  it('should disable ageFlagNewTopic when enableFlagNewTopic is false', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    component.configurationForm.controls['enableFlagNewTopic'].setValue(false);
    expect(
      component.configurationForm.controls['ageFlagNewTopic'].enabled
    ).toBe(false);
  });

  it('should enable ageFlagNewForum when enableFlagNewForum is true', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    component.configurationForm.controls['enableFlagNewForum'].setValue(true);
    expect(
      component.configurationForm.controls['ageFlagNewForum'].enabled
    ).toBe(true);
  });

  it('should disable ageFlagNewForum when enableFlagNewForum is false', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    component.configurationForm.controls['enableFlagNewForum'].setValue(false);
    expect(
      component.configurationForm.controls['ageFlagNewForum'].enabled
    ).toBe(false);
  });

  it('should save configuration and emit success', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    const emitSpy = vi.spyOn(component.modalHide, 'emit');

    component.configurationForm.patchValue({
      enableFlagNewTopic: true,
      ageFlagNewTopic: '30',
      enableFlagNewForum: false,
      ageFlagNewForum: '7',
    });

    await component.saveConfiguration();

    expect(mockGroupsService.putGroupConfigurationAsync).toHaveBeenCalledWith({
      id: '123',
      groupConfiguration: {
        newsgroups: {
          enableFlagNewTopic: true,
          enableFlagNewForum: false,
          ageFlagNewTopic: 30,
          ageFlagNewForum: Number.NaN,
        },
      },
    });
    expect(emitSpy).toHaveBeenCalledWith(
      expect.objectContaining({
        type: ActionType.UPDATE_GROUP_CONFIGURATION,
        result: ActionResult.SUCCEED,
      })
    );
    expect(component.processing()).toBe(false);
  });

  it('should emit failed result on save error', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    mockGroupsService.putGroupConfigurationAsync.mockRejectedValueOnce(
      new Error('fail')
    );
    const emitSpy = vi.spyOn(component.modalHide, 'emit');

    await component.saveConfiguration();

    expect(emitSpy).toHaveBeenCalledWith(
      expect.objectContaining({
        type: ActionType.UPDATE_GROUP_CONFIGURATION,
        result: ActionResult.FAILED,
      })
    );
    expect(component.processing()).toBe(false);
  });

  it('should set showModal to false and emit on cancel', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    component.showModal.set(true);
    const emitSpy = vi.spyOn(component.showModalChange, 'emit');

    component.cancel();

    expect(component.showModal()).toBe(false);
    expect(emitSpy).toHaveBeenCalled();
  });
});
