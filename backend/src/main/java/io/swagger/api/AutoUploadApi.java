package io.swagger.api;

import io.swagger.model.Configuration;
import io.swagger.model.PagedAutoUploadConfiguration;

/**
 * Business operations for managing auto-upload configurations within an Interest Group.
 *
 * <p>Auto-upload configurations define rules that automatically ingest content into a
 * target node of the Alfresco repository. Implementations of this interface encapsulate
 * the logic for listing, retrieving, creating, toggling and removing those
 * configurations, and are typically injected into the CircABC REST webscript endpoints.
 *
 * @author schwerr
 */
public interface AutoUploadApi {
  /**
   * Retrieves a paged list of the auto-upload configurations defined for an Interest Group.
   *
   * @param igId the identifier of the Interest Group whose auto-upload entries are requested
   * @param startItem the zero-based index of the first entry to return (paging offset)
   * @param limit the maximum number of entries to return in the page
   * @return a paged view of the matching auto-upload configurations
   */
  PagedAutoUploadConfiguration getAutoUploadEntries(
    String igId,
    int startItem,
    int limit
  );

  /**
   * Deletes an existing auto-upload configuration owned by the given Interest Group.
   *
   * @param igId the identifier of the authorized Interest Group
   * @param configurationId the identifier of the auto-upload configuration to remove
   */
  void removeAutoUploadEntry(String igId, long configurationId);

  /**
   * Enables or disables an existing auto-upload configuration owned by the given Interest Group.
   *
   * @param igId the identifier of the authorized Interest Group
   * @param configurationId the identifier of the auto-upload configuration to update
   * @param enable {@code true} to enable the configuration, {@code false} to disable it
   */
  void toggleAutoUploadEntry(String igId, long configurationId, boolean enable);

  /**
   * Retrieves the auto-upload configuration associated with a given repository node.
   *
   * @param igId the identifier of the authorized Interest Group
   * @param nodeId the identifier of the node whose auto-upload configuration is requested
   * @return the configuration associated with the node
   */
  Configuration getAutoUploadEntry(String igId, String nodeId);

  /**
   * Creates a new auto-upload configuration from its JSON representation.
   *
   * @param igId the identifier of the authorized Interest Group
   * @param autoUploadConfigurationJson the JSON payload describing the auto-upload
   *     configuration to create
   */
  void addAutoUploadEntry(String igId, String autoUploadConfigurationJson);
}
