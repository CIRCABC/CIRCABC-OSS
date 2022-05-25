package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.profile.permissions.LibraryPermissions;
import eu.cec.digit.circabc.service.profile.permissions.NewsGroupPermissions;
import io.swagger.api.FileAttachmentData;
import io.swagger.api.TopicsApi;
import io.swagger.exception.InvalidTopicException;
import io.swagger.model.Comment;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.PostJsonParser;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopicRepliesPost extends CircabcDeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(TopicRepliesPost.class);

    private TopicsApi topicsApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("id");

        try {

            if (!(this.currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
                    id, NewsGroupPermissions.NWSPOST)
                    || this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
                    id, LibraryPermissions.LIBACCESS))) {
                throw new AccessDeniedException(
                        "Cannot post the reply to the topic, not enough permissions");
            }

            FormData form = (FormData) req.parseContent();

            if (form == null || !form.getIsMultiPart()) {
                throw new IllegalArgumentException("Not a multipart request.");
            }

            if (form.getFields().length == 0) {
                throw new IllegalArgumentException("Wrong number of parameters.");
            }

            Comment body = null;
            List<FileAttachmentData> filesToAdd = new ArrayList<>();
            List<String> linksToAdd = new ArrayList<>();

            for (FormData.FormField field : form.getFields()) {

                if (field.getName().equals("comment")) {
                    String jsonString = Converter.getValue(field);
                    body = PostJsonParser.parsePartial(jsonString);
                } else if (field.getName().equals("filesToAdd") && field.getIsFile()) {
                    InputStream inputStream = field.getInputStream();
                    FileAttachmentData fileAttachmentData =
                            new FileAttachmentData(
                                    field.getFilename(),
                                    field.getContent().getSize(),
                                    inputStream,
                                    field.getContent().getMimetype(),
                                    field.getContent().getEncoding());
                    filesToAdd.add(fileAttachmentData);
                } else if (field.getName().equals("linksToAdd")) {
                    String linkToAdd = field.getContent().getContent();
                    linksToAdd.add(linkToAdd);
                } else {
                    throw new IllegalArgumentException("Unexpected parameter.");
                }
            }

            model.put("post", this.topicsApi.topicsIdRepliesPost(id, body, filesToAdd, linksToAdd));

        } catch (AccessDeniedException ade) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied");
            status.setRedirect(true);
            return null;
        } catch (InvalidNodeRefException | InvalidTopicException | ParseException | IOException inre) {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage("Bad request");
            status.setRedirect(true);
            return null;
        }

        return model;
    }

    /**
     * @return the topicsApi
     */
    public TopicsApi getTopicsApi() {
        return this.topicsApi;
    }

    /**
     * @param topicsApi the topicsApi to set
     */
    public void setTopicsApi(TopicsApi topicsApi) {
        this.topicsApi = topicsApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
