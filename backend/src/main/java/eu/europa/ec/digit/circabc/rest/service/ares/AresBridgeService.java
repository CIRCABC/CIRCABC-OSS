package eu.europa.ec.digit.circabc.rest.service.ares;

/**
 * Service abstraction for integrating CIRCABC with the Ares system (the European Commission's
 * document registration and management platform).
 *
 * <p>Implementations of this interface encapsulate the business logic required to bridge CIRCABC
 * content or events with Ares, allowing the integration workflow to be triggered without exposing
 * the underlying details to callers.
 */
public interface AresBridgeService {
  /**
   * Executes the Ares bridging workflow.
   *
   * <p>Runs the integration logic that synchronizes or forwards the relevant CIRCABC data to the
   * Ares system. The concrete behavior (what is processed and how) is defined by the implementing
   * class.
   */
  void process();
}
