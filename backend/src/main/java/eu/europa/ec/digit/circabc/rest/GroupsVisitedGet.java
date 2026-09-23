package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.GroupsApi;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Web Script endpoint that serves an HTTP {@code GET} request returning the interest
 * groups most recently visited by the current user.
 *
 * <p>The endpoint reads an optional {@code amount} query parameter that limits the number of
 * visited groups to return. When the parameter is absent, empty or not strictly positive, no limit
 * is applied ({@code amount} defaults to {@code 0}). A non-numeric value results in an
 * {@link IllegalArgumentException}.
 *
 * <p>The lookup is delegated to {@link io.swagger.api.GroupsApi#getVisitedGroups(int)} and the
 * resulting collection is exposed to the response template under the {@code groups} model key.
 * Multilingual (ML) property awareness is temporarily disabled while the groups are resolved and
 * restored afterwards. Access and node-resolution failures are translated into the appropriate HTTP
 * status codes ({@code 403 Forbidden} and {@code 400 Bad Request} respectively).
 */
public class GroupsVisitedGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsVisitedGet.class);

  /**
   * API used to resolve the interest groups recently visited by the current user.
   */
  @Autowired
  private GroupsApi groupsApi;

  /**
   * Handles the {@code GET} request by resolving the current user's most recently visited groups.
   *
   * <p>Parses the optional {@code amount} query parameter to cap the number of returned groups,
   * disables ML property awareness during the lookup, and populates the response model with the
   * resolved groups under the {@code groups} key.
   *
   * @param req the incoming web script request; the optional {@code amount} query parameter caps
   *     the number of groups returned
   * @param status the response status, updated to {@code 403} on access denial or {@code 400} on an
   *     invalid node reference
   * @param cache the cache directives for the response
   * @return a model map containing the {@code groups} entry, or {@code null} when an error status
   *     with redirect has been set
   * @throws IllegalArgumentException if the {@code amount} parameter is present but not a valid
   *     integer
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    Map<String, Object> model = new HashMap<>(7, 1.0f);

    String amountString = req.getParameter("amount");
    int amount = 0;
    try {
      amount = (((amountString == null) || amountString.isEmpty())
        ? 0
        : Integer.parseInt(amountString));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
        "Wrong numeric value for 'amount': " + amount,
        e
      );
    }

    if (amount < 1) {
      amount = 0;
    }

    try {
      MLPropertyInterceptor.setMLAware(false);

      model.put("groups", this.groupsApi.getVisitedGroups(amount));
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
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
