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
package eu.cec.digit.circabc.business.api.space;

import java.io.Serializable;

/**
 * Encapsulate the name and the path for container icons.
 *
 * @author Yanick Pignot
 */
public class ContainerIcon implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1857089599626976616L;

    private final String iconName;
    private final String iconPath;

    public ContainerIcon(final String iconName, final String iconPath) {
        super();
        this.iconName = iconName;
        this.iconPath = iconPath;
    }


    /**
     * @return the iconName
     */
    public final String getIconName() {
        return iconName;
    }

    /**
     * @return the iconPath
     */
    public final String getIconPath() {
        return iconPath;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((iconName == null) ? 0 : iconName.hashCode());
        result = PRIME * result + ((iconPath == null) ? 0 : iconPath.hashCode());
        return result;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ContainerIcon other = (ContainerIcon) obj;
        if (iconName == null) {
            if (other.iconName != null) {
                return false;
            }
        } else if (!iconName.equals(other.iconName)) {
            return false;
        }
        if (iconPath == null) {
            if (other.iconPath != null) {
                return false;
            }
        } else if (!iconPath.equals(other.iconPath)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Icon:" +
                iconName +
                "|Path:" +
                iconPath;
    }
}
