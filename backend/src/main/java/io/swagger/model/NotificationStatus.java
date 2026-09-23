package io.swagger.model;

/*
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */

/**
 * Enumerates the possible notification subscription states for a user on a given node or service.
 *
 * <p>A notification status expresses whether a user has explicitly opted in or out of
 * notifications, or whether the effective setting is derived from a parent (inherited) rather than
 * set directly on the current item.
 *
 * @author Yanick Pignot
 */
public enum NotificationStatus {
  /** The user is explicitly subscribed and will receive notifications. */
  SUBSCRIBED,
  /** The user is explicitly unsubscribed and will not receive notifications. */
  UNSUBSCRIBED,
  /** No explicit setting exists; the effective status is inherited from a parent. */
  INHERITED,
}
