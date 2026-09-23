import { CapitalizePipe } from './capitalize.pipe';

describe('CapitalizePipe', () => {
  const pipe = new CapitalizePipe();

  it('should capitalize a single word', () => {
    expect(pipe.transform('hello')).toBe('Hello');
  });

  it('should capitalize multiple words', () => {
    expect(pipe.transform('hello world')).toBe('World');
  });

  it('should return empty string for empty input', () => {
    expect(pipe.transform('')).toBe('');
  });

  it('should handle already capitalized input', () => {
    expect(pipe.transform('Hello')).toBe('Hello');
  });
});
