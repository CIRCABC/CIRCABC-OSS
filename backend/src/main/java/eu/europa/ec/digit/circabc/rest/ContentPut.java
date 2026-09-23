package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import eu.europa.ec.digit.circabc.rest.service.mail.MailTemplate;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationService;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationSubscriptionService;
import io.swagger.api.ContentApi;
import io.swagger.api.NodesApi;
import io.swagger.model.Node;
import io.swagger.model.NotifiableUser;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.NodeJsonParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Declarative Web Script handling the HTTP {@code PUT} request that
 * updates the metadata/properties of an existing content node in the CIRCABC
 * repository.
 *
 * <p>The endpoint is addressed by the content node identifier supplied in the
 * URL template variable {@code id}. The request body is a JSON representation
 * of the {@link io.swagger.model.Node} carrying the properties to update, which
 * is parsed by {@link NodeJsonParser#parseContentJSON} and applied through
 * {@link ContentApi#contentIdPut}.</p>
 *
 * <p>Behaviour summary:</p>
 * <ul>
 *   <li>Optional {@code language} request parameter controls multilingual
 *       (ML) handling: when absent the property interceptor is left ML aware,
 *       when present the given {@link Locale} is set as the content/UI locale
 *       and ML awareness is disabled so the update targets that language.</li>
 *   <li>Optional {@code notify} request parameter (defaults to {@code true})
 *       controls whether subscribed users are notified of the edit via
 *       {@link NotificationService#notifyNewFiles} using the
 *       {@link MailTemplate#NOTIFY_EDIT_BULK} template.</li>
 *   <li>The caller must hold {@code LIBMANAGEOWN} or {@code LIBEDITONLY}
 *       library permission on the node; when the node is a working copy the
 *       caller must additionally be the working copy owner.</li>
 * </ul>
 *
 * <p>On success the model exposes the refreshed node under the key
 * {@code node}. On failure the appropriate HTTP status (403, 400 or 409) is
 * set and a {@code null} model is returned.</p>
 */
public class ContentPut extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(ContentPut.class);

  /** API used to apply the property update to the content node. */
  @Autowired
  private ContentApi contentApi;

  /** API used to read back the updated node for the response model. */
  @Autowired
  private NodesApi nodesApi;

  /** Service used to verify the current user's library permissions and working-copy ownership. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** Alfresco node service used to inspect node aspects (e.g. working copy). */
  @Autowired
  private NodeService nodeService;

  /** Service used to send edit notifications to subscribed users. */
  @Autowired
  @Qualifier("CircabcNotificationService") // NOSONAR
  private NotificationService notificationService;

  /** Service used to resolve the set of users subscribed to notifications for the node. */
  @Autowired
  private NotificationSubscriptionService notificationSubscriptionService;

  /**
   * Executes the content update.
   *
   * <p>Resolves the target node from the {@code id} URL template variable,
   * validates the current user's permissions, parses the JSON request body
   * into a {@link Node}, applies the update and optionally notifies subscribed
   * users. The multilingual awareness of the property interceptor is toggled
   * based on the {@code language} parameter and always restored in the
   * {@code finally} block.</p>
   *
   * @param req the web script request; provides the {@code id} template
   *            variable, the optional {@code language} and {@code notify}
   *            parameters and the JSON body describing the node update
   * @param status the response status, set to {@code 403}, {@code 400} or
   *               {@code 409} when the update cannot be completed
   * @param cache the response cache control (not used)
   * @return a model map containing the updated node under the key
   *         {@code node}, or {@code null} when an error status has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
    boolean mlAware = MLPropertyInterceptor.isMLAware();
    String language = req.getParameter("language");
    if (language == null) {
      MLPropertyInterceptor.setMLAware(true);
    } else {
      Locale locale = Locale.of(language);
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);
    }

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("id");

    try {
      boolean notify = true;
      String notifyString = req.getParameter("notify");
      if (notifyString != null) {
        notify = Boolean.parseBoolean(notifyString);
      }
      if (
        !this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
          id,
          LibraryPermissions.LIBMANAGEOWN,
          LibraryPermissions.LIBEDITONLY
        )
      ) {
        throw new AccessDeniedException(
          "Cannot update content, not enough permissions"
        );
      }
      NodeRef nodeRef = Converter.createNodeRefFromId(id);
      if (
        this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY) &&
        !this.currentUserPermissionCheckerService.isWorkingCopyOwner(
          nodeRef.getId()
        )
      ) {
        throw new AccessDeniedException(
          "Cannot update content, not enough permissions"
        );
      }

      Node body = NodeJsonParser.parseContentJSON(
        req.getContent().getContent()
      );

      this.contentApi.contentIdPut(id, body);

      if (notify) {
        Set<NotifiableUser> users =
          notificationSubscriptionService.getNotifiableUsers(nodeRef);
        final List<NodeRef> nodeRefs = new ArrayList<>();

        nodeRefs.add(nodeRef);

        notificationService.notifyNewFiles(
          nodeRef,
          nodeRefs,
          users,
          MailTemplate.NOTIFY_EDIT_BULK
        );
      }

      model.put("node", this.nodesApi.getNodeById(id));
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
    } catch (DuplicateChildNodeNameException dcnne) {
      logger.error(ERROR_OCCURRED, dcnne);
      status.setCode(Status.STATUS_CONFLICT);
      status.setMessage("Duplicate node name");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidTypeException ite) {
      logger.error(ERROR_OCCURRED, ite);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad noderef type");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (IOException | ParseException e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad body");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }
}
