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
package eu.cec.digit.circabc.service.lock;

/**
 *
 */
public interface LockService {

    /**
     * Lock item in case of error unlike tryLock method it throw IllegalStateException
     *
     * @param item
     */
    void lock(String item);

    /**
     * Lock item in way that is not removed by UnlockJob,
     * in case of error unlike tryLock method it throw IllegalStateException
     *
     * @param item
     */
    void lockForever(String item);

    /**
     * Unlock item if it is locked , otherwise do nothing
     *
     * @param item case insensitive name of lock item
     */
    void unlock(String item);

    /**
     * Check if item is already locked
     *
     * @param item case insensitive name of lock item
     * @return true if item is already locked false otherwise
     */
    boolean isLocked(String item);

    /**
     * Try to lock item
     *
     * @param item case insensitive name of lock item
     * @return true if item cuseed to locked item false otherwise
     */
    boolean tryLock(String item);


    /**
     * Try to lock item in way that is not removed by UnlockJob
     *
     * @param item case insensitive name of lock item
     * @return true if item cuseed to locked item false otherwise
     */
    boolean tryLockForever(String item);

    /**
     * Unlock all locks that are older then @param hours
     *
     * @param hours nomber of hours
     */
    void unlockAll(int hours);
}
