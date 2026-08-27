package io.swagger.api;

import java.util.Map;

public interface SyncApi {
  /**
   * Full resync: deletes all CBC data and reloads from Alfresco.
   */
  void syncAll();

  /**
   * Resync only users (adds missing users to CBC).
   */
  void syncUsers();

  /**
   * Resync a specific Interest Group by its node ID.
   */
  void syncGroup(String groupId);

  /**
   * Resync a specific Category by its node ID.
   */
  void syncCategory(String categoryId);

  /**
   * Resync CIRCABC administrators.
   */
  void syncAdmins();

  /**
   * Returns a status map with counts from ALF and CBC tables to detect
   * desynchronization.
   */
  Map<String, Object> getSyncStatus();
}
