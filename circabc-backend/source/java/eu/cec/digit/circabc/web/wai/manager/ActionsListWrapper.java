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
package eu.cec.digit.circabc.web.wai.manager;

import org.alfresco.web.bean.repository.Node;

import java.io.Serializable;


/**
 * Wrap a simple Action list for any container for the right menu.
 *
 * @author yanick pignot
 */
public class ActionsListWrapper implements Serializable {

    private static final long serialVersionUID = -3257773143591523891L;


    private Object context;
    private String actions;

    /**
     * @param context
     * @param actions
     */
    public ActionsListWrapper(Node context, String actions) {
        super();
        this.context = context;
        this.actions = actions;
    }

    /**
     * @param context
     * @param actions
     */
    public ActionsListWrapper(Object context, String actions) {
        super();
        this.context = context;
        this.actions = actions;
    }

    /**
     * @return the actions
     */
    public String getActions() {
        return actions;
    }

    /**
     * @return the context
     */
    public Object getContext() {
        return context;
    }


}
