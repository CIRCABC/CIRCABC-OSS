package io.swagger.api;

import eu.europa.ec.digit.circabc.rest.service.app.message.AppMessageDaoService;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationService;
import io.swagger.model.*;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.SystemMessageModel;
import io.swagger.model.db.AppMessageDAO;
import io.swagger.model.db.DistributionEmailDAO;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Default implementation of {@link AppMessageApi}.
 *
 * <p>Provides the business logic backing the CIRCABC application-message (a.k.a. system
 * banner) feature. This covers three related concerns:
 *
 * <ul>
 *   <li>Managing application-message <em>templates</em> stored in the relational database
 *       (create, read, update, delete and paged listing), and exposing the currently
 *       displayable messages to end users.</li>
 *   <li>Managing the legacy ("old") system message that is stored as an Alfresco node
 *       attached to the CIRCABC root, including its display and enablement flags and its
 *       text content.</li>
 *   <li>Managing the distribution e-mail list used to notify subscribers when a message is
 *       broadcast, including subscription checks, export to an Excel workbook and the
 *       notification dispatch itself.</li>
 * </ul>
 *
 * <p>Persistence of templates and distribution e-mails is delegated to
 * {@link AppMessageDaoService}; Alfresco node interactions go through {@link NodeService},
 * {@link PersonService} and {@link CircabcApi}; and outgoing notifications are sent via the
 * injected {@link NotificationService}.
 */
public class AppMessageApiImpl implements AppMessageApi {

  /** DAO service providing persistence for message templates and distribution e-mails. */
  @Autowired
  private AppMessageDaoService appMessageDaoService;

  /** Alfresco service used to resolve person nodes and read their properties (e.g. e-mail). */
  @Autowired
  private PersonService personService;

  /** Alfresco service used to read and write node properties and child associations. */
  @Autowired
  private NodeService nodeService;

  /** CIRCABC API used to obtain the CIRCABC root node reference. */
  @Autowired
  private CircabcApi circabcApi;

  /** Service used to send system-message notifications to distribution e-mail recipients. */
  @Autowired
  @Qualifier("CircabcNotificationService") // NOSONAR
  private NotificationService notificationService;

  /**
   * Returns the application messages that should currently be displayed to end users.
   *
   * <p>All stored templates are retrieved and only those flagged as displayable
   * ({@code showMessage == true}) are converted and returned.
   *
   * @return the list of enabled {@link AppMessage} instances; never {@code null}
   */
  @Override
  public List<AppMessage> getAppMessages() {
    List<AppMessage> result = new ArrayList<>();

    List<AppMessageDAO> allTemplates =
      appMessageDaoService.selectAppMessageTemplates(-1, -1);
    for (AppMessageDAO template : allTemplates) {
      if (Boolean.TRUE.equals(template.getShowMessage())) {
        AppMessage message = convertToAppMessage(template);
        result.add(message);
      }
    }

    return result;
  }

  /**
   * Converts a persisted {@link AppMessageDAO} template into its {@link AppMessage} DTO
   * representation, mapping the closure date (when present) into a {@link DateTime}.
   *
   * @param template the persisted template to convert; must not be {@code null}
   * @return the corresponding {@link AppMessage} DTO
   */
  private AppMessage convertToAppMessage(AppMessageDAO template) {
    AppMessage result = new AppMessage();
    result.setId(template.getId());
    result.setContent(template.getMessageContent());

    if (template.getDateClosure() != null) {
      result.setDateClosure(new DateTime(template.getDateClosure()));
    }

    result.setDisplayTime(template.getDisplayTime());
    result.setEnabled(template.getShowMessage());
    result.setLevel(template.getMessageLevel());
    return result;
  }

