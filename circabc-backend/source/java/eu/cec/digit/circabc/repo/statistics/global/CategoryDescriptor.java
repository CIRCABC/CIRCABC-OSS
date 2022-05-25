/**
 * Copyright 2006 European Community
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
 */
/**
 *
 */
package eu.cec.digit.circabc.repo.statistics.global;

import eu.cec.digit.circabc.repo.statistics.ig.IgDescriptor;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;
import java.util.Set;

/** @author beaurpi */
public class CategoryDescriptor {

    private String name;
    private String title;
    private NodeRef ref;
    private List<IgDescriptor> listOfIgs;
    private Set<String> listOfAdmins;

    /** */
    public CategoryDescriptor() {
    }

    /** @return the name */
    public String getName() {
        return name;
    }

    /** @param name the name to set */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the title */
    public String getTitle() {
        return title;
    }

    /** @param title the title to set */
    public void setTitle(String title) {
        this.title = title;
    }

    /** @return the ref */
    public NodeRef getRef() {
        return ref;
    }

    /** @param ref the ref to set */
    public void setRef(NodeRef ref) {
        this.ref = ref;
    }

    /** @return the listOfIgs */
    public List<IgDescriptor> getListOfIgs() {
        return listOfIgs;
    }

    /** @param listOfIgs the listOfIgs to set */
    public void setListOfIgs(List<IgDescriptor> listOfIgs) {
        this.listOfIgs = listOfIgs;
    }

    /** @return the listOfAdmins */
    public Set<String> getListOfAdmins() {
        return listOfAdmins;
    }

    /** @param listOfAdmins the listOfAdmins to set */
    public void setListOfAdmins(Set<String> listOfAdmins) {
        this.listOfAdmins = listOfAdmins;
    }
}
