import { NodeIdPipe } from 'app/shared/pipes/nodeid.pipe';

describe('NodeIdPipe', () => {
  const pipe = new NodeIdPipe();

  it('should strip workspace://SpacesStore/ prefix from nodeRef', () => {
    expect(pipe.transform('workspace://SpacesStore/abc-123')).toBe('abc-123');
  });

  it('should throw if nodeRef does not contain the expected prefix', () => {
    expect(() => pipe.transform('invalid-ref')).toThrow(
      'nodeRef is invalid:invalid-ref'
    );
  });

  it('should throw if nodeRef is empty', () => {
    expect(() => pipe.transform('')).toThrow('nodeRef should be provided');
  });
});
