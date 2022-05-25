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
package eu.cec.digit.circabc.web.comparator;

import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.Comparator;

public class SelectItemLabelComparator implements Comparator<SelectItem>, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 2167978083235849666L;

    @Override
    public int compare(SelectItem selectItem1, SelectItem selectItem2) {

        if ((selectItem1.getLabel() != null) && selectItem2.getLabel() != null) {
            return selectItem1.getLabel().compareTo(selectItem2.getLabel());
        } else if ((selectItem1.getLabel() == null) && selectItem2.getLabel() == null) {
            return 0;
        } else if ((selectItem1.getLabel() == null)) {
            return -1;
        } else {
            return 1;
        }

    }


}
