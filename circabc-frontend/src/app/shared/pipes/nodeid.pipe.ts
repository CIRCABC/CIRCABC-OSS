import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'cbcNodeId',
  pure: true,
})
export class NodeIdPipe implements PipeTransform {
  public transform(nodeRef: string): string {
    if (nodeRef) {
      const result = nodeRef.replace('workspace://SpacesStore/', '');
      if (result.length === nodeRef.length) {
        throw new Error(`nodeRef is invalid:${nodeRef}`);
      } else {
        return result;
      }
    } else {
      throw new Error('nodeRef should be provided');
    }
  }
}
