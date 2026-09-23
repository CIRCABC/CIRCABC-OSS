import { TestBed } from '@angular/core/testing';
import { environment } from 'environments/environment';
import { vi } from 'vitest';
import { SummarizeResponse, SummarizeService } from './summarize.service';

describe('SummarizeService', () => {
  let service: SummarizeService;
  const originalAiAgentUrl = environment.aiAgentUrl;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SummarizeService);
  });

  afterEach(() => {
    vi.restoreAllMocks();
    sessionStorage.clear();
    environment.aiAgentUrl = originalAiAgentUrl;
  });

  describe('isEnabled', () => {
    it('should return true when aiAgentUrl is set', () => {
      environment.aiAgentUrl = 'http://localhost:8081';
      expect(service.isEnabled).toBe(true);
    });

    it('should return false when aiAgentUrl is empty', () => {
      environment.aiAgentUrl = '';
      expect(service.isEnabled).toBe(false);
    });
  });

  describe('summarize', () => {
    const mockResponse: SummarizeResponse = {
      nodeId: 'node-123',
      summary: 'This is a summary.',
    };

    beforeEach(() => {
      environment.aiAgentUrl = 'http://localhost:8081';
    });

    it('should call fetch with correct URL and headers', async () => {
      sessionStorage.setItem('ticket', 'test-ticket');
      const fetchSpy = vi
        .spyOn(globalThis, 'fetch')
        .mockResolvedValue(
          new Response(JSON.stringify(mockResponse), { status: 200 })
        );

      await service.summarize('node-123');

      expect(fetchSpy).toHaveBeenCalledWith(
        'http://localhost:8081/summarize/node-123',
        expect.objectContaining({
          method: 'POST',
          headers: { 'X-Circabc-Ticket': 'test-ticket' },
        })
      );
    });

    it('should use empty ticket when sessionStorage has no ticket', async () => {
      const fetchSpy = vi
        .spyOn(globalThis, 'fetch')
        .mockResolvedValue(
          new Response(JSON.stringify(mockResponse), { status: 200 })
        );

      await service.summarize('node-123');

      expect(fetchSpy).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: { 'X-Circabc-Ticket': '' },
        })
      );
    });

    it('should return the parsed response on success', async () => {
      vi.spyOn(globalThis, 'fetch').mockResolvedValue(
        new Response(JSON.stringify(mockResponse), { status: 200 })
      );

      const result = await service.summarize('node-123');

      expect(result).toEqual(mockResponse);
    });

    it('should throw when response is not ok', async () => {
      vi.spyOn(globalThis, 'fetch').mockResolvedValue(
        new Response('', { status: 500 })
      );

      await expect(service.summarize('node-123')).rejects.toThrow(
        'Summarize failed: 500'
      );
    });
  });
});