  /**
   * Returns a page of application-message templates together with the total count.
   *
   * @param page the zero-based page index to retrieve
   * @param limit the maximum number of templates per page ({@code -1} for no limit)
   * @return a {@link PagedAppMessages} holding the requested page of templates and the
   *     total number of templates available
   */
  @Override
  public PagedAppMessages getAppMessageTemplates(int page, int limit) {
    PagedAppMessages result = new PagedAppMessages();
    List<AppMessageDAO> allTemplates =
      appMessageDaoService.selectAppMessageTemplates(page, limit);
    for (AppMessageDAO template : allTemplates) {
      AppMessage message = convertToAppMessage(template);
      result.getData().add(message);
    }

    Integer total = appMessageDaoService.countAppMessageTemplates();
    result.setTotal(total.longValue());

    return result;
  }

  /**
   * Creates and persists a new application-message template.
   *
   * @param template the template to create; its closure date, when present, is converted
   *     to a {@link Date} before persistence
   */
  @Override
  public void addAppMessageTemplate(AppMessage template) {
    Date closure = null;
    if (template.getDateClosure() != null) {
      closure = template.getDateClosure().toDate();
    }

    appMessageDaoService.addAppMessageTemplate(
      template.getContent(),
      closure,
      template.getLevel(),
      template.getDisplayTime(),
      template.getEnabled()
    );
  }

  /**
   * Updates an existing application-message template, identified by {@code template.getId()}.
   *
   * @param template the template carrying the new values; its closure date, when present,
   *     is converted to a {@link Date} before persistence
   */
  @Override
  public void updateAppMessageTemplate(AppMessage template) {
    Date closure = null;
    if (template.getDateClosure() != null) {
      closure = template.getDateClosure().toDate();
    }

    appMessageDaoService.updateAppMessageTemplate(
      template.getId(),
      template.getContent(),
      closure,
      template.getLevel(),
      template.getDisplayTime(),
      template.getEnabled()
    );
  }

  /**
   * Deletes the application-message template with the given identifier.
   *
   * @param id the identifier of the template to delete
   */
  @Override
  public void deleteAppMessageTemplate(Integer id) {
    appMessageDaoService.deleteAppMessageTemplate(id);
  }

  /**
   * Retrieves a single application-message template by its identifier.
   *
   * @param id the identifier of the template to retrieve
   * @return the corresponding {@link AppMessage}
   */
  @Override
  public AppMessage getAppMessageTemplate(Integer id) {
    return convertToAppMessage(appMessageDaoService.getMessageTemplate(id));
  }

  /**
   * Reads whether the display of the legacy ("old") application message is enabled at the
   * CIRCABC root level.
   *
   * <p>When the underlying property is not set, display defaults to {@code true}.
   *
   * @return a {@link DisplayConfiguration} whose {@code display} flag reflects the stored
   *     configuration
   */
  @Override
  public DisplayConfiguration getDisplayOldMessage() {
    NodeRef circabcRef = circabcApi.getCircabcNodeRef();
    Serializable prop = nodeService.getProperty(
      circabcRef,
      CircabcModel.PROP_DISPLAY_OLD_APP_MESSAGE
    );

    DisplayConfiguration result = new DisplayConfiguration();
    result.setDisplay((prop == null || Boolean.parseBoolean(prop.toString())));

    return result;
  }

  /**
   * Sets whether the display of the legacy ("old") application message is enabled at the
   * CIRCABC root level.
   *
   * @param displayOld {@code true} to display the old message, {@code false} otherwise
   */
  @Override
  public void setDisplayOldMessage(Boolean displayOld) {
    NodeRef circabcRef = circabcApi.getCircabcNodeRef();
    nodeService.setProperty(
      circabcRef,
      CircabcModel.PROP_DISPLAY_OLD_APP_MESSAGE,
      displayOld
    );
  }

  /**
   * Reads whether the legacy ("old") system message node is currently enabled.
   *
   * @return an {@link EnableConfiguration} whose {@code enable} flag reflects the stored
   *     value; {@code false} when no old system message node exists
   */
  @Override
  public EnableConfiguration getEnableOldMessage() {
    NodeRef oldMessageRef = getOldMessageRef();
    EnableConfiguration result = new EnableConfiguration();
    result.setEnable(false);

    if (oldMessageRef != null) {
      result.setEnable(
        (Boolean) nodeService.getProperty(
          oldMessageRef,
          SystemMessageModel.PROP_IS_SYSTEMMESSAGE_ENABLED
        )
      );
    }

    return result;
  }

