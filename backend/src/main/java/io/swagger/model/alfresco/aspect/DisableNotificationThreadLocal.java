/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package io.swagger.model.alfresco.aspect;

/**
 * Thread-scoped holder for a boolean flag that controls whether notifications
 * should be suppressed for the current thread of execution.
 *
 * <p>Certain operations (for example bulk imports or administrative changes)
 * need to mutate content without triggering the notification side effects that
 * are normally raised by Alfresco aspects/behaviours. This class exposes a
 * per-thread {@link Boolean} flag that such behaviours can consult: when the
 * flag is set to {@code true}, notifications are expected to be disabled for
 * work performed on that thread.</p>
 *
 * <p>The backing value is stored in a {@link ThreadLocal}, so each thread sees
 * its own independent flag and the default value is {@link Boolean#FALSE}
 * (notifications enabled). Callers that set the flag should clear it via
 * {@link #remove()} once the work is complete to avoid leaking state across
 * pooled/reused threads.</p>
 */
public class DisableNotificationThreadLocal {

  /**
   * Shared, thread-local backing store for the flag. Each thread accessing this
   * holder gets its own value, defaulting to {@link Boolean#FALSE}.
   */
  private static ThreadLocalBoolean flag = new ThreadLocalBoolean();

  /**
   * Clears the flag for the current thread, resetting it to its default value
   * ({@link Boolean#FALSE}). Should be called once the disabling context is no
   * longer needed to prevent state from leaking to subsequent tasks that reuse
   * the same thread.
   */
  public void remove() {
    flag.remove();
  }

  /**
   * Sets the notification-disabling flag for the current thread.
   *
   * @param value {@code true} to suppress notifications for the current thread,
   *     {@code false} to (re-)enable them
   */
  public void set(Boolean value) {
    flag.set(value);
  }

  /**
   * Returns the current thread's notification-disabling flag.
   *
   * @return {@code true} if notifications are disabled for the current thread,
   *     otherwise {@code false} (the default when the flag has not been set)
   */
  public Boolean get() {
    return flag.get();
  }

  /**
   * {@link ThreadLocal} specialization that supplies a non-{@code null} default
   * of {@link Boolean#FALSE}, ensuring notifications are enabled unless a thread
   * explicitly disables them.
   */
  private static class ThreadLocalBoolean extends ThreadLocal<Boolean> {

    /**
     * Provides the initial per-thread value.
     *
     * @return {@link Boolean#FALSE}, meaning notifications are enabled by
     *     default
     */
    @Override
    public Boolean initialValue() {
      return Boolean.FALSE;
    }
  }
}
