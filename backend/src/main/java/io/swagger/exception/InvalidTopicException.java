package io.swagger.exception;

/**
 * Checked exception signalling that a topic referenced or manipulated in the
 * REST layer is invalid.
 *
 * <p>Typically thrown when an operation targets a node that is expected to be a
 * newsgroup topic but does not exist, is not a topic, or otherwise fails
 * topic-level validation.
 *
 * @author beaurpi
 */
public class InvalidTopicException extends Exception {

  /**
   * Serialization version identifier used to verify class compatibility during
   * deserialization.
   */
  private static final long serialVersionUID = 1317649542000367706L;
}
