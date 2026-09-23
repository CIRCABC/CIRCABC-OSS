import appInfo from 'app/app-info.json';

describe('appInfo', () => {
  it('should have appVersion property', () => {
    expect(appInfo.appVersion).toBeDefined();
    expect(typeof appInfo.appVersion).toBe('string');
  });

  it('should have alfVersion property', () => {
    expect(appInfo.alfVersion).toBeDefined();
    expect(typeof appInfo.alfVersion).toBe('string');
  });

  it('should have buildDate property', () => {
    expect(appInfo.buildDate).toBeDefined();
    expect(typeof appInfo.buildDate).toBe('string');
  });

  it('should have expected structure', () => {
    expect(appInfo).toEqual({
      appVersion: expect.any(String),
      alfVersion: expect.any(String),
      buildDate: expect.any(String),
    });
  });
});
