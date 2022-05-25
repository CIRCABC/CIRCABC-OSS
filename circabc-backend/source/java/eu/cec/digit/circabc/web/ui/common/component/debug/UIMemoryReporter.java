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
package eu.cec.digit.circabc.web.ui.common.component.debug;

import eu.cec.digit.circabc.service.admin.debug.CacheReport;
import eu.cec.digit.circabc.service.admin.debug.ServerConfigurationService;
import eu.cec.digit.circabc.web.Services;
import org.alfresco.web.ui.common.component.debug.BaseDebugComponent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.SchedulerException;

import javax.faces.context.FacesContext;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Component which displays the Memory usage and the cache management
 *
 * @author Yanick Pignot
 */
public class UIMemoryReporter extends BaseDebugComponent {

    /**
     * A logger for the class
     */
    final static Log logger = LogFactory.getLog(UIMemoryReporter.class);
    private static final String STARS = "*************************************";
    private static final String[] FIRST_LINES_KEYS =
            {
                    "Runtime max Heap Size",
                    "Runtime Free Memory",
                    "Runtime Total memory",
                    "Runtime Available Processors",
                    "Caches Total Size",
                    "Estimated Max Size",
                    "Caches currently consume MB",
                    "Caches currently consume %",
                    "The estimated maximum size is MB",
                    "The estimated maximum size is %"
            };
    private transient ServerConfigurationService serverConfigurationService;

    /**
     * @see javax.faces.component.UIComponent#getFamily()
     */
    public String getFamily() {
        return "eu.cec.digit.circabc.faces.debug.MemoryReporter";
    }

