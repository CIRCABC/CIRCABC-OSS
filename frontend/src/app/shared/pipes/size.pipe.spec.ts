import { SizePipe } from './size.pipe';

describe('SizePipe', () => {
  const pipe = new SizePipe();

  it('should return empty string for null', () => {
    expect(pipe.transform(null, true)).toBe('');
  });

  it('should return empty string for 0', () => {
    expect(pipe.transform(0, true)).toBe('');
  });

  it('should return bytes for values below threshold (SI)', () => {
    expect(pipe.transform(500, true)).toBe('500 B');
  });

  it('should return bytes for values below threshold (binary)', () => {
    expect(pipe.transform(1000, false)).toBe('1000 B');
  });

  it('should convert to KB (SI)', () => {
    expect(pipe.transform(1500, true)).toBe('1.50 KB');
  });

  it('should convert to KiB (binary)', () => {
    expect(pipe.transform(1536, false)).toBe('1.50 KiB');
  });

  it('should convert to MB (SI)', () => {
    expect(pipe.transform(1500000, true)).toBe('1.50 MB');
  });

  it('should convert to MiB (binary)', () => {
    expect(pipe.transform(1572864, false)).toBe('1.50 MiB');
  });

  it('should handle string input', () => {
    expect(pipe.transform('2048', false)).toBe('2 KiB');
  });

  it('should drop trailing zeros when value is exact', () => {
    expect(pipe.transform(1000, true)).toBe('1 KB');
  });
});