  /**
   * Enables or disables the legacy ("old") system message node, if such a node exists.
   *
   * @param enableOld {@code true} to enable the old system message, {@code false} to
   *     disable it
   */
  @Override
  public void setEnableOldMessage(Boolean enableOld) {
    NodeRef oldMessageRef = getOldMessageRef();

    if (oldMessageRef != null) {
      nodeService.setProperty(
        oldMessageRef,
        SystemMessageModel.PROP_IS_SYSTEMMESSAGE_ENABLED,
        enableOld
      );
    }
  }

  /**
   * Updates the text content of the legacy ("old") system message node, if such a node
   * exists.
   *
   * @param template the template whose content becomes the new system-message text
   */
  @Override
  public void udpateOldAppMessage(AppMessage template) {
    NodeRef oldMessageRef = getOldMessageRef();

    if (oldMessageRef != null) {
      nodeService.setProperty(
        oldMessageRef,
        SystemMessageModel.PROP_SYSTEMMESSAGE_TEXT,
        template.getContent()
      );
    }
  }

  /**
   * Returns a page of distribution e-mail entries matching an optional search query,
   * together with the total match count.
   *
   * @param page the zero-based page index to retrieve
   * @param limit the maximum number of entries per page ({@code -1} for no limit)
   * @param query an optional filter applied to the e-mail addresses; may be empty
   * @return a {@link PagedEmails} holding the requested page of entries and the total
   *     number of matching entries
   */
  @Override
  public PagedEmails getAppDistributionEmails(
    int page,
    int limit,
    String query
  ) {
    List<DistributionEmailDAO> list =
      appMessageDaoService.selectDistributionEmails(page, limit, query);
    Long total = appMessageDaoService.countDistributionEmails(query);
    PagedEmails result = new PagedEmails();
    result.setData(list);
    result.setTotal(total);
    return result;
  }

  /**
   * Adds the given distribution e-mail entries, skipping any address that is already
   * subscribed.
   *
   * @param list the distribution e-mail entries to add
   */
  @Override
  public void addAppDistributionPostEmails(List<DistributionEmailDAO> list) {
    for (DistributionEmailDAO distribEmail : list) {
      if (
        Boolean.FALSE.equals(
          isSubscribedDistributionEmail(distribEmail.getEmailAddress())
        )
      ) {
        appMessageDaoService.insertEmail(distribEmail);
      }
    }
  }

  /**
   * Indicates whether the given e-mail address is already present in the distribution list.
   *
   * @param query the e-mail address to check; may be {@code null}
   * @return {@code true} if the address is subscribed, {@code false} otherwise (including
   *     when {@code query} is {@code null})
   */
  @Override
  public Boolean isSubscribedDistributionEmail(String query) {
    if (query != null) {
      return appMessageDaoService.hasDistributionEmail(query) > 0;
    }

    return false;
  }

  /**
   * Removes the distribution e-mail entry with the given identifier, if the identifier is
   * not {@code null}.
   *
   * @param id the identifier of the distribution e-mail entry to remove; may be
   *     {@code null}, in which case no action is taken
   */
  @Override
  public void removeAppDistributionPostEmails(Integer id) {
    if (id != null) {
      appMessageDaoService.deleteDistributionEmail(id);
    }
  }

