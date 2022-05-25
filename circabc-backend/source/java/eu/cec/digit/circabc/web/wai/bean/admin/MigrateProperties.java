/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.web.wai.bean.admin;

import au.com.bytecode.opencsv.CSVReader;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.repo.struct.SimplePath;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.wai.bean.content.AddContentBean;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.PropertyMap;
import org.alfresco.web.bean.repository.Node;
import org.springframework.extensions.surf.util.I18NUtil;

import javax.faces.context.FacesContext;
import javax.jcr.PathNotFoundException;
import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Dirty emergency migration helper. This bean need an interest group and a simple circa upload file
 * to update properties in the repository.
 *
 * @author Pignot Yanick
 */
public class MigrateProperties extends AddContentBean {

    private static final long serialVersionUID = 8518953203603149020L;

    private static final ThreadLocal<DateFormat> _DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("dd/MM/yyyy");
        }
    };
    private static final String[] EXPECTED_COL_TITLES = new String[]{
            "URN", "LANGUAGE", "VERSION", "LINGUISTIC", "FILENAME", "OVERWRITE", "ENTRYTYPE", "TITLE",
            "KEYWORDS", "AUTHOR", "EXPIRATION", "RANKING", "ABSTRACT", "ISSUEDATE", "REFERENCE", "STATUS"
    };
    private String interestGroupPath;

    public static Date convert(String dateAsString) {
        if (dateAsString == null || dateAsString.length() < 1) {
            return null;
        }

        try {
            return _DATE_FORMAT.get().parse(dateAsString);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void init(final Map<String, String> parameters) {
        super.init(parameters);
    }

    @Override
    protected String finishImpl(final FacesContext context, String outcome) throws Exception {

        SimplePath igPath;
        try {
            igPath = getManagementService().getNodePath(safeString(getInterestGroupPath()));
        } catch (PathNotFoundException e) {
            throw new IllegalArgumentException(e);
        }

        if (!NavigableNodeType.IG_ROOT.isNodeFromType(new Node(igPath.getNodeRef()))) {
            throw new IllegalArgumentException("The path doens't point to an interest group");
        }

        final File file = getFirstUploadedFile().getFile();
        final CSVReader reader = new CSVReader(new FileReader(file), '\t');

        // line of titles
        String[] nextLine = reader.readNext();

        if (nextLine == null) {
            throw new IllegalArgumentException("The file is empty");
        }
        if (EXPECTED_COL_TITLES.length != nextLine.length) {
            throw new IllegalArgumentException(
                    "Invalid number of column. Expected: " + EXPECTED_COL_TITLES.length);
        }
        for (int x = 0; x < EXPECTED_COL_TITLES.length; ++x) {
            if (!EXPECTED_COL_TITLES[x].equals(nextLine[x])) {
                throw new IllegalArgumentException(
                        "Invalid column name at position " + (x + 1) + "Expected: " + EXPECTED_COL_TITLES[x]);
            }
        }

        PropertyMap properties = null;

        String documentPath = null;

        while ((nextLine = reader.readNext()) != null) {

            properties = new PropertyMap();
            documentPath = null;

            //col 0: URN

            //col 1: LANGUAGE
            properties.put(ContentModel.PROP_LOCALE, I18NUtil.parseLocale(safeString(nextLine[1])));

            //col 2: VERSION
            if (nextLine[2].length() > 0) {
                properties.put(VersionModel.PROP_QNAME_VERSION_LABEL, safeString(nextLine[2]));
            }

            //col 3: LINGUISTIC

            //col 4: FILENAME
            documentPath = igPath.toString() + "/Library/" + safeString(nextLine[4]);

            //col 5: OVERWRITE

            //col 6: ENTRYTYPE

            //col 7: TITLE
            properties.put(ContentModel.PROP_TITLE, safeString(nextLine[7]));

            //col 8: KEYWORDS

            //col 9: AUTHOR
            properties.put(ContentModel.PROP_AUTHOR, safeString(nextLine[9]));

            //col 10: EXPIRATION
            properties.put(DocumentModel.PROP_EXPIRATION_DATE, convert(nextLine[10]));

            //col 11: RANKING
            properties.put(DocumentModel.PROP_SECURITY_RANKING, safeString(nextLine[11]));

            //col 12: ABSTRACT
            properties.put(ContentModel.PROP_DESCRIPTION, safeString(nextLine[12]));

            //col 13: ISSUEDATE
            properties.put(DocumentModel.PROP_ISSUE_DATE, convert(nextLine[13]));

            //col 14: REFERENCE
            properties.put(DocumentModel.PROP_REFERENCE, safeString(nextLine[14]));

            //col 15: STATUS
            properties.put(DocumentModel.PROP_STATUS, safeString(nextLine[15]));

            try {
                // get the noderef of the targeted document
                NodeRef document = getManagementService().getNodePath(documentPath).getNodeRef();
                // set the properties
                getNodeService().addProperties(document, properties);
            } catch (PathNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        }

        init(null);

        return outcome;
    }

    private String safeString(String str) {
        return str == null ? "" : str.trim();
    }

    @Override
    protected String doPostCommitProcessing(final FacesContext context, final String outcome) {
        clearUpload();
        this.parameters = null;

        return "dialog:close";
    }

    @Override
    public String cancel() {
        init(null);

        return super.cancel();

    }

    /**
     * @return the interestGroupPath
     */
    public final String getInterestGroupPath() {
        if (interestGroupPath == null) {
            try {
                interestGroupPath = getManagementService().getRootSimplePath().toString() + "/?/?";
            } catch (Exception ignore) {
                // should not but wihtout side effect.
            }
        }

        return interestGroupPath;
    }

    /**
     * @param interestGroupPath the interestGroupPath to set
     */
    public final void setInterestGroupPath(String interestGroupPath) {
        this.interestGroupPath = interestGroupPath;
    }

}
