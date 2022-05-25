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
package eu.cec.digit.circabc.web.wai.dialog.ig;

import eu.cec.digit.circabc.repo.external.repositories.RepositoryConfiguration;
import eu.cec.digit.circabc.service.external.repositories.ExternalRepositoriesManagementService;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Dialog to add or remove an available external repository.
 *
 * @author schwerr
 */
public class ManageExternalRepositoriesDialog extends BaseWaiDialog {

    private static final long serialVersionUID = 2844550968531893829L;

    private ExternalRepositoriesManagementService externalRepositoriesManagementService = null;

    private int pageSize = 10;
    private List<SelectItem> availableRepositories = null;
    private String selectedAvailableRepository = null;
    private String parentNodeId = null;

    @Override
    public void init(Map<String, String> parameters) {

        super.init(parameters);

        if (parameters != null) {

            if (parentNodeId == null && parameters.containsKey("id")) {
                parentNodeId = "workspace://SpacesStore/" + parameters.get("id");
            }

            if (parameters.containsKey("removeName")) {

                String name = parameters.get("removeName");

                externalRepositoriesManagementService.removeRepository(
                        parentNodeId, name);
                getConfiguredRepositories();
            }
        }

        fillAvailableRepositories();
    }

    private void fillAvailableRepositories() {

        this.availableRepositories = new ArrayList<>();

        Collection<RepositoryConfiguration> availableRepositories =
                externalRepositoriesManagementService.getAvailableRepositories();

        Collection<RepositoryConfiguration> configuredRepositories =
                externalRepositoriesManagementService.getConfiguredRepositories(
                        parentNodeId);

        for (RepositoryConfiguration repositoryConfiguration : availableRepositories) {
            if (!configuredRepositories.contains(repositoryConfiguration)) {
                this.availableRepositories.add(new SelectItem(
                        repositoryConfiguration.getName(),
                        repositoryConfiguration.getName()));
            }
        }
    }

    public List<Map<String, String>> getConfiguredRepositories() {

        Collection<RepositoryConfiguration> repositories =
                externalRepositoriesManagementService.getConfiguredRepositories(
                        parentNodeId);

        List<Map<String, String>> configuredRepositories = new ArrayList<>();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy");

        for (RepositoryConfiguration repositoryConfiguration : repositories) {

            Map<String, String> repository = new HashMap<>();

            repository.put("icon", "/images/extension/storage_icon_small.png");
            repository.put("name", repositoryConfiguration.getName());
            repository.put("date", simpleDateFormat.format(
                    repositoryConfiguration.getRegistrationDate()));

            configuredRepositories.add(repository);
        }

        return configuredRepositories;
    }

    public String addRepository() {

        RepositoryConfiguration configuration = new RepositoryConfiguration();

        configuration.setName(selectedAvailableRepository);

        externalRepositoriesManagementService.addRepository(parentNodeId, configuration);

        fillAvailableRepositories();
        getConfiguredRepositories();

        return "wai:dialog:close:wai:dialog:manageExternalRepositoriesDialogWai";
    }

    public String removeRepository() {
        return "wai:dialog:close:wai:dialog:manageExternalRepositoriesDialogWai";
    }

    /**
     * @see eu.cec.digit.circabc.web.wai.dialog.WaiDialog#getPageIconAltText()
     */
    @Override
    public String getPageIconAltText() {
        return translate("manage_external_repositories_dialog_description");
    }

    /**
     * @see eu.cec.digit.circabc.web.wai.dialog.WaiDialog#getBrowserTitle()
     */
    @Override
    public String getBrowserTitle() {
        return translate("manage_external_repositories_dialog_title");
    }

    @Override
    public String getCancelButtonLabel() {
        return translate("close");
    }

    /**
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext,
     * java.lang.String)
     */
    @Override
    protected String finishImpl(FacesContext context, String outcome)
            throws Throwable {
        return outcome;
    }

    /**
     * Gets the value of the availableRepositories
     *
     * @return the availableRepositories
     */
    public List<SelectItem> getAvailableRepositories() {
        return availableRepositories;
    }

    /**
     * Sets the value of the availableRepositories
     *
     * @param availableRepositories the availableRepositories to set.
     */
    public void setAvailableRepositories(List<SelectItem> availableRepositories) {
        this.availableRepositories = availableRepositories;
    }

    /**
     * Gets the value of the pageSize
     *
     * @return the pageSize
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Gets the value of the selectedAvailableRepository
     *
     * @return the selectedAvailableRepository
     */
    public String getSelectedAvailableRepository() {
        return selectedAvailableRepository;
    }

    /**
     * Sets the value of the selectedAvailableRepository
     *
     * @param selectedAvailableRepository the selectedAvailableRepository to set.
     */
    public void setSelectedAvailableRepository(String selectedAvailableRepository) {
        this.selectedAvailableRepository = selectedAvailableRepository;
    }

    /**
     * Sets the value of the externalRepositoriesManagementService
     *
     * @param externalRepositoriesManagementService the externalRepositoriesManagementService to set.
     */
    public void setExternalRepositoriesManagementService(
            ExternalRepositoriesManagementService externalRepositoriesManagementService) {
        this.externalRepositoriesManagementService = externalRepositoriesManagementService;
    }
}
