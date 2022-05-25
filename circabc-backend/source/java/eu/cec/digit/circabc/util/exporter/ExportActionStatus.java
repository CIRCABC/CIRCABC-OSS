package eu.cec.digit.circabc.util.exporter;

/**
 * Different states that the action executer can take.
 *
 * @author schwerr
 */
public enum ExportActionStatus {
    WAITING, READY, STARTED, FINISHED, FINISHED_WITH_ERRORS, FAILED, INTERRUPTED
}