    /**
     * @see org.alfresco.web.ui.common.component.debug.BaseDebugComponent#getDebugData()
     */
    @SuppressWarnings("unchecked")
    public Map getDebugData() {
        Map properties;

        final Map propertiesFull = new LinkedHashMap();
        final Map propertiesBestUsed = new LinkedHashMap();
        final Map propertiesAverageUsed = new LinkedHashMap();
        final Map propertiesBadUsed = new LinkedHashMap();
        final Map propertiesUnused = new LinkedHashMap();

        try {
            final List<CacheReport> caches = getServerConfigurationService().getCaches();

            final long maxHeapSize = Runtime.getRuntime().maxMemory();
            final long freeMemory = Runtime.getRuntime().freeMemory();
            final long totalMemory = Runtime.getRuntime().totalMemory();
            final int availableProcessors = Runtime.getRuntime().availableProcessors();

            long allCachesTotalSize = 0L;
            double estimatedMaxSize = 0L;

            // Put these keys now to reserve the first places of LinkedHashMap
            for (final String title : FIRST_LINES_KEYS) {
                propertiesFull.put(title, "");
            }

            String name;
            String hitRatio;
            Double hitRatioValue;
            String hitRationColorBegin;
            String hitRationColorEnd;

            String percentageFullBegin;
            String percentageFullEnd;

            String percentageFull;
            Double percentageFullValue;

            double cacheEstimatedMaxSize;

            final String high = "Green";
            final String medium = "#cc00ff";
            final String low = "Red";

            for (final CacheReport cache : caches) {
                hitRationColorBegin = "";
                hitRationColorEnd = "";
                percentageFullBegin = "";
                percentageFullEnd = "";
                hitRatioValue = null;
                // get the size
                allCachesTotalSize += cache.getSize();
                cacheEstimatedMaxSize = cache.getEstimatedMaxSize();
                estimatedMaxSize +=
                        (Double.isNaN(cacheEstimatedMaxSize) || Double.isInfinite(cacheEstimatedMaxSize))
                                ? 0.0
                                : cacheEstimatedMaxSize;

                name = cache.getName();
                hitRatio = cache.getHitRatio();
                if (!hitRatio.endsWith("NaN percent")) {
                    hitRatio = hitRatio.substring(0, hitRatio.length() - "percent".length());
                    hitRatio = hitRatio.trim();
                    hitRatio = hitRatio.replace(',', '.');
                    logger.debug(hitRatio);
                    hitRatioValue = Double.parseDouble(hitRatio);
                    logger.debug("hitRatioValue" + hitRatioValue);

                    if (hitRatioValue >= 80) {
                        properties = propertiesBestUsed;
                        hitRationColorBegin = "<font color=\"" + high + "\">";
                        hitRationColorEnd = "</font>";
                    } else if (hitRatioValue >= 60) {
                        properties = propertiesAverageUsed;
                        hitRationColorBegin = "<font color=\"" + medium + "\">";
                        hitRationColorEnd = "</font>";
                    } else if (hitRatioValue != 0) {
                        properties = propertiesBadUsed;
                        hitRationColorBegin = "<font color=\"" + low + "\">";
                        hitRationColorEnd = "</font>";
                    } else {
                        properties = propertiesUnused;
                        hitRatioValue = null;
                    }
                } else {
                    properties = propertiesUnused;
                }

                percentageFull = cache.getPercentageFull();
                if (hitRatioValue != null) {
                    percentageFull = percentageFull
                            .substring(0, percentageFull.length() - "percent".length());
                    percentageFull = percentageFull.trim();
                    percentageFull = percentageFull.replace(',', '.');
                    percentageFullValue = Double.valueOf(percentageFull);
                    logger.debug("totalMissCountValue" + percentageFullValue);

                    if (percentageFullValue >= 80) {
                        percentageFullBegin = "<font color=\"" + low + "\">";
                        percentageFullEnd = "</font>";
                    } else if (percentageFullValue >= 60) {
                        percentageFullBegin = "<font color=\"" + high + "\">";
                        percentageFullEnd = "</font>";
                    } else {
                        percentageFullBegin = "<font color=\"" + medium + "\">";
                        percentageFullEnd = "</font>";
                    }
                }

                properties.put("<b>Cache: </b>" + name, "");
                properties.put(hitRationColorBegin + name + " - Hit Ratio" + hitRationColorEnd,
                        hitRationColorBegin + cache.getHitRatio() + hitRationColorEnd);
                properties.put(name + " - Hit Count", cache.getHitCount());
                properties.put(name + " - Miss Count", cache.getPercentageFull());
                properties.put(name + " - Deep Size", cache.getSizeMB());
                properties.put(name + " - Current Count", cache.getCurrentSize());
                properties.put(name + " - Dirty Entry Count", cache.getDirtyEntryCount());
                properties.put(percentageFullBegin + name + " - Percentage used" + percentageFullEnd,
                        percentageFullBegin + cache.getPercentageFull() + percentageFullEnd);
                properties.put(name + " - Max Count", cache.getMaxSize());
                properties.put(name + " - TX Max Count", cache.getTxMaxItems());
                properties.put(name + " - Cluster Type", cache.getClusterType());
                properties.put(name + " - Backup Count", cache.getBackupCount());
                properties.put(name + " - Estimated maximum size", cache.getEstMaxSize());
                properties
                        .put(name + " - Memory Store Eviction Policy", cache.getMemoryStoreEvictionPolicy());
                properties.put(name + " - Eviction Percentage", cache.getEvictionPercentage());
                properties.put(name + " - Merge Policy", cache.getMergePolicy());
                properties.put(name + " - Time To Live Seconds", cache.getTimeToLiveSeconds());
                properties.put(name + " - Time To Idle Seconds", cache.getTimeToIdleSeconds());
            }

            int propertiesByCache = 19;

            propertiesFull.put(STARS, STARS);
            propertiesFull.put("<b>Caches with Hit Ratio >= 80% FOUND</b>",
                    (propertiesBestUsed.size() / propertiesByCache) + "/" + caches.size());
            propertiesFull.put(STARS, STARS);
            propertiesFull.putAll(propertiesBestUsed);

            propertiesFull.put(STARS, STARS);
            propertiesFull.put("<b>Caches with 80% > Hit Ratio >= 60% FOUND</b>",
                    (propertiesAverageUsed.size() / propertiesByCache) + "/" + caches.size());
            propertiesFull.put(STARS, STARS);
            propertiesFull.putAll(propertiesAverageUsed);

            propertiesFull.put(STARS, STARS);
            propertiesFull.put("<b>Caches with Hit Ratio < 60% FOUND</b>",
                    (propertiesBadUsed.size() / propertiesByCache) + "/" + caches.size());
            propertiesFull.put(STARS, STARS);
            propertiesFull.putAll(propertiesBadUsed);

            propertiesFull.put(STARS, STARS);
            propertiesFull.put("<b>Caches Unused FOUND</b>",
                    (propertiesUnused.size() / propertiesByCache) + "/" + caches.size());
            propertiesFull.put(STARS, STARS);
            propertiesFull.putAll(propertiesUnused);

            double sizePercentage = (double) allCachesTotalSize / (double) maxHeapSize * 100.0;
            double maxSizePercentage = estimatedMaxSize / (double) maxHeapSize * 100.0;

            // fill the missing values
            propertiesFull
                    .put(FIRST_LINES_KEYS[0], String.format("%5.2f MB", maxHeapSize / 1024.0 / 1024.0));
            propertiesFull
                    .put(FIRST_LINES_KEYS[1], String.format("%5.2f MB", freeMemory / 1024.0 / 1024.0));
            propertiesFull
                    .put(FIRST_LINES_KEYS[2], String.format("%5.2f MB", totalMemory / 1024.0 / 1024.0));
            propertiesFull.put(FIRST_LINES_KEYS[3], availableProcessors);
            propertiesFull.put(FIRST_LINES_KEYS[4],
                    String.format("%5.2f MB", allCachesTotalSize / 1024.0 / 1024.0));
            propertiesFull
                    .put(FIRST_LINES_KEYS[5], String.format("%5.2f MB", estimatedMaxSize / 1024.0 / 1024.0));
            propertiesFull.put(FIRST_LINES_KEYS[6],
                    String.format("%5.2f MB", (double) allCachesTotalSize / 1024.0 / 1024.0));
            propertiesFull
                    .put(FIRST_LINES_KEYS[7], String.format("%3.2f percent of VM size", sizePercentage));
            propertiesFull
                    .put(FIRST_LINES_KEYS[8], String.format("%5.2f MB", estimatedMaxSize / 1024.0 / 1024.0));
            propertiesFull
                    .put(FIRST_LINES_KEYS[9], String.format("%3.2f percent of VM size", maxSizePercentage));
        } catch (final SchedulerException e) {
            logger.error("Error during geting debug data ", e);
        }
        return propertiesFull;
    }

    /**
     * @return the serverConfigurationService
     */
    protected final ServerConfigurationService getServerConfigurationService() {
        if (serverConfigurationService == null) {
            serverConfigurationService = Services
                    .getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getServerConfigurationService();
        }
        return serverConfigurationService;
    }
}
