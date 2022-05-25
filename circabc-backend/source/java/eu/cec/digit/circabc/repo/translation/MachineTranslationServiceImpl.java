package eu.cec.digit.circabc.repo.translation;

import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.error.CircabcRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MachineTranslationServiceImpl implements MachineTranslationService {

    private static final Log logger = LogFactory.getLog(MachineTranslationServiceImpl.class);
    private String url;
    private String username;
    private String password;

    public MachineTranslationServiceImpl() {
        url = CircabcConfiguration.getProperty(CircabcConfiguration.MT_REST_URL);
        username = CircabcConfiguration.getProperty(CircabcConfiguration.MT_REST_USERNAME);
        password = CircabcConfiguration.getProperty(CircabcConfiguration.MT_REST_PASSWORD);
    }

    private static String createTranslationRequest(
            String applicationName,
            String departmentNumber,
            String documentToTranslate,
            String domains,
            String errorCallback,
            String externalReference,
            String institution,
            String originalFileName,
            String outputFormat,
            int priority,
            String requesterCallback,
            String requestType,
            String sourceLanguage,
            String targetLanguage,
            String targetTranslationPath,
            String textToTranslate,
            String username)
            throws JSONException {

        String[] targetLanguages = targetLanguage.split(",");
        JSONArray targetLanguagesJSON = new JSONArray();
        for (int i = 0; i < targetLanguages.length; i++) {
            targetLanguagesJSON.put(i, targetLanguages[i]);
        }
        if (documentToTranslate.equals("")) {
            return new JSONObject()
                    .put("priority", priority)
                    .put("externalReference", externalReference)
                    .put(
                            "callerInformation",
                            new JSONObject()
                                    .put("application", applicationName)
                                    .put("username", username)
                                    .put("institution", institution))
                    .put("textToTranslate", textToTranslate)
                    .put("sourceLanguage", sourceLanguage)
                    .put("targetLanguages", targetLanguagesJSON)
                    .put("domain", "SPD")
                    .put("requesterCallback", requesterCallback)
                    .put("errorCallback", errorCallback)
                    .toString();
        } else {
            return new JSONObject()
                    .put("priority", priority)
                    .put("externalReference", externalReference)
                    .put(
                            "callerInformation",
                            new JSONObject()
                                    .put("application", applicationName)
                                    .put("username", username)
                                    .put("institution", institution))
                    .put("documentToTranslatePath", documentToTranslate)
                    .put("sourceLanguage", sourceLanguage)
                    .put("targetLanguages", targetLanguagesJSON)
                    .put("domain", "SPD")
                    .put(
                            "destinations",
                            new JSONObject()
                                    .put("ftpDestinations", new JSONArray().put(0, targetTranslationPath)))
                    .put("requesterCallback", requesterCallback)
                    .put("errorCallback", errorCallback)
                    .toString();
        }
    }

    @Override
    public void sendMessage(
            String applicationName,
            String departmentNumber,
            String documentToTranslate,
            String domains,
            String errorCallback,
            String externalReference,
            String institution,
            String originalFileName,
            String outputFormat,
            int priority,
            String requesterCallback,
            String requestType,
            String sourceLanguage,
            String targetLanguage,
            String targetTranslationPath,
            String textToTranslate,
            String username) {

        try {
            DefaultHttpClient client = new DefaultHttpClient();
            client
                    .getCredentialsProvider()
                    .setCredentials(
                            AuthScope.ANY, new UsernamePasswordCredentials(this.username, this.password));
            HttpPost post = new HttpPost(this.url);
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-type", "application/json");
            String json =
                    createTranslationRequest(
                            applicationName,
                            departmentNumber,
                            documentToTranslate,
                            domains,
                            errorCallback,
                            externalReference,
                            institution,
                            originalFileName,
                            outputFormat,
                            priority,
                            requesterCallback,
                            requestType,
                            sourceLanguage,
                            targetLanguage,
                            targetTranslationPath,
                            textToTranslate,
                            username);
            if (logger.isInfoEnabled()) {
                logger.info("json:" + json);
                logger.info("url" + this.url);
            }
            post.setEntity(new StringEntity(json, "UTF-8"));
            HttpClientParams.setRedirecting(post.getParams(), false);
            HttpResponse response = client.execute(post);
            if (logger.isInfoEnabled()) {
                logger.info("response status line " + response.getStatusLine().toString());
                logger.info("response status code " + response.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when sending message to translation service", e);
            }
            throw new CircabcRuntimeException("Error when call machine translation web service");
        }
    }
}
