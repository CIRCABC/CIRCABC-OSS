package io.swagger.api;

import eu.cec.digit.circabc.repo.config.auto.upload.Configuration;
import io.swagger.model.PagedAutoUploadConfiguration;

/**
 * @author schwerr
 */
public interface AutoUploadApi {

    PagedAutoUploadConfiguration getAutoUploadEntries(String igId, int startItem, int limit);

    void removeAutoUploadEntry(long configurationId);

    void toggleAutoUploadEntry(long configurationId, boolean enable);

    Configuration getAutoUploadEntry(String nodeId);

    void addAutoUploadEntry(String autoUploadConfigurationJson);
}
