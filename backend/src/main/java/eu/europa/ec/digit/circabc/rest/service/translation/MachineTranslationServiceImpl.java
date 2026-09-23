package eu.europa.ec.digit.circabc.rest.service.translation;

import io.swagger.config.CircabcConfig;
import io.swagger.exception.CircabcRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default {@link MachineTranslationService} implementation that dispatches translation requests to
 * an external machine translation REST endpoint (the eTranslation-style service).
 *
 * <p>It builds a JSON payload from a {@link MachineTranslationRequest} and submits it via an HTTP
 * {@code POST} using HTTP Basic authentication. Two payload shapes are supported:
 *
 * <ul>
 *   <li>plain text translation (when no document path is provided), and
 *   <li>document translation (when a document path is provided), which also specifies an FTP
 *       destination for the translated output.
 * </ul>
 *
 * <p>The service endpoint URL and Basic authentication credentials are resolved from
 * {@link CircabcConfig} during {@link #init()}.
 */
public class MachineTranslationServiceImpl
  implements MachineTranslationService
{

  /** Logger for request payloads, endpoint URL and HTTP response diagnostics. */
  private static final Log logger = LogFactory.getLog(
    MachineTranslationServiceImpl.class
  );

  /** Base URL of the external machine translation REST endpoint. */
  private String url;

  /** Username used for HTTP Basic authentication against the translation service. */
  private String username;

  /** Password used for HTTP Basic authentication against the translation service. */
  private String password;

  /** Configuration source providing the translation service URL and REST credentials. */
  @Autowired
  private CircabcConfig circabcConfig;

  /**
   * Initializes the service by loading the endpoint URL and Basic authentication credentials from
   * {@link CircabcConfig}. Intended to be invoked as a Spring bean init method after dependency
   * injection.
   */
  public void init() {
    url = circabcConfig.getMtServiceUrl();
    username = circabcConfig.getMtRESTUsername();
    password = circabcConfig.getMtRESTPassword();
  }

  /**
   * Builds the JSON request body expected by the machine translation service from the given
   * request.
   *
   * <p>The comma-separated target languages are expanded into a JSON array. When no document path
   * is supplied ({@link MachineTranslationRequest#getDocumentToTranslate()} is empty) a text
   * translation payload is produced using {@code textToTranslate}; otherwise a document translation
   * payload is produced using {@code documentToTranslatePath} together with an FTP destination
   * derived from the target translation path. The translation domain is fixed to {@code "SPD"}.
   *
   * @param req the translation request to serialize; must not be {@code null}
   * @return the serialized JSON request body
   * @throws JSONException if the JSON payload cannot be constructed
   */
  private static String createTranslationRequest(MachineTranslationRequest req)
    throws JSONException {
    String[] targetLanguages = req.getTargetLanguage().split(",");
    JSONArray targetLanguagesJSON = new JSONArray();
    for (int i = 0; i < targetLanguages.length; i++) {
      targetLanguagesJSON.put(i, targetLanguages[i]);
    }
    if (req.getDocumentToTranslate().equals("")) {
      return new JSONObject()
        .put("priority", req.getPriority())
        .put("externalReference", req.getExternalReference())
        .put(
          "callerInformation",
          new JSONObject()
            .put("application", req.getApplicationName())
            .put("username", req.getUsername())
            .put("institution", req.getInstitution())
        )
        .put("textToTranslate", req.getTextToTranslate())
        .put("sourceLanguage", req.getSourceLanguage())
        .put("targetLanguages", targetLanguagesJSON)
        .put("domain", "SPD")
        .put("requesterCallback", req.getRequesterCallback())
        .put("errorCallback", req.getErrorCallback())
        .toString();
    } else {
      return new JSONObject()
        .put("priority", req.getPriority())
        .put("externalReference", req.getExternalReference())
        .put(
          "callerInformation",
          new JSONObject()
            .put("application", req.getApplicationName())
            .put("username", req.getUsername())
            .put("institution", req.getInstitution())
        )
        .put("documentToTranslatePath", req.getDocumentToTranslate())
        .put("sourceLanguage", req.getSourceLanguage())
        .put("targetLanguages", targetLanguagesJSON)
        .put("domain", "SPD")
        .put(
          "destinations",
          new JSONObject().put(
            "ftpDestinations",
            new JSONArray().put(0, req.getTargetTranslationPath())
          )
        )
        .put("requesterCallback", req.getRequesterCallback())
        .put("errorCallback", req.getErrorCallback())
        .toString();
    }
  }

  /**
   * Submits the given translation request to the external machine translation service.
   *
   * <p>Builds an HTTP {@code POST} carrying the JSON payload produced by
   * {@link #createTranslationRequest(MachineTranslationRequest)}, using HTTP Basic authentication
   * with the configured credentials. Automatic redirect following is disabled and the response
   * status is logged. Any failure during payload construction or transport is wrapped and
   * rethrown.
   *
   * @param request the translation request to send; must not be {@code null}
   * @throws CircabcRuntimeException if an error occurs while calling the translation web service
   */
  @Override
  public void sendMessage(MachineTranslationRequest request) {
    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(
      AuthScope.ANY,
      new UsernamePasswordCredentials(this.username, this.password)
    );

    try (
      CloseableHttpClient client = HttpClients.custom()
        .setDefaultCredentialsProvider(credentialsProvider)
        .build();
    ) {
      RequestConfig requestConfig = RequestConfig.custom()
        .setRedirectsEnabled(false)
        .build();
      HttpPost post = new HttpPost(this.url);
      post.setConfig(requestConfig);
      post.setHeader("Accept", "application/json");
      post.setHeader("Content-type", "application/json");
      String json = createTranslationRequest(request);
      if (logger.isInfoEnabled()) {
        logger.info("json:" + json);
        logger.info("url" + this.url);
      }
      post.setEntity(new StringEntity(json, "UTF-8"));
      HttpResponse response = client.execute(post);
      if (logger.isInfoEnabled()) {
        logger.info(
          "response status line " + response.getStatusLine().toString()
        );
        logger.info(
          "response status code " + response.getStatusLine().getStatusCode()
        );
      }
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error when sending message to translation service", e);
      }
      throw new CircabcRuntimeException(
        "Error when call machine translation web service"
      );
    }
  }
}
