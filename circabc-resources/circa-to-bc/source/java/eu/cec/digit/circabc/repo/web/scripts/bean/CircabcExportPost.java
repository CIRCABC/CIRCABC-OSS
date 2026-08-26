package eu.cec.digit.circabc.repo.web.scripts.bean;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.service.migration.CategoryInterestGroupPair;
import eu.cec.digit.circabc.service.migration.ExportService;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * REST API webscript to trigger export of a CIRCABC Interest Group.
 * POST /service/api/circabc/export
 * Body: { "category": "catName", "interestGroup": "igName", "iterationName": "iter1" }
 */
public class CircabcExportPost extends CircabcDeclarativeWebScript {

    private static final Log logger = LogFactory.getLog(CircabcExportPost.class);

    private ExportService circabcExportService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        final Map<String, Object> model = new HashMap<>();

        try {
            final JSONObject body = (JSONObject) org.json.simple.JSONValue.parse(req.getContent().getContent());
            final String category = (String) body.get("category");
            final String interestGroup = (String) body.get("interestGroup");
            final String iterationName = (String) body.get("iterationName");
            final String iterationDescription = (String) body.getOrDefault("iterationDescription", "CIRCABC IG Export");

            if (category == null || interestGroup == null || iterationName == null) {
                status.setCode(Status.STATUS_BAD_REQUEST);
                model.put("message", "Missing required fields: category, interestGroup, iterationName");
                return model;
            }

            final CategoryInterestGroupPair pair = new CategoryInterestGroupPair(category, interestGroup);
            final MigrationTracer<ImportRoot> tracer = circabcExportService.runExport(
                    pair, iterationName, iterationDescription);

            model.put("message", "Export completed successfully");
            model.put("iterationName", iterationName);
            model.put("implementationName", circabcExportService.getImplementationName());
            if (tracer.getMarshalledFile() != null) {
                model.put("exportFileRef", tracer.getMarshalledFile().toString());
            }

        } catch (ExportationException e) {
            logger.error("Export failed", e);
            status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
            model.put("message", "Export failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during export", e);
            status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
            model.put("message", "Unexpected error: " + e.getMessage());
        }

        return model;
    }

    public void setCircabcExportService(ExportService circabcExportService) {
        this.circabcExportService = circabcExportService;
    }
}
