/**
 * Cross-tab `sessionStorage` replication used as an application initializer.
 *
 * Browsers scope `sessionStorage` to a single tab, so a freshly opened tab
 * starts empty even when the user already has an authenticated session in
 * another tab. This module bridges that gap using the `storage` event on
 * `localStorage` (which *is* shared across tabs) as a signalling channel:
 *
 * 1. A new tab writes a `getSessionStorage` marker to `localStorage`.
 * 2. Existing tabs react to the `storage` event and dump their `sessionStorage`
 *    into `localStorage` under the `sessionStorage` key.
 * 3. The new tab reads that payload back and repopulates its own
 *    `sessionStorage`.
 *
 * A 1 second timeout guards the case where no other tab responds.
 */
function transferSessionStorage(): Promise<void> {
  return new Promise<void>((resolve) => {
    let dataReceived = false;

    const sessionStorage_transfer = (event: StorageEvent) => {
      let localEvent: StorageEvent = event;
      if (!localEvent) {
        localEvent = window.event as StorageEvent; // NOSONAR - IE compatibility fallback
      } // ie suq
      if (!localEvent.newValue) {
        return;
      } // do nothing if no value to work with
      if (localEvent.key === 'getSessionStorage') {
        // another tab asked for the sessionStorage -> send it
        localStorage.setItem('sessionStorage', JSON.stringify(sessionStorage));
        // the other tab should now have it, so we're done with it.
        localStorage.removeItem('sessionStorage'); // <- could do short timeout as well.
      } else if (
        localEvent.key === 'sessionStorage' &&
        !sessionStorage.length
      ) {
        // another tab sent data <- get it
        const data = JSON.parse(localEvent.newValue) as Record<string, string>;
        for (const key in data) {
          sessionStorage.setItem(key, data[key]);
        }

        // Session data received, mark as done and resolve
        dataReceived = true;
        resolve();
      }
    };

    // listen for changes to localStorage
    if (globalThis.addEventListener) {
      globalThis.addEventListener('storage', sessionStorage_transfer, false);
    } else {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      (globalThis as any).attachEvent('onstorage', sessionStorage_transfer);
    }

    // Ask other tabs for session storage (this is ONLY to trigger event)
    if (sessionStorage.length) {
      // Already has session data, resolve immediately
      resolve();
    } else {
      localStorage.setItem('getSessionStorage', 'dummy');
      localStorage.removeItem('getSessionStorage');

      // The timeout is needed as a fallback in case no other tabs respond
      setTimeout(() => {
        if (!dataReceived) {
          resolve(); // Resolve anyway after timeout
        }
      }, 1000);
    }
  });
}

/**
 * Factory that produces the `provideAppInitializer` callback which performs the
 * cross-tab {@link transferSessionStorage} handshake during bootstrap.
 *
 * @returns An initializer function returning a promise that resolves once the
 * session storage has been transferred (or the fallback timeout elapses).
 */
export function sessionStorageInitializer(): () => Promise<void> {
  return () => transferSessionStorage();
}
