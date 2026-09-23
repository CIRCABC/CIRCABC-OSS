import { Location } from '@angular/common';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  Node as ModelNode,
  NodesService,
  TopicService,
  User,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';

import { ViewEditDetailsTopicComponent } from './view-edit-details-topic.component';

const mockNode: ModelNode = {
  id: 'topic-123',
  name: 'test-topic',
  title: { en: 'Test Topic' },
  description: { en: 'A test topic' },
  properties: {
    security_ranking: 'PUBLIC',
    expiration_date: '2026-12-31T00:00:00',
    creator: 'user1',
    modifier: 'user2',
    versionLabel: '1.0',
    created: '2026-01-01T00:00:00',
    modified: '2026-02-01T00:00:00',
    ismoderated: 'false',
    owner: 'user1',
  },
  permissions: {
    NwsAdmin: 'ALLOWED',
  },
};

const mockUser: User = {
  userId: 'user1',
  firstname: 'John',
  lastname: 'Doe',
  email: 'john@example.com',
};

describe('ViewEditDetailsTopicComponent', () => {
  const paramsSubject = new Subject<{ [key: string]: string }>();

  const mockNodesService = {
    getNodeAsync: vi.fn().mockResolvedValue(mockNode),
  };
  const mockUserService = { getUserAsync: vi.fn().mockResolvedValue(mockUser) };
  const mockTopicService = {
    putTopic: vi.fn().mockReturnValue(of({})),
    putTopicAsync: vi.fn().mockResolvedValue({}),
  };
  const mockLocation = { back: vi.fn() };
  const mockLoginService = {
    getCurrentUsername: vi.fn().mockReturnValue('user1'),
  };
  const mockPermEvalService = {
    isNewsgroupAdmin: vi.fn().mockReturnValue(true),
    isOwner: vi.fn().mockReturnValue(false),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ViewEditDetailsTopicComponent],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
        },
        { provide: NodesService, useValue: mockNodesService },
        { provide: UserService, useValue: mockUserService },
        { provide: TopicService, useValue: mockTopicService },
        { provide: Location, useValue: mockLocation },
        { provide: LoginService, useValue: mockLoginService },
        { provide: PermissionEvaluatorService, useValue: mockPermEvalService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  async function createComponent() {
    const fixture = TestBed.createComponent(ViewEditDetailsTopicComponent);
    const component = fixture.componentInstance;
    component.topicId = 'topic-123';
    fixture.detectChanges();
    await fixture.whenStable();
    return { fixture, component };
  }

  it('should create', async () => {
    const { component } = await createComponent();
    expect(component).toBeTruthy();
  });

  it('should initialize the form', async () => {
    const { component } = await createComponent();

    expect(component.updateTopicForm).toBeDefined();
    expect(component.updateTopicForm.controls['title']).toBeDefined();
    expect(component.updateTopicForm.controls['description']).toBeDefined();
    expect(component.updateTopicForm.controls['name']).toBeDefined();
    expect(component.updateTopicForm.controls['securityRanking']).toBeDefined();
    expect(component.updateTopicForm.controls['expirationDate']).toBeDefined();
  });

  it('should fill form with topic data', async () => {
    const { component } = await createComponent();

    expect(mockNodesService.getNodeAsync).toHaveBeenCalledWith({
      id: 'topic-123',
    });
    expect(component.updateTopicForm.controls['title'].value).toEqual(
      expect.objectContaining({ en: 'Test Topic' })
    );
    expect(component.updateTopicForm.controls['name'].value).toBe('test-topic');
    expect(component.updateTopicForm.controls['securityRanking'].value).toBe(
      'PUBLIC'
    );
    expect(component.updateTopicForm.controls['expirationDate'].value).toBe(
      '31/12/2026'
    );
  });

  it('should load creator and modifier users', async () => {
    const { component } = await createComponent();

    expect(mockUserService.getUserAsync).toHaveBeenCalledWith({
      userId: 'user1',
    });
    expect(mockUserService.getUserAsync).toHaveBeenCalledWith({
      userId: 'user2',
    });
    expect(component.creator()).toEqual(mockUser);
    expect(component.modifier()).toEqual(mockUser);
  });

  it('should set topicId from route params', async () => {
    const { component } = await createComponent();
    paramsSubject.next({ topicId: 'new-topic-456' });

    expect(component.topicId).toBe('new-topic-456');
  });

  it('should call location.back on goBack', async () => {
    const { component } = await createComponent();
    component.goBack();

    expect(mockLocation.back).toHaveBeenCalled();
  });

  it('should enable all form controls on enableEdit', async () => {
    const { component } = await createComponent();

    component.updateTopicForm.controls['title'].disable();
    component.enableEdit();

    expect(component.updateTopicForm.controls['title'].enabled).toBe(true);
    expect(component.viewing).toBe(false);
  });

  it('should clear expiration date', async () => {
    const { component } = await createComponent();
    component.clearExpirationDate();

    expect(component.updateTopicForm.controls['expirationDate'].value).toBe('');
  });

  it('should update topic and emit topicUpdated', async () => {
    const { component } = await createComponent();

    const emitSpy = vi.spyOn(component.topicUpdated, 'emit');

    component.updateTopicForm.controls['title'].setValue({ en: 'Updated' });
    component.updateTopicForm.controls['description'].setValue({
      en: 'Updated desc',
    });
    component.updateTopicForm.controls['name'].setValue('updated-name');
    component.updateTopicForm.controls['securityRanking'].setValue('INTERNAL');
    component.updateTopicForm.controls['expirationDate'].setValue('01/01/2027');

    await component.update();

    expect(mockTopicService.putTopicAsync).toHaveBeenCalledWith({
      id: 'topic-123',
      node: expect.objectContaining({
        name: 'updated-name',
      }),
    });
    expect(emitSpy).toHaveBeenCalled();
    expect(mockLocation.back).toHaveBeenCalled();
    expect(component.processing()).toBe(false);
  });

  it('should set processing to false even if update throws', async () => {
    mockNodesService.getNodeAsync.mockResolvedValueOnce({
      id: 'topic-123',
      name: 'test-topic',
      title: { en: 'Test Topic' },
      description: { en: 'A test topic' },
      properties: undefined,
    } as unknown as ModelNode);
    const { component } = await createComponent();

    await expect(component.update()).rejects.toThrow('"topic" is undefined.');
    expect(component.processing()).toBe(false);
  });

  it('should return true from isTopicAdmin when user is newsgroup admin', async () => {
    const { component } = await createComponent();

    expect(component.isTopicAdmin()).toBe(true);
    expect(mockPermEvalService.isNewsgroupAdmin).toHaveBeenCalledWith(mockNode);
  });

  it('should return true from isTopicAdmin when user is owner', async () => {
    mockPermEvalService.isNewsgroupAdmin.mockReturnValue(false);
    mockPermEvalService.isOwner.mockReturnValue(true);

    const { component } = await createComponent();

    expect(component.isTopicAdmin()).toBe(true);
    expect(mockPermEvalService.isOwner).toHaveBeenCalledWith(mockNode, 'user1');
  });

  it('should return versionLabel from properties', async () => {
    const { component } = await createComponent();

    expect(component.versionLabel).toBe('1.0');
  });

  it('should return created date substring', async () => {
    const { component } = await createComponent();

    expect(component.created).toBe('2026-01-01');
  });

  it('should return modified date substring', async () => {
    const { component } = await createComponent();

    expect(component.modified).toBe('2026-02-01');
  });

  it('should cancel and go back', async () => {
    const { component } = await createComponent();
    component.cancel();

    expect(mockNodesService.getNodeAsync).toHaveBeenCalled();
    expect(mockLocation.back).toHaveBeenCalled();
  });
});
