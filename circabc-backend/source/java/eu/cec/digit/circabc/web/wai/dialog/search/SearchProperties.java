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
package eu.cec.digit.circabc.web.wai.dialog.search;

import eu.cec.digit.circabc.service.keyword.Keyword;

import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Extension of the Alfresco Search Properties bean that add Circabc advanced search specif fiedls.
 *
 * @author Yanick Pignot
 */
public class SearchProperties extends org.alfresco.web.bean.search.SearchProperties {

    public static final String BEAN_NAME = "CircabcSearchProperties";
    public static final String ANY_VALUE = "any";
    public static final String ANY_MESSAGE_KEY = "advanced_search_dialog_any";
    /**
     * lookin from the current interest group select option
     */
    public static final String LOOKIN_INTEREST_GROUP = "interestGroup";
    /**
     * lookin from the current interest group service (i.e. libarry / neywsgroup / ... ) select
     * option
     */
    public static final String LOOKIN_CURRENT_SERVICE = "service";
    /**
     * lookin from the current location select option
     */
    public static final String LOOKIN_CURRENT_LOCATION = "currentLocation";
    /**
     * Search ml and not ml documents
     */
    public static final String LANGUAGE_ALL = "allLanguage";
    /**
     * Search ml documents in the current user defined language (content filter language)
     */
    public static final String LANGUAGE_CURRENT = "currentLanguage";
    /**
     * Search ml documents in a given language
     */
    public static final String LANGUAGE_SPECIFY = "specifyLanguage";
    /**
     * perform the search only in the current version
     */
    public static final String DEEP_OPTION_LATEST_VERSION = "lastVersion";
    /**
     * perform the search in all version
     */
    public static final String DEEP_OPTION_ALL_VERSIONS = "allVersion";
    private static final long serialVersionUID = -3160066413815928080L;
    /**
     * The list of security rankings
     */
    private List<SelectItem> securityRankings = null;
    /**
     * The list of statuses
     */
    private List<SelectItem> statuses = null;
    /**
     * The list of languages
     */
    private List<SelectItem> languages = null;

    /**
     * The keywords search field
     */
    private List<Keyword> keywords = null;
    /**
     * The status search field
     */
    private String status = null;
    /**
     * The reference search field
     */
    private String reference = null;
    /**
     * The security ranking search field
     */
    private String securityRanking = null;
    /**
     * Check if the search is performed on the issue date
     */
    private boolean issueDateChecked = false;
    /**
     * Check if the search is performed on the expiration date
     */
    private boolean expirationDateChecked = false;
    /**
     * The from issue date search field
     */
    private Date issueDateFrom = null;
    /**
     * The to issue date search field
     */
    private Date issueDateTo = null;
    /**
     * The from expiration date search field
     */
    private Date expirationDateFrom = null;
    /**
     * The to expiration date search field
     */
    private Date expirationDateTo = null;
    /**
     * The url search filed
     */
    private String url = null;
    /**
     * The url search filed
     */
    private String language = null;
    /**
     * The language option
     */
    private String selectedLanguageOption = null;
    /**
     * The deep option
     */
    private String selectedDeepOption = null;

    /**
     * The dynamic properties search fields
     */
    private Serializable dynamicProperty1 = null;

    private Serializable dynamicProperty2 = null;

    private Serializable dynamicProperty3 = null;

    private Serializable dynamicProperty4 = null;

    private Serializable dynamicProperty5 = null;

    private Serializable dynamicProperty6 = null;

    private Serializable dynamicProperty7 = null;

    private Serializable dynamicProperty8 = null;

    private Serializable dynamicProperty9 = null;

    private Serializable dynamicProperty10 = null;


    private Serializable dynamicProperty11 = null;

    private Serializable dynamicProperty12 = null;

    private Serializable dynamicProperty13 = null;

    private Serializable dynamicProperty14 = null;

    private Serializable dynamicProperty15 = null;

    private Serializable dynamicProperty16 = null;

    private Serializable dynamicProperty17 = null;

