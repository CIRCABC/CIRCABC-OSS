import { HttpRequest, HttpResponse } from '@angular/common/http';
import { RequestCacheWithMap } from 'app/core/interceptors/request-cache.service';
import { vi } from 'vitest';

describe('RequestCacheWithMap', () => {
  let service: RequestCacheWithMap;

  beforeEach(() => {
    service = new RequestCacheWithMap();
  });

  it('should return undefined for uncached request', () => {
    const req = new HttpRequest('GET', '/api/test');
    expect(service.get(req)).toBeUndefined();
  });

  it('should return cached response after put', () => {
    const req = new HttpRequest('GET', '/api/test');
    const response = new HttpResponse({ body: { data: 1 } });

    service.put(req, response);

    expect(service.get(req)).toBe(response);
  });

  it('should return undefined for expired entries', () => {
    const req = new HttpRequest('GET', '/api/expired');
    const response = new HttpResponse({ body: 'old' });

    vi.spyOn(Date, 'now').mockReturnValue(1000);
    service.put(req, response);

    vi.spyOn(Date, 'now').mockReturnValue(1000 + 300001);

    expect(service.get(req)).toBeUndefined();
  });

  it('should remove expired entries when putting new ones', () => {
    const req1 = new HttpRequest('GET', '/api/old');
    const req2 = new HttpRequest('GET', '/api/new');
    const res1 = new HttpResponse({ body: 'old' });
    const res2 = new HttpResponse({ body: 'new' });

    vi.spyOn(Date, 'now').mockReturnValue(1000);
    service.put(req1, res1);

    vi.spyOn(Date, 'now').mockReturnValue(1000 + 300001);
    service.put(req2, res2);

    expect(service.cache.has('/api/old')).toBe(false);
    expect(service.cache.has('/api/new')).toBe(true);
  });

  it('should cache by urlWithParams', () => {
    const req1 = new HttpRequest('GET', '/api/test?a=1');
    const req2 = new HttpRequest('GET', '/api/test?a=2');
    const res1 = new HttpResponse({ body: 'one' });
    const res2 = new HttpResponse({ body: 'two' });

    service.put(req1, res1);
    service.put(req2, res2);

    expect(service.get(req1)).toBe(res1);
    expect(service.get(req2)).toBe(res2);
  });
});
