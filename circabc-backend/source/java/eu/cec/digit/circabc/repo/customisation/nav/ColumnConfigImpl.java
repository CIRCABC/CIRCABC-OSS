/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.cec.digit.circabc.repo.customisation.nav;

import eu.cec.digit.circabc.service.customisation.nav.ColumnConfig;
import org.alfresco.util.ParameterCheck;

import java.io.Serializable;

/**
 * Concrete implementation of a column navigation customisation
 *
 * @author Yanick Pignot
 */
public class ColumnConfigImpl implements Serializable, ColumnConfig {

    private static final String DYN_ATTR = "dynAttr";

    /**
     *
     */
    private static final long serialVersionUID = 9173564530415575122L;

    private final String name;
    private String label;
    private String converter;
    private String resolver;

    /*package*/ ColumnConfigImpl(final String name) {
        ParameterCheck.mandatoryString("The column name", name);
        this.name = name;
    }

    /**
     * @return the converter
     */
    public final String getConverter() {
        return mandatory(converter, null);
    }

    /**
     * @param converter the converter to set
     */
    /*package*/
    final void setConverter(String converter) {
        this.converter = converter;
    }

    /**
     * @return the label
     */
    public final String getLabel() {
        return mandatory(label, name);
    }

    /**
     * @param label the label to set
     */
    public final void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the name
     */
    public final String getName() {
        return name;
    }

    /**
     * @return the resolver
     */
    public final String getResolver() {
        return mandatory(resolver, name);
    }

    /**
     * @param resolver the resolver to set
     */
    /*package*/
    final void setResolver(String resolver) {
        this.resolver = resolver;
    }

    private String mandatory(final String expected, final String replacement) {
        if (expected == null || expected.length() < 1) {
            return replacement;
        } else {
            return expected;
        }
    }

    @Override
    public String toString() {
        return "Column [name:"
                + name
                + ",label:"
                + label
                + ",converter:"
                + converter
                + ",resolover:"
                + resolver
                + "]";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((converter == null) ? 0 : converter.hashCode());
        result = PRIME * result + ((label == null) ? 0 : label.hashCode());
        result = PRIME * result + ((name == null) ? 0 : name.hashCode());
        result = PRIME * result + ((resolver == null) ? 0 : resolver.hashCode());
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
        final ColumnConfigImpl other = (ColumnConfigImpl) obj;
        if (converter == null) {
            if (other.converter != null) {
                return false;
            }
        } else if (!converter.equals(other.converter)) {
            return false;
        }
        if (label == null) {
            if (other.label != null) {
                return false;
            }
        } else if (!label.equals(other.label)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (resolver == null) {
            if (other.resolver != null) {
                return false;
            }
        } else if (!resolver.equals(other.resolver)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isDynamicProperty() {

        return name.startsWith(DYN_ATTR);
    }
}
