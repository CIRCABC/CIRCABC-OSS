import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AnalyticsService } from 'app/core//analytics.service';
import { LoginService } from 'app/core/login.service';
import { SaveAsService } from 'app/core/save-as.service';
import { SERVER_URL } from 'app/core/variables';
import { vi } from 'vitest';

describe('SaveAsService', () => {
  let service: SaveAsService;
  let httpMock: HttpTestingController;

  const mockLoginService = {
    getTicket: vi.fn().mockReturnValue('test-ticket'),
  };

  const mockAnalyticsService = {
    trackDownload: vi.fn(),
  };

  const serverUrl = 'http://localhost:8080/';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        SaveAsService,
        { provide: LoginService, useValue: mockLoginService },
        { provide: AnalyticsService, useValue: mockAnalyticsService },
        { provide: SERVER_URL, useValue: serverUrl },
      ],
    });

    service = TestBed.inject(SaveAsService);
    httpMock = TestBed.inject(HttpTestingController);
    vi.clearAllMocks();
    mockLoginService.getTicket.mockReturnValue('test-ticket');
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeDefined();
  });

  describe('saveAsDirect', () => {
    it('should track download and create a download link', () => {
      const fakeLink = document.createElement('a');
      const clickSpy = vi.spyOn(fakeLink, 'click');
      const removeSpy = vi.spyOn(fakeLink, 'remove');
      vi.spyOn(document, 'createElement').mockReturnValue(fakeLink);

      service.saveAsDirect('123', 'file.pdf');

      expect(mockAnalyticsService.trackDownload).toHaveBeenCalledWith(
        `${serverUrl}rest/download/123?ticket=test-ticket`,
        'file.pdf'
      );
      expect(fakeLink.download).toBe('file.pdf');
      expect(clickSpy).toHaveBeenCalled();
      expect(removeSpy).toHaveBeenCalled();
    });

    it('should not create link if name is empty', () => {
      const appendSpy = vi.spyOn(document.body, 'appendChild');

      service.saveAsDirect('123', '');

      expect(appendSpy).not.toHaveBeenCalled();
    });
  });

  describe('saveUrlAsync', () => {
    it('should fetch blob and call saveAs', async () => {
      const blob = new Blob(['content']);
      const url = 'http://example.com/file';

      const promise = service.saveUrlAsync(url, 'doc.pdf');

      const req = httpMock.expectOne(url);
      expect(req.request.headers.get('Authorization')).toBe(
        `Basic ${btoa('test-ticket')}`
      );
      req.flush(blob);

      await promise;

      expect(mockAnalyticsService.trackDownload).toHaveBeenCalledWith(
        url,
        'doc.pdf'
      );
    });
  });

  describe('saveAs', () => {
    it('should open XHR with correct URL and track download', () => {
      const openSpy = vi.fn();
      const setRequestHeaderSpy = vi.fn();
      const sendSpy = vi.fn();

      class FakeXHR {
        open = openSpy;
        setRequestHeader = setRequestHeaderSpy;
        send = sendSpy;
        responseType = '';
        onload: ((e: unknown) => void) | null = null;
      }

      vi.stubGlobal('XMLHttpRequest', FakeXHR);

      service.saveAs('456', 'report.xlsx');

      expect(openSpy).toHaveBeenCalledWith(
        'GET',
        `${serverUrl}rest/download/456`,
        true
      );
      expect(setRequestHeaderSpy).toHaveBeenCalledWith(
        'Authorization',
        `Basic ${btoa('test-ticket')}`
      );
      expect(sendSpy).toHaveBeenCalled();
      expect(mockAnalyticsService.trackDownload).toHaveBeenCalledWith(
        `${serverUrl}rest/download/456`,
        'report.xlsx'
      );

      vi.unstubAllGlobals();
    });
  });

  describe('saveUrlAs', () => {
    it('should open XHR with provided URL and track download', () => {
      const openSpy = vi.fn();
      const setRequestHeaderSpy = vi.fn();
      const sendSpy = vi.fn();

      class FakeXHR {
        open = openSpy;
        setRequestHeader = setRequestHeaderSpy;
        send = sendSpy;
        responseType = '';
        onload: ((e: unknown) => void) | null = null;
      }

      vi.stubGlobal('XMLHttpRequest', FakeXHR);

      service.saveUrlAs('http://custom.url/file', 'data.csv');

      expect(openSpy).toHaveBeenCalledWith(
        'GET',
        'http://custom.url/file',
        true
      );
      expect(setRequestHeaderSpy).toHaveBeenCalledWith(
        'Authorization',
        `Basic ${btoa('test-ticket')}`
      );
      expect(sendSpy).toHaveBeenCalled();
      expect(mockAnalyticsService.trackDownload).toHaveBeenCalledWith(
        'http://custom.url/file',
        'data.csv'
      );

      vi.unstubAllGlobals();
    });
  });
});
