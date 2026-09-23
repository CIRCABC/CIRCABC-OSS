package io.swagger.api;

/**
 * Defines the business operations for validating CAPTCHA challenges.
 *
 * <p>Implementations verify that the answer supplied by a client matches the
 * expected solution for a previously issued CAPTCHA challenge, typically as a
 * bot-protection measure for public-facing operations (e.g. self-registration
 * or anonymous form submissions).
 */
public interface CaptchaApi {
  /**
   * Validates a CAPTCHA answer against a previously issued challenge.
   *
   * @param captchaToken the token associated with the issued CAPTCHA challenge
   * @param captchaId the identifier of the CAPTCHA challenge to validate against
   * @param answer the answer provided by the client for the challenge
   * @return {@code true} if the supplied answer is correct for the given
   *     challenge, {@code false} otherwise
   */
  boolean validate(String captchaToken, String captchaId, String answer);
}
