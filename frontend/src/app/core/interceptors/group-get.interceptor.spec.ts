import {
  HttpClient,
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { groupGetInterceptor } from 'app/core/interceptors/group-get.interceptor';
import { LoginService } from 'app/core/login.service';
import { VisitedGroupService } from 'app/core/visited-groups/visited-group.service';
import { vi } from 'vitest';

describe('GroupGetInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let mockLoginService: { isGuest: ReturnType<typeof vi.fn> };
  let mockVisitedGroupService: { isVisited: ReturnType<typeof vi.fn> };

  const groupUrl =
    '/service/circabc/groups/12345678-1234-1234-1234-123456789abc';

  beforeEach(() => {
    mockLoginService = { isGuest: vi.fn() };
    mockVisitedGroupService = { isVisited: vi.fn() };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([groupGetInterceptor])),
        provideHttpClientTesting(),
        { provide: LoginService, useValue: mockLoginService },
        { provide: VisitedGroupService, useValue: mockVisitedGroupService },
      ],
    });

    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should pass through non-matching requests unchanged', () => {
    mockLoginService.isGuest.mockReturnValue(false);
    mockVisitedGroupService.isVisited.mockReturnValue(false);

    httpClient.post(groupUrl, {}).subscribe();

    const req = httpMock.expectOne(groupUrl);
    expect(req.request.params.has('log')).toBe(false);
    req.flush({});
  });

  it('should pass through GET requests that do not match the group URL pattern', () => {
    httpClient
      .get('/service/circabc/other/12345678-1234-1234-1234-123456789abc')
      .subscribe();

    const req = httpMock.expectOne(
      '/service/circabc/other/12345678-1234-1234-1234-123456789abc'
    );
    expect(req.request.params.has('log')).toBe(false);
    req.flush({});
  });

  it('should add log=false param when user is guest', () => {
    mockLoginService.isGuest.mockReturnValue(true);
    mockVisitedGroupService.isVisited.mockReturnValue(false);

    httpClient.get(groupUrl).subscribe();

    const req = httpMock.expectOne((r) => r.url === groupUrl);
    expect(req.request.params.get('log')).toBe('false');
    req.flush({});
  });

  it('should add log=false param when group is already visited', () => {
    mockLoginService.isGuest.mockReturnValue(false);
    mockVisitedGroupService.isVisited.mockReturnValue(true);

    httpClient.get(groupUrl).subscribe();

    const req = httpMock.expectOne((r) => r.url === groupUrl);
    expect(req.request.params.get('log')).toBe('false');
    req.flush({});
  });

  it('should not add log param when user is not guest and group is not visited', () => {
    mockLoginService.isGuest.mockReturnValue(false);
    mockVisitedGroupService.isVisited.mockReturnValue(false);

    httpClient.get(groupUrl).subscribe();

    const req = httpMock.expectOne(groupUrl);
    expect(req.request.params.has('log')).toBe(false);
    req.flush({});
  });

  it('should extract the correct group id from the URL', () => {
    const groupId = 'abcdef01-2345-6789-abcd-ef0123456789';
    mockLoginService.isGuest.mockReturnValue(false);
    mockVisitedGroupService.isVisited.mockReturnValue(false);

    httpClient.get(`/service/circabc/groups/${groupId}`).subscribe();

    const req = httpMock.expectOne(`/service/circabc/groups/${groupId}`);
    expect(mockVisitedGroupService.isVisited).toHaveBeenCalledWith(groupId);
    req.flush({});
  });
});
