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
package eu.cec.digit.circabc.repo.lock.ibatis;

import java.util.Date;

import org.mybatis.spring.SqlSessionTemplate;

/**
 * @author Slobodan Filipovic
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 IBatisSqlMapper was moved to Spring.
 */
public class IBatisLockDaoServiceImpl implements LockDaoService {

    private SqlSessionTemplate sqlSessionTemplate = null;

    public int getLockCount(String item) {
        return (Integer) sqlSessionTemplate.selectOne("CircabcLock.select_lock_count", item);
    }

    public void insertLock(String item) {
        sqlSessionTemplate.insert("CircabcLock.insert_lock", item);
    }

    @Override
    public void insertLockNulls(String item) {
        sqlSessionTemplate.insert("CircabcLock.insert_lock_nulls", item);
    }

    public void deleteLock(String item) {
        sqlSessionTemplate.delete("CircabcLock.delete_lock", item);
    }

    public void deleteLocks(int hours) {
        Date date =  new Date(System.currentTimeMillis() - hours * 3600 * 1000);
        sqlSessionTemplate.delete("CircabcLock.delete_locks", date);
    }

    /**
     * @param sqlSessionTemplate the sqlSessionTemplate to set
     */
    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }
}
