package eu.europa.ec.digit.circabc.rest.service.log;

import io.swagger.model.db.LogRecordDAO;
import io.swagger.model.db.LogRestDAO;

/**
 * Service responsible for converting REST-layer log entries into their persistence
 * representation.
 *
 * <p>Implementations map a {@link LogRestDAO}, which carries the log data as produced by the REST
 * layer, onto a {@link LogRecordDAO} suitable for storage. This decouples the log format exposed by
 * the REST endpoints from the format persisted in the database.
 */
public interface LogTransformService {
  /**
   * Transforms a REST log entry into its persistable log record form.
   *
   * @param logRestDAO the REST-layer log entry to convert
   * @return the corresponding {@link LogRecordDAO} ready to be persisted
   */
  LogRecordDAO transform(LogRestDAO logRestDAO);
}
