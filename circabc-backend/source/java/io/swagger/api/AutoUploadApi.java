package io.swagger.api;

import eu.cec.digit.circabc.repo.config.auto.upload.Configuration;
import io.swagger.model.PagedAutoUploadConfiguration;

/**
 * @author schwerr
 */
public interface AutoUploadApi {
  PagedAutoUploadConfiguration getAutoUploadEntries(
    String igId,
    int startItem,
    int limit
  );

  void removeAutoUploadEntry(String igId, long configurationId);

  void toggleAutoUploadEntry(String igId, long configurationId, boolean enable);

  Configuration getAutoUploadEntry(String igId, String nodeId);

  void addAutoUploadEntry(String igId, String autoUploadConfigurationJson);
}
