package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.GroupsApi;
import io.swagger.model.GroupConfiguration;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.InterestGroupJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco declarative web script that handles the HTTP {@code PUT} request used to update the
 * configuration of an Interest Group.
 *
 * <p>The endpoint expects the target Interest Group identifier as the {@code id} template variable
 * in the URL and an optional {@code language} request parameter. When {@code language} is provided,
 * the content and UI locales are set accordingly and multilingual (ML) awareness is disabled so that
 * the values for the requested language are persisted; otherwise the request is processed in an
 * ML-aware fashion. The updated {@link GroupConfiguration} payload is parsed from the request body.
 *
 * <p>Only a group administrator of the target Interest Group is authorized to perform the update;
 * unauthorized callers receive an HTTP {@code 403 Forbidden} response. Malformed input results in an
 * HTTP {@code 400 Bad Request} response.
 *
 * <p>On success the response model exposes the persisted configuration under the {@code configuration}
 * key.
 */
public class GroupConfigurationPut extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupConfigurationPut.class);

  /** API providing the Interest Group business operations, including configuration updates. */
  @Autowired
  private GroupsApi groupsApi;

  /** Service used to verify that the current authenticated user is an administrator of the group. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the update of an Interest Group configuration.
   *
   * <p>Reads the group identifier from the {@code id} URL template variable and, optionally, the
   * {@code language} request parameter. It verifies that the current user is a group administrator,
   * parses the {@link GroupConfiguration} from the request body and delegates the update to
   * {@link GroupsApi#putInterestGroupConfiguration(String, GroupConfiguration)}. The multilingual
   * awareness flag is always restored to its original value before returning.
   *
   * @param req the web script request carrying the {@code id} template variable, the optional
   *     {@code language} parameter and the JSON body describing the configuration
   * @param status the response status, set to {@code 403} on access denial or {@code 400} on invalid
   *     input
   * @param cache the response cache directives
   * @return a model map containing the updated configuration under the {@code configuration} key, or
   *     {@code null} when the request is rejected (forbidden or bad request)
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String groupIp = templateVars.get("id");

    String language = req.getParameter("language");
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    if (language == null) {
      MLPropertyInterceptor.setMLAware(true);
    } else {
      Locale locale = Locale.of(language);
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);
    }
    try {
      if (groupIp != null) {
        if (!this.currentUserPermissionCheckerService.isGroupAdmin(groupIp)) {
          throw new AccessDeniedException(
            "Current Authority cannot update the group configuration, not enough permission"
          );
        }

        GroupConfiguration body =
          InterestGroupJsonParser.parseGroupConfiguration(req);
        model.put(
          "configuration",
          this.groupsApi.putInterestGroupConfiguration(groupIp, body)
        );
      }
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException | ParseException | IOException inre) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
