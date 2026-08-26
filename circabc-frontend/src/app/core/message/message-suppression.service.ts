import { Injectable } from '@angular/core';

interface SuppressedEntry {
  id: string;
  contentHash: string;
}

@Injectable({
  providedIn: 'root',
})
export class MessageSuppressionService {
  private readonly STORAGE_KEY = 'suppressedMessages';

  /**
   * Check if a message is suppressed with the given content.
   * Returns false if the content has changed since suppression.
   */
  public isSuppressed(messageId: string, content: string): boolean {
    const entries = this.getSuppressedEntries();
    const entry = entries.find((e) => e.id === messageId);
    if (!entry) {
      return false;
    }
    return entry.contentHash === this.hashContent(content);
  }

  /**
   * Suppress a message by storing its ID and a hash of its content.
   * If the content changes later, the message will reappear.
   */
  public suppress(messageId: string, content: string): void {
    const entries = this.getSuppressedEntries();
    const contentHash = this.hashContent(content);
    const existingIndex = entries.findIndex((e) => e.id === messageId);

    if (existingIndex >= 0) {
      entries[existingIndex].contentHash = contentHash;
    } else {
      entries.push({ id: messageId, contentHash });
    }

    this.saveSuppressedEntries(entries);
  }

  /**
   * Remove a message from the suppression list
   */
  public unsuppress(messageId: string): void {
    const entries = this.getSuppressedEntries();
    const filtered = entries.filter((e) => e.id !== messageId);
    this.saveSuppressedEntries(filtered);
  }

  /**
   * Get all suppressed message IDs
   */
  public getSuppressedIds(): string[] {
    return this.getSuppressedEntries().map((e) => e.id);
  }

  /**
   * Generate a simple hash from content string for change detection
   */
  private hashContent(content: string): string {
    let hash = 0;
    for (let i = 0; i < content.length; i++) {
      const char = content.charCodeAt(i);
      hash = (hash << 5) - hash + char;
      hash = hash & hash; // Convert to 32-bit integer
    }
    return hash.toString(36);
  }

  private getSuppressedEntries(): SuppressedEntry[] {
    try {
      const stored = localStorage.getItem(this.STORAGE_KEY);
      if (!stored) {
        return [];
      }
      const parsed: unknown = JSON.parse(stored);
      if (!Array.isArray(parsed)) {
        return [];
      }
      // Handle migration from old format (string[]) to new format (SuppressedEntry[])
      if (parsed.length > 0 && typeof parsed[0] === 'string') {
        return (parsed as string[]).map((id) => ({ id, contentHash: '' }));
      }
      return parsed as SuppressedEntry[];
    } catch (error) {
      console.error(
        'Failed to read suppressed messages from local storage',
        error
      );
      return [];
    }
  }

  private saveSuppressedEntries(entries: SuppressedEntry[]): void {
    try {
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(entries));
    } catch (error) {
      console.error(
        'Failed to save suppressed messages to local storage',
        error
      );
    }
  }
}
