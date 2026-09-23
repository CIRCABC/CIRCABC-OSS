import { Pipe, PipeTransform } from '@angular/core';

/**
 * Pure pipe that converts a bare Alfresco node identifier into a fully
 * qualified NodeRef string.
 *
 * Used in templates via the `cbcNodeRef` name to prefix a raw node id with the
 * Alfresco `workspace://SpacesStore/` store reference, producing a NodeRef the
 * backend can resolve.
 *
 * @example
 * {{ '1234-5678' | cbcNodeRef }}
 * // => 'workspace://SpacesStore/1234-5678'
 */
@Pipe({
  name: 'cbcNodeRef',
  pure: true,
})
export class NodeRefPipe implements PipeTransform {
  /**
   * Builds an Alfresco NodeRef from a node identifier.
   *
   * @param nodeId - The bare node identifier to convert.
   * @returns The fully qualified NodeRef string in the form
   * `workspace://SpacesStore/{nodeId}`.
   * @throws Error If `nodeId` is falsy (missing or empty).
   */
  public transform(nodeId: string): string {
    if (nodeId) {
      return `workspace://SpacesStore/${nodeId}`;
    }
    throw new Error('nodeRef should be provided');
  }
}
