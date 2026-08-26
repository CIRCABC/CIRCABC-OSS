import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'cbcNodeRef',
  pure: true,
})
export class NodeRefPipe implements PipeTransform {
  public transform(nodeId: string): string {
    if (nodeId) {
      return `workspace://SpacesStore/${nodeId}`;
    }
    throw new Error('nodeRef should be provided');
  }
}
