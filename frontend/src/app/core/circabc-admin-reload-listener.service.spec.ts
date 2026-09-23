import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { CiracbcAdminReloadListenerService } from './circabc-admin-reload-listener.service';

describe('CiracbcAdminReloadListenerService', () => {
  let service: CiracbcAdminReloadListenerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CiracbcAdminReloadListenerService);
  });

  it('should be created', () => {
    expect(service).toBeDefined();
  });

  it('should emit when propagateCircabcAdminRefresh is called', () => {
    const callback = vi.fn();
    service.refreshAnnounced$.subscribe(callback);

    service.propagateCircabcAdminRefresh();

    expect(callback).toHaveBeenCalledTimes(1);
  });

  it('should emit multiple times on multiple calls', () => {
    const callback = vi.fn();
    service.refreshAnnounced$.subscribe(callback);

    service.propagateCircabcAdminRefresh();
    service.propagateCircabcAdminRefresh();

    expect(callback).toHaveBeenCalledTimes(2);
  });

  it('should not emit to subscribers that unsubscribed', () => {
    const callback = vi.fn();
    const sub = service.refreshAnnounced$.subscribe(callback);
    sub.unsubscribe();

    service.propagateCircabcAdminRefresh();

    expect(callback).not.toHaveBeenCalled();
  });
});
