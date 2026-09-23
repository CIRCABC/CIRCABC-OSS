import { Pipe, PipeTransform } from '@angular/core';

/**
 * Pure Angular pipe (`cbcNodeId`) that extracts the bare node identifier from
 * an Alfresco node reference by stripping the `workspace://SpacesStore/`
 * store prefix.
 *
 * Being pure, it is only re-evaluated when its input reference changes,
 * making it cheap to use in templates that display raw node ids.
 *
 * @example
 * ```html
 * {{ 'workspace://SpacesStore/1234-5678' | cbcNodeId }}
 * <!-- renders: 1234-5678 -->
 * ```
 */
@Pipe({
  name: 'cbcNodeId',
  pure: true,
})
export class NodeIdPipe implements PipeTransform {
  /**
   * Transforms a fully-qualified Alfresco node reference into its bare node id
   * by removing the `workspace://SpacesStore/` prefix.
   *
   * @param nodeRef The Alfresco node reference, e.g.
   * `workspace://SpacesStore/1234-5678`.
   * @returns The node identifier without the store prefix.
   * @throws {Error} If `nodeRef` is falsy (no reference provided).
   * @throws {Error} If `nodeRef` does not contain the expected
   * `workspace://SpacesStore/` prefix and is therefore considered invalid.
   */
  public transform(nodeRef: string): string {
    if (nodeRef) {
      const result = nodeRef.replace('workspace://SpacesStore/', '');
      if (result.length === nodeRef.length) {
        throw new Error(`nodeRef is invalid:${nodeRef}`);
      }
      return result;
    }
    throw new Error('nodeRef should be provided');
  }
}
