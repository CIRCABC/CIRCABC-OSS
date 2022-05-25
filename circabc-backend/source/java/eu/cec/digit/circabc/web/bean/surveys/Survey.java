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
package eu.cec.digit.circabc.web.bean.surveys;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Represent an IPM survey
 *
 * @author Matthieu Sprunck
 */
public class Survey implements Serializable {

    /**
     * The serial UID
     */
    private static final long serialVersionUID = 6406690407022902856L;

    private String name;

    private String subject;

    private String status;

    private Date startDate;

    private Date closeDate;

    private List<String> translations;

    private String pivotLang;

    public Survey(String name, String subject, String status, Date startDate,
                  Date endDate, List<String> translations, String pivotLang) {
        super();
        this.name = name;
        this.subject = subject;
        this.status = status;
        this.startDate = startDate;
        this.closeDate = endDate;
        this.translations = translations;
        this.pivotLang = pivotLang;
    }

    public Date getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Date endDate) {
        this.closeDate = endDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPivotLang() {
        return pivotLang;
    }

    public void setPivotLang(String pivotLang) {
        this.pivotLang = pivotLang;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<String> getTranslations() {
        return translations;
    }

    public void setTranslations(List<String> translations) {
        this.translations = translations;
    }
}
