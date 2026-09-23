import { Location } from '@angular/common';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  ForumService,
  Node as ModelNode,
  NodesService,
  User,
  UserService,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ViewEditDetailsForumComponent } from './view-edit-details-forum.component';

const mockNode: ModelNode = {
  id: 'forum-123',
  title: { en: 'Test Forum' },
  description: { en: 'A test forum' },
  name: 'test-forum',
  properties: {
    creator: 'user1',
    modifier: 'user2',
    versionLabel: '1.0',
    created: '2024-01-15T10:00:00Z',
    modified: '2024-02-20T12:00:00Z',
    ismoderated: 'true',
  },
};

const mockUser: User = { userId: 'user1', firstname: 'John', lastname: 'Doe' };

describe('ViewEditDetailsForumComponent', () => {
  const mockNodesService = {
    getNodeAsync: vi.fn().mockResolvedValue(mockNode),
  };
  const mockUserService = { getUserAsync: vi.fn().mockResolvedValue(mockUser) };
  const mockForumService = {
    putForum: vi.fn().mockReturnValue(of({})),
    putForumAsync: vi.fn().mockResolvedValue({}),
  };
  const mockLocation = { back: vi.fn() };
  const mockUiMessageService = { addErrorMessage: vi.fn() };
  const mockPermEvalService = {
    isNewsgroupAdmin: vi.fn((node: ModelNode | undefined) => !!node),
  };
  const mockRoute = { params: of({ forumId: 'forum-123' }) };

  beforeEach(async () => {
    vi.clearAllMocks();
    mockNodesService.getNodeAsync.mockResolvedValue(mockNode);
    mockUserService.getUserAsync.mockResolvedValue(mockUser);
    mockForumService.putForum.mockReturnValue(of({}));
    mockForumService.putForumAsync.mockResolvedValue({});

    await TestBed.configureTestingModule({
      imports: [ViewEditDetailsForumComponent],
      providers: [
        { provide: NodesService, useValue: mockNodesService },
        { provide: UserService, useValue: mockUserService },
        { provide: ForumService, useValue: mockForumService },
        { provide: Location, useValue: mockLocation },
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: PermissionEvaluatorService, useValue: mockPermEvalService },
        { provide: ActivatedRoute, useValue: mockRoute },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    })
      .overrideComponent(ViewEditDetailsForumComponent, {
        set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
      })
      .compileComponents();
  });

  it('should create', async () => {
    const fixture = TestBed.createComponent(ViewEditDetailsForumComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.componentInstance).toBeDefined();
  });

  it('should load forum data on init', async () => {
    const fixture = TestBed.createComponent(ViewEditDetailsForumComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    const component = fixture.componentInstance;

    expect(component.forumId()).toBe('forum-123');
    expect(component.forum()).toEqual(mockNode);
    expect(mockNodesService.getNodeAsync).toHaveBeenCalledWith({
      id: 'forum-123',
    });
  });

  it('should load creator user', async () => {
    const fixture = TestBed.createComponent(ViewEditDetailsForumComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    const component = fixture.componentInstance;

    expect(component.creator()).toEqual(mockUser);
    expect(mockUserService.getUserAsync).toHaveBeenCalledWith({
      userId: 'user1',
    });
  });

  it('should cut date string to first 10 characters', async () => {
    const fixture = TestBed.createComponent(ViewEditDetailsForumComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    expect(component.cutDate('2024-01-15T10:00:00Z')).toBe('2024-01-15');
    expect(component.cutDate(undefined as unknown as string)).toBe('');
  });

  it('should call location.back on goBack', async () => {
    const fixture = TestBed.createComponent(ViewEditDetailsForumComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    component.goBack();
    expect(mockLocation.back).toHaveBeenCalled();
  });

  it('should enable form controls on enableEdit', async () => {
    const fixture = TestBed.createComponent(ViewEditDetailsForumComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    const component = fixture.componentInstance;

    component.updateForumForm.controls['title'].disable();
    component.enableEdit();

    expect(component.updateForumForm.controls['title'].enabled).toBe(true);
    expect(component.viewing).toBe(false);
  });

  it('should update forum and go back', async () => {
    const fixture = TestBed.createComponent(ViewEditDetailsForumComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    const component = fixture.componentInstance;

    component.updateForumForm.controls['title'].setValue({ en: 'Updated' });
    component.updateForumForm.controls['description'].setValue({
      en: 'Updated desc',
    });
    component.updateForumForm.controls['name'].setValue('updated-name');

    await component.update();

    expect(mockForumService.putForumAsync).toHaveBeenCalledWith({
      id: 'forum-123',
      node: expect.objectContaining({
        title: { en: 'Updated' },
        description: { en: 'Updated desc' },
        name: 'updated-name',
      }),
    });
    expect(mockLocation.back).toHaveBeenCalled();
    expect(component.processing).toBe(false);
  });

  it('should delegate isForumAdmin to permEvalService', async () => {
    const fixture = TestBed.createComponent(ViewEditDetailsForumComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    const component = fixture.componentInstance;

    expect(component.isForumAdmin()).toBe(true);
    expect(mockPermEvalService.isNewsgroupAdmin).toHaveBeenCalledWith(mockNode);
  });

  it('should return properties from getters', async () => {
    const fixture = TestBed.createComponent(ViewEditDetailsForumComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    const component = fixture.componentInstance;

    expect(component.versionLabel).toBe('1.0');
    expect(component.created).toBe('2024-01-15');
    expect(component.modified).toBe('2024-02-20');
    expect(component.ismoderated).toBe('true');
  });

  it('should return empty strings from getters when no properties', async () => {
    mockNodesService.getNodeAsync.mockResolvedValue({ id: 'x' } as ModelNode);
    const fixture = TestBed.createComponent(ViewEditDetailsForumComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    const component = fixture.componentInstance;

    expect(component.versionLabel).toBe('');
    expect(component.created).toBe('');
    expect(component.modified).toBe('');
    expect(component.ismoderated).toBe('false');
  });
});
