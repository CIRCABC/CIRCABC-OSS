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
package eu.cec.digit.circabc.web.wai.bean.navigation.library;

import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.wai.bean.navigation.LibraryBean;
import eu.cec.digit.circabc.web.wai.bean.navigation.news.PostBean;
import eu.cec.digit.circabc.web.wai.menu.ActionWrapper;

import java.util.List;

/**
 * Bean that backs the navigation inside the posts of the library service
 *
 * @author yanick pignot
 */
public class DiscussionPostBean extends PostBean {

    public static final String BEAN_NAME = "LibPostBean";
    private static final long serialVersionUID = -5757464931958052246L;
    private DiscussionForumBean libForumBean;
    private LibraryBean libraryBean;

    public NavigableNodeType getManagedNodeType() {
        return NavigableNodeType.LIBRARY_POST;
    }

    @Override
    public List<ActionWrapper> getActions() {
        return getLibraryBean().getActions();
    }

    /**
     * @return the libForumBean
     */
    protected final DiscussionForumBean getLibForumBean() {
        if (libForumBean == null) {
            libForumBean = (DiscussionForumBean) Beans.getWaiBrosableBean(DiscussionForumBean.BEAN_NAME);
        }
        return libForumBean;
    }

    /**
     * @param libForumBean the libForumBean to set
     */
    public final void setLibForumBean(DiscussionForumBean libForumBean) {
        this.libForumBean = libForumBean;
    }

    /**
     * @return the libraryBean
     */
    protected final LibraryBean getLibraryBean() {
        if (libraryBean == null) {
            libraryBean = (LibraryBean) Beans.getWaiBrosableBean(LibraryBean.BEAN_NAME);
        }
        return libraryBean;
    }

    /**
     * @param libraryBean the libraryBean to set
     */
    public final void setLibraryBean(LibraryBean libraryBean) {
        this.libraryBean = libraryBean;
    }


}
