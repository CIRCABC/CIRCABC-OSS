import { TestBed } from '@angular/core/testing';
import { HeaderReloadListenerService } from 'app/core/header-reload-listener.service';

describe('HeaderReloadListenerService', () => {
  let service: HeaderReloadListenerService;

  beforeEach(() => {
    service = TestBed.inject(HeaderReloadListenerService);
  });

  it('should be created', () => {
    expect(service).toBeDefined();
  });

  it('should emit on refreshAnnounced$ when propagateHeaderRefresh is called', () => {
    let emitted = false;
    service.refreshAnnounced$.subscribe(() => {
      emitted = true;
    });

    service.propagateHeaderRefresh();

    expect(emitted).toBe(true);
  });

  it('should emit multiple times', () => {
    let count = 0;
    service.refreshAnnounced$.subscribe(() => {
      count++;
    });

    service.propagateHeaderRefresh();
    service.propagateHeaderRefresh();

    expect(count).toBe(2);
  });
});
