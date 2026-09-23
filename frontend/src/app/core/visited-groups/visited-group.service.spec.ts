import { TestBed } from '@angular/core/testing';
import { InterestGroup } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { VisitedGroupService } from 'app/core/visited-groups/visited-group.service';
import { vi } from 'vitest';

describe('VisitedGroupService', () => {
  let service: VisitedGroupService;
  const mockLoginService = { isGuest: vi.fn() };

  const ig: InterestGroup = {
    id: 'group-123',
    name: 'Test Group',
    permissions: {},
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [{ provide: LoginService, useValue: mockLoginService }],
    });
    service = TestBed.inject(VisitedGroupService);
    mockLoginService.isGuest.mockReset();
  });

  it('should be created', () => {
    expect(service).toBeDefined();
  });

  it('should mark a group as visited when user is not guest', () => {
    mockLoginService.isGuest.mockReturnValue(false);
    service.visitGroup(ig);
    expect(service.isVisited('group-123')).toBe(true);
  });

  it('should not mark a group as visited when user is guest', () => {
    mockLoginService.isGuest.mockReturnValue(true);
    service.visitGroup(ig);
    expect(service.isVisited('group-123')).toBe(false);
  });

  it('should return false for a group that has not been visited', () => {
    expect(service.isVisited('unknown-id')).toBe(false);
  });

  it('should not duplicate entries when visiting the same group twice', () => {
    mockLoginService.isGuest.mockReturnValue(false);
    service.visitGroup(ig);
    service.visitGroup(ig);
    expect(service.isVisited('group-123')).toBe(true);
  });
});
