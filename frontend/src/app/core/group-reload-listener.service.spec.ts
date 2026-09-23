import { TestBed } from '@angular/core/testing';
import { GroupReloadListenerService } from 'app/core/group-reload-listener.service';
import { vi } from 'vitest';

describe('GroupReloadListenerService', () => {
  let service: GroupReloadListenerService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [GroupReloadListenerService] });
    service = TestBed.inject(GroupReloadListenerService);
  });

  it('should be created', () => {
    expect(service).toBeDefined();
  });

  it('should emit groupId on refreshAnnounced$ when propagateGroupRefresh is called', () => {
    const callback = vi.fn();
    service.refreshAnnounced$.subscribe((id: string) => callback(id));

    service.propagateGroupRefresh('group-123');

    expect(callback).toHaveBeenCalledWith('group-123');
  });

  it('should emit multiple values in order', () => {
    const results: string[] = [];
    service.refreshAnnounced$.subscribe((id: string) => results.push(id));

    service.propagateGroupRefresh('a');
    service.propagateGroupRefresh('b');

    expect(results).toEqual(['a', 'b']);
  });

  it('should not emit to subscribers that subscribed after the event', () => {
    const callback = vi.fn();

    service.propagateGroupRefresh('missed');
    service.refreshAnnounced$.subscribe((id: string) => callback(id));

    expect(callback).not.toHaveBeenCalled();
  });
});
