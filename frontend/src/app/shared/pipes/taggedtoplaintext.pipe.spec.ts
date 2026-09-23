import { TaggedToPlainTextPipe } from './taggedtoplaintext.pipe';

describe('TaggedToPlainTextPipe', () => {
  const pipe = new TaggedToPlainTextPipe();

  it('should remove HTML tags from a string', () => {
    expect(pipe.transform('<p>Hello <b>World</b></p>')).toBe('Hello World');
  });

  it('should return empty string for undefined', () => {
    expect(pipe.transform(undefined)).toBe('');
  });

  it('should return empty string for empty string', () => {
    expect(pipe.transform('')).toBe('');
  });

  it('should return plain text unchanged', () => {
    expect(pipe.transform('no tags here')).toBe('no tags here');
  });

  it('should handle self-closing tags', () => {
    expect(pipe.transform('line1<br/>line2')).toBe('line1line2');
  });
});
