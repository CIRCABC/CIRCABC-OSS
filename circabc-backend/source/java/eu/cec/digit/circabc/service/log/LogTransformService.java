package eu.cec.digit.circabc.service.log;

import eu.cec.digit.circabc.repo.log.LogRecordDAO;
import eu.cec.digit.circabc.repo.log.LogRestDAO;

public interface LogTransformService {
    LogRecordDAO transform (LogRestDAO logRestDAO);
}
