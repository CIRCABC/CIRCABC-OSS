import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  CategoryService,
  Node as ModelNode,
  NodesService,
  Profile,
  ProfileService,
} from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ImportProfileComponent } from './import-profile.component';

const mockNode: ModelNode = { id: 'ig-1', parentId: 'cat-1' };
const mockProfiles: Profile[] = [
  { id: 'p1', name: 'Profile 1' },
  { id: 'p2', name: 'Profile 2' },
];

const mockNodesService = {
  getNodeAsync: vi.fn().mockResolvedValue(mockNode),
};

const mockCategoryService = {
  getExportedProfilesAsync: vi.fn().mockResolvedValue(mockProfiles),
};

const mockProfileService = {
  postImportedProfile: vi.fn().mockReturnValue(of({})),
  postImportedProfileAsync: vi.fn().mockResolvedValue({}),
};

describe('ImportProfileComponent', () => {
  let component: ImportProfileComponent;
  let componentRef: ComponentRef<ImportProfileComponent>;
  let fixture: ComponentFixture<ImportProfileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ImportProfileComponent],
      providers: [
        { provide: NodesService, useValue: mockNodesService },
        { provide: CategoryService, useValue: mockCategoryService },
        { provide: ProfileService, useValue: mockProfileService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ImportProfileComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('igNodeId', 'ig-1');
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load node and exported profiles on init', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockNodesService.getNodeAsync).toHaveBeenCalledWith({ id: 'ig-1' });
    expect(mockCategoryService.getExportedProfilesAsync).toHaveBeenCalledWith({
      id: 'cat-1',
      ignoreIgId: 'ig-1',
    });
    expect(component.ig()).toEqual(mockNode);
    expect(component.exportedProfiles()).toEqual(mockProfiles);
  });

  it('should not fetch exported profiles if node has no parentId', async () => {
    mockNodesService.getNodeAsync.mockResolvedValueOnce({ id: 'ig-1' });
    mockCategoryService.getExportedProfilesAsync.mockClear();

    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockCategoryService.getExportedProfilesAsync).not.toHaveBeenCalled();
  });

  it('should emit CANCELED result and reset form on cancelWizard', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    const emitSpy = vi.spyOn(component.profileImported, 'emit');

    component.cancelWizard();

    expect(component.showModal()).toBe(false);
    expect(emitSpy).toHaveBeenCalledWith(
      expect.objectContaining({ result: 0, type: 'profile.import' })
    );
  });

  it('should import selected profile and emit SUCCEED', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    component.importProfileForm.patchValue({ selectedProfile: 'p1' });
    const emitSpy = vi.spyOn(component.profileImported, 'emit');

    await component.import();

    expect(mockProfileService.postImportedProfileAsync).toHaveBeenCalledWith({
      id: 'ig-1',
      profile: mockProfiles[0],
    });
    expect(emitSpy).toHaveBeenCalledWith(
      expect.objectContaining({ result: 1, type: 'profile.import' })
    );
    expect(component.importing()).toBe(false);
  });

  it('should emit FAILED result when import throws', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    component.importProfileForm.patchValue({ selectedProfile: 'p1' });
    mockProfileService.postImportedProfileAsync.mockRejectedValueOnce(
      new Error('fail')
    );
    const emitSpy = vi.spyOn(component.profileImported, 'emit');

    await component.import();

    expect(emitSpy).toHaveBeenCalledWith(
      expect.objectContaining({ result: -1, type: 'profile.import' })
    );
    expect(component.importing()).toBe(false);
  });
});