    private Serializable dynamicProperty18 = null;

    private Serializable dynamicProperty19 = null;

    private Serializable dynamicProperty20 = null;


    /**
     * @return the dynamicProperty1
     */
    public Serializable getDynamicProperty1() {
        return dynamicProperty1;
    }

    /**
     * @param dynamicProperty1 the dynamicProperty1 to set
     */
    public void setDynamicProperty1(Serializable dynamicProperty1) {
        this.dynamicProperty1 = dynamicProperty1;
    }

    /**
     * @return the dynamicProperty2
     */
    public Serializable getDynamicProperty2() {
        return dynamicProperty2;
    }

    /**
     * @param dynamicProperty2 the dynamicProperty2 to set
     */
    public void setDynamicProperty2(Serializable dynamicProperty2) {
        this.dynamicProperty2 = dynamicProperty2;
    }

    /**
     * @return the dynamicProperty3
     */
    public Serializable getDynamicProperty3() {
        return dynamicProperty3;
    }

    /**
     * @param dynamicProperty3 the dynamicProperty3 to set
     */
    public void setDynamicProperty3(Serializable dynamicProperty3) {
        this.dynamicProperty3 = dynamicProperty3;
    }

    /**
     * @return the dynamicProperty4
     */
    public Serializable getDynamicProperty4() {
        return dynamicProperty4;
    }

    /**
     * @param dynamicProperty4 the dynamicProperty4 to set
     */
    public void setDynamicProperty4(Serializable dynamicProperty4) {
        this.dynamicProperty4 = dynamicProperty4;
    }

    /**
     * @return the dynamicProperty5
     */
    public Serializable getDynamicProperty5() {
        return dynamicProperty5;
    }

    /**
     * @param dynamicProperty5 the dynamicProperty5 to set
     */
    public void setDynamicProperty5(Serializable dynamicProperty5) {
        this.dynamicProperty5 = dynamicProperty5;
    }

    /**
     * @return the expirationDateChecked
     */
    public boolean isExpirationDateChecked() {
        return expirationDateChecked;
    }

    /**
     * @param expirationDateChecked the expirationDateChecked to set
     */
    public void setExpirationDateChecked(boolean expirationDateChecked) {
        this.expirationDateChecked = expirationDateChecked;
    }

    /**
     * @return the expirationDateFrom
     */
    public Date getExpirationDateFrom() {
        return expirationDateFrom;
    }

    /**
     * @param expirationDateFrom the expirationDateFrom to set
     */
    public void setExpirationDateFrom(Date expirationDateFrom) {
        this.expirationDateFrom = expirationDateFrom;
    }

    /**
     * @return the expirationDateTo
     */
    public Date getExpirationDateTo() {
        return expirationDateTo;
    }

    /**
     * @param expirationDateTo the expirationDateTo to set
     */
    public void setExpirationDateTo(Date expirationDateTo) {
        this.expirationDateTo = expirationDateTo;
    }

    /**
     * @return the issueDateChecked
     */
    public boolean isIssueDateChecked() {
        return issueDateChecked;
    }

    /**
     * @param issueDateChecked the issueDateChecked to set
     */
    public void setIssueDateChecked(boolean issueDateChecked) {
        this.issueDateChecked = issueDateChecked;
    }

    /**
     * @return the issueDateFrom
     */
    public Date getIssueDateFrom() {
        return issueDateFrom;
    }

    /**
     * @param issueDateFrom the issueDateFrom to set
     */
    public void setIssueDateFrom(Date issueDateFrom) {
        this.issueDateFrom = issueDateFrom;
    }

    /**
     * @return the issueDateTo
     */
    public Date getIssueDateTo() {
        return issueDateTo;
    }

    /**
     * @param issueDateTo the issueDateTo to set
     */
    public void setIssueDateTo(Date issueDateTo) {
        this.issueDateTo = issueDateTo;
    }

    /**
     * @return the keywords
     */
    public List<Keyword> getKeywords() {
        return keywords;
    }

