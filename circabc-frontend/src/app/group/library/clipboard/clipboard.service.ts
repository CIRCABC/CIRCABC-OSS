import { Injectable } from '@angular/core';
import { Node } from 'app/core/generated/circabc';
import { Observable, Subject } from 'rxjs';

@Injectable()
export class ClipboardService {
  // communication channel between the clipboard copy actions
  // that trigger the addition to the clipboard, and the clipboard itself
  // it updates the clipboard state on the fly without needing to poll from it,
  // but listening in background for data to be pushed into the clipboard

  private addedItems: Subject<Node> = new Subject<Node>();
  private removedItems: Subject<Node> = new Subject<Node>();

  public itemsAdded$: Observable<Node> = this.addedItems.asObservable();
  public itemsRemoved$: Observable<Node> = this.removedItems.asObservable();

  public addItem(node: Node, saveInSession = false, groupId = ''): void {
    if (saveInSession) {
      let nodes: Node[] = [];
      const json = sessionStorage.getItem(`cbc-clipboard${groupId}`);

      if (json !== null) {
        nodes = JSON.parse(json);
      }

      if (nodes.find((a) => a.id === node.id)) {
        return;
      }
      nodes.push(node);
      sessionStorage.setItem(`cbc-clipboard${groupId}`, JSON.stringify(nodes));
    }
    this.addedItems.next(node);
  }

  public removeItem(node: Node): void {
    this.removedItems.next(node);
  }
}
