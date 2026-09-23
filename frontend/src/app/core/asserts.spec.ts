import { assertDefined } from 'app/core/asserts';

describe('assertDefined', () => {
  it('should not throw for a defined value', () => {
    expect(() => assertDefined('hello')).not.toThrow();
  });

  it('should not throw for falsy but defined values', () => {
    expect(() => assertDefined(0)).not.toThrow();
    expect(() => assertDefined('')).not.toThrow();
    expect(() => assertDefined(false)).not.toThrow();
  });

  it('should throw for null', () => {
    expect(() => assertDefined(null)).toThrow(
      'Must not be a nullable or undefined value'
    );
  });

  it('should throw for undefined', () => {
    expect(() => assertDefined(undefined)).toThrow(
      'Must not be a nullable or undefined value'
    );
  });
});
