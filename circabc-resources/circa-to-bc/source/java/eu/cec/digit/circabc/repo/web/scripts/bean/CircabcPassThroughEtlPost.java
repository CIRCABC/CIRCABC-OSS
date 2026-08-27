package eu.cec.digit.circabc.repo.web.scripts.bean;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import eu.cec.digit.circabc.service.migration.ETLException;
import eu.cec.digit.circabc.service.migration.ETLService;

/**
 * REST API webscript to trigger pass-through ETL (skip user transformation).
 * POST /service/api/circabc/etl/passthrough
 * Body: { "iterationName": "..." }
 */
public class CircabcPassThroughEtlPost extends CircabcDeclarativeWebScript {

    private static final Log logger = LogFactory.getLog(CircabcPassThroughEtlPost.class);

    private ETLService etlService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        final Map<String, Object> model = new HashMap<>();

        try {
            final JSONObject body = (JSONObject) org.json.simple.JSONValue.parse(req.getContent().getContent());
            final String iterationName = (String) body.get("iterationName");

            if (iterationName == null || iterationName.isEmpty()) {
                status.setCode(Status.STATUS_BAD_REQUEST);
                model.put("message", "Missing required field: iterationName");
                return model;
            }

            etlService.passThroughEtl(iterationName);

            model.put("message", "Pass-through ETL completed. Iteration ready for import.");
            model.put("iterationName", iterationName);

        } catch (ETLException e) {
            logger.error("Pass-through ETL failed", e);
            status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
            model.put("message", "Pass-through ETL failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during pass-through ETL", e);
            status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
            model.put("message", "Unexpected error: " + e.getMessage());
        }

        return model;
    }

    public void setEtlService(ETLService etlService) {
        this.etlService = etlService;
    }
}
