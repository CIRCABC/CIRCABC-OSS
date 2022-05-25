/**
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
package eu.cec.digit.circabc.service.admin.debug;

/**
 * Base interface to compute the report of a cache.
 *
 * @author Yanick Pignot
 */
public interface CacheReport {

    /**
     * @return the size
     */
    long getSize();

    /**
     * @return the Estimated Max Size
     */
    double getEstimatedMaxSize();

    /**
     * @return the currentSize
     */
    String getCurrentSize();

    /**
     * @return the estMaxSize
     */
    String getEstMaxSize();

    /**
     * @return the hitCount
     */
    String getHitCount();

    /**
     * @return the hitRatio
     */
    String getHitRatio();

    /**
     * @return the maxSize
     */
    String getMaxSize();

    /**
     * @return the percentageFull
     */
    String getPercentageFull();

    /**
     * @return the sizeMB
     */
    String getSizeMB();

    /**
     * @return the totalMissCount
     */
    String getTotalMissCount();

    /**
     * @return the name
     */
    String getName();

    /**
     * @return the memoryStoreEvictionPolicy
     */
    String getMemoryStoreEvictionPolicy();

    /**
     * @return the timeToIdleSeconds
     */
    String getTimeToIdleSeconds();

    /**
     * @return the timeToLiveSeconds
     */
    String getTimeToLiveSeconds();

    /**
     * @return the txMaxItems
     */
    String getTxMaxItems();

    /**
     * @return the clusterType
     */
    String getClusterType();

    /**
     * @return the backupCount
     */
    String getBackupCount();

    /**
     * @return the evictionPercentage
     */
    String getEvictionPercentage();

    /**
     * @return the mergePolicy
     */
    String getMergePolicy();

    /**
     * @return the dirtyEntryCount
     */
    String getDirtyEntryCount();
}