  /**
   * Exports the entire distribution e-mail list as an Excel workbook.
   *
   * <p>Each address is written to a single "email" column. When the number of rows reaches
   * the Excel 2007 per-sheet maximum, a new sheet is started so that arbitrarily large
   * lists can be exported.
   *
   * @return an {@link Workbook} (XSSF format) containing every subscribed e-mail address
   */
  @Override
  public Workbook getdistributionListAsExcel() {
    Workbook workbook = new XSSFWorkbook();
    Sheet s1 = workbook.createSheet();
    Row r1 = s1.createRow(0);
    Cell header = r1.createCell(0);
    header.setCellValue("email");
    int i = 1;

    for (DistributionEmailDAO distrib : getAppDistributionEmails(
      0,
      -1,
      ""
    ).getData()) {
      if (i == SpreadsheetVersion.EXCEL2007.getMaxRows()) {
        s1 = workbook.createSheet();
        r1 = s1.createRow(0);
        header = r1.createCell(0);
        header.setCellValue("email");
        i = 1;
      }

      Row r = s1.createRow(i);
      Cell mail = r.createCell(0);
      mail.setCellValue(distrib.getEmailAddress());
      i++;
    }

    return workbook;
  }

  /**
   * Returns the distribution e-mail entry subscribed for the given user, resolved via the
   * user's Alfresco person e-mail address.
   *
   * @param userId the Alfresco user identifier; when empty, {@code null} is returned
   * @return the matching {@link DistributionEmailDAO}, or {@code null} if the user id is
   *     empty or no entry matches the user's e-mail address
   */
  @Override
  public DistributionEmailDAO getSubscribedDistributionEmail(String userId) {
    if (!"".equals(userId)) {
      NodeRef personRef = personService.getPerson(userId);
      String email = String.valueOf(
        nodeService.getProperty(personRef, ContentModel.PROP_EMAIL)
      );
      return appMessageDaoService.getDistributionEmail(email);
    }

    return null;
  }

  /**
   * Notifies every subscriber in the distribution list about the given message template.
   *
   * <p>The distribution list is traversed page by page (25 entries at a time) and a
   * system-message notification is dispatched for each batch, including a final partial
   * page when the total is not an exact multiple of the page size.
   *
   * @param template the message template to broadcast to all subscribers
   */
  @Override
  public void notifyTemplate(AppMessage template) {
    int page = 0;
    int limit = 25;
    PagedEmails mails;
    List<String> mailAddress = new ArrayList<>();

    do {
      mails = getAppDistributionEmails(page, limit, "");
      for (DistributionEmailDAO distrib : mails.getData()) {
        mailAddress.add(distrib.getEmailAddress());
      }
      notificationService.notifySystemMessage(mailAddress, template);
      page++;
      mailAddress.clear();
    } while (page < mails.getTotal() / limit);
    // last page
    if (mails.getTotal() % limit > 0) {
      mails = getAppDistributionEmails(page, limit, "");
      for (DistributionEmailDAO distrib : mails.getData()) {
        mailAddress.add(distrib.getEmailAddress());
      }
      notificationService.notifySystemMessage(mailAddress, template);
    }
  }

  /**
   * Retrieves a distribution e-mail entry by its numeric identifier.
   *
   * @param idInt the identifier of the distribution e-mail entry
   * @return the matching {@link DistributionEmailDAO}
   */
  @Override
  public DistributionEmailDAO getSubscribedDistributionEmailById(
    Integer idInt
  ) {
    return appMessageDaoService.getDistributionEmailById(idInt);
  }

  /**
   * Locates the legacy ("old") system message node stored as a child of the CIRCABC root.
   *
   * <p>When several system message children exist, the last one encountered is returned.
   *
   * @return the {@link NodeRef} of the old system message node, or {@code null} if none is
   *     present
   */
  private NodeRef getOldMessageRef() {
    NodeRef circabcNodeRef = circabcApi.getCircabcNodeRef();
    List<ChildAssociationRef> listOfMessages = nodeService.getChildAssocs(
      circabcNodeRef,
      ContentModel.ASSOC_CONTAINS,
      QName.createQName(
        SystemMessageModel.CIRCABC_SYSTEMMESSAGE_MODEL_1_0_URI,
        "System Message"
      )
    );

    NodeRef result = null;

    for (ChildAssociationRef childAssoc : listOfMessages) {
      result = childAssoc.getChildRef();
    }

    return result;
  }
}
