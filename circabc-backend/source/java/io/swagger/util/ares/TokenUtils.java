package io.swagger.util.ares;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class TokenUtils {
    private static final Log logger = LogFactory.getLog(TokenUtils.class);

    private static final String ALGORITHM = "HmacSHA256";

    private TokenUtils() {
    }

    public static String generateToken(
            String apiKey, String secret, String date, String httpMethod, String url) {
        String concatenatedString = apiKey + date + httpMethod + url;
        if (logger.isDebugEnabled()) {
            logger.debug("Concatenated string: '" + concatenatedString + "'");
        }
        Mac hmacSHA256;
        try {
            hmacSHA256 = Mac.getInstance(ALGORITHM);

            SecretKeySpec secretKey =
                    new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            hmacSHA256.init(secretKey);
            byte[] token = hmacSHA256.doFinal(concatenatedString.getBytes(StandardCharsets.UTF_8));
            return DatatypeConverter.printHexBinary(token).toLowerCase();

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Error", e);
        }

        return null;
    }

    public static boolean validateToken(
            String token, String apiKey, String secret, String date, String httpMethod, String uri) {
        // Validate that date is not a future date & not more than 5 minutes before
        // current date
        // This validation is done at the moment of extracting details from request.
        if (StringUtils.isBlank(token)
                || StringUtils.isBlank(apiKey)
                || StringUtils.isBlank(secret)
                || StringUtils.isBlank(date)
                || StringUtils.isBlank(httpMethod)
                || StringUtils.isBlank(uri)) {
            logger.error("Token validation details not valid!");
            return false;
        }

        String genToken = generateToken(apiKey, secret, date, httpMethod, uri);
        if (logger.isDebugEnabled()) {
            logger.debug("Received : " + token);
            logger.debug("Generated: " + genToken);
        }

        return token.equals(genToken);
    }
}
