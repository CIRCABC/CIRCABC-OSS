import { NodeRefPipe } from './noderef.pipe';

describe('NodeRefPipe', () => {
  const pipe = new NodeRefPipe();

  it('should return a workspace nodeRef for a given nodeId', () => {
    expect(pipe.transform('abc-123')).toBe('workspace://SpacesStore/abc-123');
  });

  it('should throw an error when nodeId is empty', () => {
    expect(() => pipe.transform('')).toThrow('nodeRef should be provided');
  });
});
