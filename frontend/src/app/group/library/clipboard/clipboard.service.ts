import { Service } from '@angular/core';
import { Node } from 'app/core/generated/circabc';
import { Observable, Subject } from 'rxjs';

/**
 * Service that acts as a communication channel for the document library
 * clipboard feature.
 *
 * It bridges the clipboard copy actions (which trigger the addition of
 * {@link Node}s to the clipboard) and the clipboard view itself. Rather than
 * polling for clipboard state, consumers subscribe to the exposed observable
 * streams and are notified in the background whenever an item is pushed to or
 * removed from the clipboard.
 *
 * Optionally, added items can be persisted in the browser `sessionStorage`
 * (keyed per group) so the clipboard survives navigation within a session.
 *
 * Key collaborators:
 * - {@link Node}: the library items exchanged through the clipboard.
 * - RxJS `Subject`/`Observable`: used to broadcast add/remove events.
 * - `sessionStorage`: used for optional per-group persistence.
 */
@Service({ autoProvided: false })
export class ClipboardService {
  // communication channel between the clipboard copy actions
  // that trigger the addition to the clipboard, and the clipboard itself
  // it updates the clipboard state on the fly without needing to poll from it,
  // but listening in background for data to be pushed into the clipboard

  /** Source subject that emits a node whenever an item is added to the clipboard. */
  private readonly addedItems: Subject<Node> = new Subject<Node>();
  /** Source subject that emits a node whenever an item is removed from the clipboard. */
  private readonly removedItems: Subject<Node> = new Subject<Node>();

  /** Observable stream emitting each {@link Node} added to the clipboard. */
  public itemsAdded$: Observable<Node> = this.addedItems.asObservable();
  /** Observable stream emitting each {@link Node} removed from the clipboard. */
  public itemsRemoved$: Observable<Node> = this.removedItems.asObservable();

  /**
   * Adds a node to the clipboard by broadcasting it on {@link itemsAdded$}.
   *
   * When `saveInSession` is enabled, the node is also persisted to
   * `sessionStorage` under the `cbc-clipboard<groupId>` key. Duplicate nodes
   * (matched by `id`) already present in the stored list are ignored and the
   * method returns without broadcasting.
   *
   * @param node - The library node to add to the clipboard.
   * @param saveInSession - Whether to persist the node in `sessionStorage`. Defaults to `false`.
   * @param groupId - The group identifier used to scope the `sessionStorage` key. Defaults to an empty string.
   * @returns Nothing.
   */
  public addItem(node: Node, saveInSession = false, groupId = ''): void {
    if (saveInSession) {
      let nodes: Node[] = [];
      const json = sessionStorage.getItem(`cbc-clipboard${groupId}`);

      if (json !== null) {
        nodes = JSON.parse(json) as Node[];
      }

      if (nodes.some((a) => a.id === node.id)) {
        return;
      }
      nodes.push(node);
      sessionStorage.setItem(`cbc-clipboard${groupId}`, JSON.stringify(nodes));
    }
    this.addedItems.next(node);
  }

  /**
   * Removes a node from the clipboard by broadcasting it on {@link itemsRemoved$}.
   *
   * Note: this only notifies subscribers; it does not update any
   * `sessionStorage` persistence written by {@link addItem}.
   *
   * @param node - The library node to remove from the clipboard.
   * @returns Nothing.
   */
  public removeItem(node: Node): void {
    this.removedItems.next(node);
  }
}