    /**
     * @param keywords the keywords to set
     */
    public void setKeywords(List<Keyword> keywords) {
        this.keywords = keywords;
    }

    /**
     * @return the reference
     */
    public String getReference() {
        return reference;
    }

    /**
     * @param reference the reference to set
     */
    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * @return the securityRanking
     */
    public String getSecurityRanking() {
        return securityRanking;
    }

    /**
     * @param securityRanking the securityRanking to set
     */
    public void setSecurityRanking(String securityRanking) {
        this.securityRanking = securityRanking;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the securityRankings
     */
    public List<SelectItem> getSecurityRankings() {
        return securityRankings;
    }

    /**
     * @param securityRankings the securityRankings to set
     */
    public void setSecurityRankings(List<SelectItem> securityRankings) {
        this.securityRankings = securityRankings;
    }

    /**
     * @return the statuses
     */
    public List<SelectItem> getStatuses() {
        return statuses;
    }

    /**
     * @param statuses the statuses to set
     */
    public void setStatuses(List<SelectItem> statuses) {
        this.statuses = statuses;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @param language the language to set
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * @return the languages
     */
    public List<SelectItem> getLanguages() {
        return languages;
    }

    /**
     * @param languages the languages to set
     */
    public void setLanguages(List<SelectItem> languages) {
        this.languages = languages;
    }

    /**
     * @return the deepOption
     */
    public String getSelectedDeepOption() {
        return selectedDeepOption;
    }

    /**
     * @param deepOption the deepOption to set
     */
    public void setSelectedDeepOption(String deepOption) {
        this.selectedDeepOption = deepOption;
    }

    /**
     * @return the languageOption
     */
    public String getSelectedLanguageOption() {
        return selectedLanguageOption;
    }

    /**
     * @param languageOption the languageOption to set
     */
    public void setSelectedLanguageOption(String languageOption) {
        this.selectedLanguageOption = languageOption;
    }

    public Serializable getDynamicProperty6() {
        return dynamicProperty6;
    }

    public void setDynamicProperty6(Serializable dynamicProperty6) {
        this.dynamicProperty6 = dynamicProperty6;
    }

    public Serializable getDynamicProperty7() {
        return dynamicProperty7;
    }

    public void setDynamicProperty7(Serializable dynamicProperty7) {
        this.dynamicProperty7 = dynamicProperty7;
    }

    public Serializable getDynamicProperty8() {
        return dynamicProperty8;
    }

    public void setDynamicProperty8(Serializable dynamicProperty8) {
        this.dynamicProperty8 = dynamicProperty8;
    }

    public Serializable getDynamicProperty9() {
        return dynamicProperty9;
    }

    public void setDynamicProperty9(Serializable dynamicProperty9) {
        this.dynamicProperty9 = dynamicProperty9;
    }

    public Serializable getDynamicProperty10() {
        return dynamicProperty10;
    }

    public void setDynamicProperty10(Serializable dynamicProperty10) {
        this.dynamicProperty10 = dynamicProperty10;
    }

    public Serializable getDynamicProperty11() {
        return dynamicProperty11;
    }

    public void setDynamicProperty11(Serializable dynamicProperty11) {
        this.dynamicProperty11 = dynamicProperty11;
    }

    public Serializable getDynamicProperty12() {
        return dynamicProperty12;
    }

    public void setDynamicProperty12(Serializable dynamicProperty12) {
        this.dynamicProperty12 = dynamicProperty12;
    }

    public Serializable getDynamicProperty13() {
        return dynamicProperty13;
    }

    public void setDynamicProperty13(Serializable dynamicProperty13) {
        this.dynamicProperty13 = dynamicProperty13;
    }

    public Serializable getDynamicProperty14() {
        return dynamicProperty14;
    }

    public void setDynamicProperty14(Serializable dynamicProperty14) {
        this.dynamicProperty14 = dynamicProperty14;
    }

    public Serializable getDynamicProperty15() {
        return dynamicProperty15;
    }

    public void setDynamicProperty15(Serializable dynamicProperty15) {
        this.dynamicProperty15 = dynamicProperty15;
    }

    public Serializable getDynamicProperty16() {
        return dynamicProperty16;
    }

    public void setDynamicProperty16(Serializable dynamicProperty16) {
        this.dynamicProperty16 = dynamicProperty16;
    }

    public Serializable getDynamicProperty17() {
        return dynamicProperty17;
    }

    public void setDynamicProperty17(Serializable dynamicProperty17) {
        this.dynamicProperty17 = dynamicProperty17;
    }

    public Serializable getDynamicProperty18() {
        return dynamicProperty18;
    }

    public void setDynamicProperty18(Serializable dynamicProperty18) {
        this.dynamicProperty18 = dynamicProperty18;
    }

    public Serializable getDynamicProperty19() {
        return dynamicProperty19;
    }

    public void setDynamicProperty19(Serializable dynamicProperty19) {
        this.dynamicProperty19 = dynamicProperty19;
    }

    public Serializable getDynamicProperty20() {
        return dynamicProperty20;
    }

    public void setDynamicProperty20(Serializable dynamicProperty20) {
        this.dynamicProperty20 = dynamicProperty20;
    }

    public void setDynamicProperty(int index, Serializable dynamicProperty) {
        switch (index) {
            case 1:
                this.dynamicProperty1 = dynamicProperty;
                break;
            case 2:
                this.dynamicProperty2 = dynamicProperty;
                break;
            case 3:
                this.dynamicProperty3 = dynamicProperty;
                break;
            case 4:
                this.dynamicProperty4 = dynamicProperty;
                break;
            case 5:
                this.dynamicProperty5 = dynamicProperty;
                break;
            case 6:
                this.dynamicProperty6 = dynamicProperty;
                break;
            case 7:
                this.dynamicProperty7 = dynamicProperty;
                break;
            case 8:
                this.dynamicProperty8 = dynamicProperty;
                break;
            case 9:
                this.dynamicProperty9 = dynamicProperty;
                break;
            case 10:
                this.dynamicProperty10 = dynamicProperty;
                break;
            case 11:
                this.dynamicProperty11 = dynamicProperty;
                break;
            case 12:
                this.dynamicProperty12 = dynamicProperty;
                break;
            case 13:
                this.dynamicProperty13 = dynamicProperty;
                break;
            case 14:
                this.dynamicProperty14 = dynamicProperty;
                break;
            case 15:
                this.dynamicProperty15 = dynamicProperty;
                break;
            case 16:
                this.dynamicProperty16 = dynamicProperty;
                break;
            case 17:
                this.dynamicProperty17 = dynamicProperty;
                break;
            case 18:
                this.dynamicProperty18 = dynamicProperty;
                break;
            case 19:
                this.dynamicProperty19 = dynamicProperty;
                break;
            case 20:
                this.dynamicProperty20 = dynamicProperty;
                break;
            default:
                break;
        }

    }

    public Serializable getDynamicProperty(int index) {
        switch (index) {
            case 1:
                return this.dynamicProperty1;
            case 2:
                return this.dynamicProperty2;
            case 3:
                return this.dynamicProperty3;
            case 4:
                return this.dynamicProperty4;
            case 5:
                return this.dynamicProperty5;
            case 6:
                return this.dynamicProperty6;
            case 7:
                return this.dynamicProperty7;
            case 8:
                return this.dynamicProperty8;
            case 9:
                return this.dynamicProperty9;
            case 10:
                return this.dynamicProperty10;
            case 11:
                return this.dynamicProperty11;
            case 12:
                return this.dynamicProperty12;
            case 13:
                return this.dynamicProperty13;
            case 14:
                return this.dynamicProperty14;
            case 15:
                return this.dynamicProperty15;
            case 16:
                return this.dynamicProperty16;
            case 17:
                return this.dynamicProperty17;
            case 18:
                return this.dynamicProperty18;
            case 19:
                return this.dynamicProperty19;
            case 20:
                return this.dynamicProperty20;
            default:
                return null;
        }
    }
}
