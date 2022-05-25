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
package eu.cec.digit.circabc.repo.lock;

import eu.cec.digit.circabc.repo.lock.ibatis.LockDaoService;
import eu.cec.digit.circabc.service.lock.LockService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DBLockServiceImpl implements LockService {

    private static final Log logger = LogFactory.getLog(DBLockServiceImpl.class);
    private LockDaoService lockDaoService;

    public boolean isLocked(String item) {
        int lockCount = 0;
        try {
            final String itemUpper = item.toUpperCase();
            lockCount = lockDaoService.getLockCount(itemUpper);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Erorr executing lockDaoService.getLockCount  for item  " + item, e);
            }
        }
        return (lockCount == 1);
    }

    @Override
    public boolean tryLock(String item) {

        try {
            final String itemUpper = item.toUpperCase();
            lockDaoService.insertLock(itemUpper);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean tryLockForever(String item) {
        try {
            final String itemUpper = item.toUpperCase();
            lockDaoService.insertLockNulls(itemUpper);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void lock(String item) {
        try {
            final String itemUpper = item.toUpperCase();
            lockDaoService.insertLock(itemUpper);
        } catch (Exception e) {
            throw new IllegalStateException("Can not lock item " + item + " it is already locked.", e);
        }
    }

    @Override
    public void lockForever(String item) {
        try {
            final String itemUpper = item.toUpperCase();
            lockDaoService.insertLockNulls(itemUpper);
        } catch (Exception e) {
            throw new IllegalStateException("Can not lock item " + item + " it is already locked.", e);
        }
    }

    @Override
    public void unlock(String item) {
        try {
            final String itemUpper = item.toUpperCase();
            lockDaoService.deleteLock(itemUpper);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Erorr executing lockDaoService.unlock for item  " + item, e);
            }
        }
    }

    @Override
    public void unlockAll(int hours) {
        try {
            lockDaoService.deleteLocks(hours);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Erorr executing lockDaoService.unlockAll for item  ", e);
            }
        }
    }

    /**
     * @param lockDaoService the lockDaoService to set
     */
    public void setLockDaoService(LockDaoService lockDaoService) {
        this.lockDaoService = lockDaoService;
    }
}
