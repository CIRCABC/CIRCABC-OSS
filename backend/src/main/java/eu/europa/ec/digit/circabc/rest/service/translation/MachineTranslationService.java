package eu.europa.ec.digit.circabc.rest.service.translation;

/**
 * Service responsible for submitting machine translation requests.
 *
 * <p>Implementations handle the dispatch of a {@link MachineTranslationRequest} to the underlying
 * machine translation backend (for example by sending a message to a queue or invoking an external
 * translation service), decoupling callers from the transport and delivery details.
 */
public interface MachineTranslationService {
  /**
   * Submits a machine translation request for processing.
   *
   * @param request the machine translation request describing the content to translate and its
   *     associated parameters; must not be {@code null}
   */
  void sendMessage(MachineTranslationRequest request);
}
