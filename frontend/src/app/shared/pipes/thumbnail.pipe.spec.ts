import { TestBed } from '@angular/core/testing';
import { ALF_BASE_PATH } from 'app/core/variables';
import { ThumbnailPipe } from 'app/shared/pipes/thumbnail.pipe';
import { environment } from 'environments/environment';
import { vi } from 'vitest';

describe('ThumbnailPipe', () => {
  const alfBasePath = 'http://localhost/alfresco';
  let pipe: ThumbnailPipe;

  beforeEach(() => {
    environment.useAlfrescoAPI = false;
    TestBed.configureTestingModule({
      providers: [
        ThumbnailPipe,
        { provide: ALF_BASE_PATH, useValue: alfBasePath },
      ],
    });
    pipe = TestBed.inject(ThumbnailPipe);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should return thumbnail URL with SpacesStore by default', () => {
    const result = pipe.transform('abc-123');
    expect(result).toBe(
      `${alfBasePath}/node/workspace/SpacesStore/abc-123/content/thumbnails/doclib?c=queue&ph=true`
    );
  });

  it('should return thumbnail URL with version2Store when isVersion is true', () => {
    const result = pipe.transform('abc-123', true);
    expect(result).toBe(
      `${alfBasePath}/node/workspace/version2Store/abc-123/content/thumbnails/doclib?c=queue&ph=true`
    );
  });

  it('should return Alfresco API URL when useAlfrescoAPI is true', () => {
    environment.useAlfrescoAPI = true;
    const result = pipe.transform('abc-123');
    expect(result).toBe(
      `${environment.serverURL}api/-default-/public/alfresco/versions/1/nodes/abc-123/renditions/doclib/content`
    );
  });

  it('should throw an error when id is empty', () => {
    expect(() => pipe.transform('')).toThrow('id should be provided');
  });
});
