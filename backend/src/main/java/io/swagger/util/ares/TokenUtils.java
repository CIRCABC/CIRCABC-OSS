package io.swagger.util.ares;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility for creating and verifying HMAC-based authentication tokens used when
 * integrating with the ARES service.
 *
 * <p>A token is computed as the hexadecimal, lower-cased HMAC-SHA256 digest of the
 * concatenation of the API key, request date, HTTP method and target URL, keyed with
 * a shared secret. This allows the recipient to verify that a request originates from a
 * party holding the shared secret and that the signed request details have not been
 * tampered with.
 *
 * <p>This class is not meant to be instantiated; all functionality is exposed through
 * static methods.
 */
public class TokenUtils {

  /** Logger used to report token generation and validation errors. */
  private static final Log logger = LogFactory.getLog(TokenUtils.class);

  /** Name of the MAC algorithm used to compute the token (HMAC with SHA-256). */
  private static final String ALGORITHM = "HmacSHA256";

  /** Prevents instantiation of this static utility class. */
  private TokenUtils() {}

  /**
   * Generates an HMAC-SHA256 authentication token for the given request details.
   *
   * <p>The token is derived from the concatenation of {@code apiKey}, {@code date},
   * {@code httpMethod} and {@code url}, signed with the supplied {@code secret} and
   * returned as a lower-cased hexadecimal string.
   *
   * @param apiKey the API key identifying the caller
   * @param secret the shared secret used as the HMAC key
   * @param date the request date, included in the signed payload
   * @param httpMethod the HTTP method of the request (e.g. {@code GET}, {@code POST})
   * @param url the target request URL
   * @return the lower-cased hexadecimal HMAC-SHA256 token, or {@code null} if the token
   *     could not be computed (e.g. the algorithm is unavailable or the key is invalid)
   */
  public static String generateToken(
    String apiKey,
    String secret,
    String date,
    String httpMethod,
    String url
  ) {
    String concatenatedString = apiKey + date + httpMethod + url;

    Mac hmacSHA256;
    try {
      hmacSHA256 = Mac.getInstance(ALGORITHM);

      SecretKeySpec secretKey = new SecretKeySpec(
        secret.getBytes(StandardCharsets.UTF_8),
        ALGORITHM
      );
      hmacSHA256.init(secretKey);
      byte[] token = hmacSHA256.doFinal(
        concatenatedString.getBytes(StandardCharsets.UTF_8)
      );
      return DatatypeConverter.printHexBinary(token).toLowerCase();
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      logger.error("Error", e);
    }

    return null;
  }

  /**
   * Validates that the supplied token matches the token generated from the given request
   * details.
   *
   * <p>All parameters must be non-blank; otherwise validation fails. The token is
   * recomputed from {@code apiKey}, {@code secret}, {@code date}, {@code httpMethod} and
   * {@code uri} and compared against the provided {@code token}.
   *
   * <p>Note: the recency of {@code date} (not in the future and not more than five minutes
   * in the past) is expected to be validated separately when the request details are
   * extracted; this method only checks the token signature.
   *
   * @param token the token to validate
   * @param apiKey the API key identifying the caller
   * @param secret the shared secret used as the HMAC key
   * @param date the request date included in the signed payload
   * @param httpMethod the HTTP method of the request (e.g. {@code GET}, {@code POST})
   * @param uri the target request URI
   * @return {@code true} if all details are present and the recomputed token equals the
   *     supplied {@code token}; {@code false} otherwise
   */
  public static boolean validateToken(
    String token,
    String apiKey,
    String secret,
    String date,
    String httpMethod,
    String uri
  ) {
    // Validate that date is not a future date & not more than 5 minutes before
    // current date
    // This validation is done at the moment of extracting details from request.
    if (
      StringUtils.isBlank(token) ||
      StringUtils.isBlank(apiKey) ||
      StringUtils.isBlank(secret) ||
      StringUtils.isBlank(date) ||
      StringUtils.isBlank(httpMethod) ||
      StringUtils.isBlank(uri)
    ) {
      logger.error("Token validation details not valid!");
      return false;
    }
    String genToken = generateToken(apiKey, secret, date, httpMethod, uri);
    return token.equals(genToken);
  }
}
