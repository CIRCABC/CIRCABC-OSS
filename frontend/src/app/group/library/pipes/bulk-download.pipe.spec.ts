import { TestBed } from '@angular/core/testing';
import { BASE_PATH } from 'app/core/generated/circabc';
import { BulkDownloadPipe } from './bulk-download.pipe';

describe('BulkDownloadPipe', () => {
  let pipe: BulkDownloadPipe;
  const basePath = 'http://localhost:8080/api';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [BulkDownloadPipe, { provide: BASE_PATH, useValue: basePath }],
    });
    pipe = TestBed.inject(BulkDownloadPipe);
  });

  it('should build bulk download URL with multiple node IDs', () => {
    const result = pipe.transform(['id1', 'id2', 'id3']);
    expect(result).toBe(
      `${basePath}/content/bulk?nodeIds=id1&nodeIds=id2&nodeIds=id3`
    );
  });

  it('should build bulk download URL with a single node ID', () => {
    const result = pipe.transform(['id1']);
    expect(result).toBe(`${basePath}/content/bulk?nodeIds=id1`);
  });

  it('should skip undefined values in the array', () => {
    const result = pipe.transform(['id1', undefined, 'id2']);
    expect(result).toBe(`${basePath}/content/bulk?nodeIds=id1&nodeIds=id2`);
  });

  it('should return URL with empty query when array is empty', () => {
    const result = pipe.transform([]);
    expect(result).toBe(`${basePath}/content/bulk?`);
  });

  it('should throw when nodeIds is null/undefined', () => {
    expect(() =>
      pipe.transform(null as unknown as (string | undefined)[])
    ).toThrow('The list of node ids should be provided');
  });
});
