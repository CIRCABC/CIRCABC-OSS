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
package eu.cec.digit.circabc.business.impl.nav;

import eu.cec.digit.circabc.business.api.BusinessRegistry;
import eu.cec.digit.circabc.business.api.BusinessValidationError;
import eu.cec.digit.circabc.business.api.nav.NavNode;
import eu.cec.digit.circabc.business.api.nav.NodeType;
import eu.cec.digit.circabc.business.api.props.PropertyItem;
import eu.cec.digit.circabc.business.api.props.RuntimePropertyResolver;
import eu.cec.digit.circabc.business.impl.props.ConfigurableProperty;
import eu.cec.digit.circabc.repo.struct.SimplePath;
import org.alfresco.service.cmr.repository.NodeRef;

import java.io.Serializable;
import java.util.*;

/**
 * Circabc implementation of a node.
 *
 * @author Yanick Pignot
 * <p>
 * TODO this class is sample but not final implementation of NavNode. --> Read NavNode
 */
public class NavNodeMap implements NavNode, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7943806693314741668L;
    private final NodeRef nodeId;
    private transient BusinessRegistry registry = null;
    private String title = null;
    private NodeType type = null;
    private SimplePath path = null;
    private Map<String, Boolean> permissions = null;
    private Boolean locked = null;
    private NavNode parent = null;
    private List<NavNode> childs = null;
    private List<String> admins = null;
    private Map<String, Object> actions; // implement an action wrapper.
    private Map<String, PropertyItem> staticProperties;
    private Map<String, RuntimePropertyResolver> runtimeProperties = new HashMap<>(12);


    /**
     * @param nodeId
     * @param staticProperties
     * @param registry
     */
    public NavNodeMap(final NodeRef nodeId, final Map<String, PropertyItem> properties,
                      final BusinessRegistry registry) {
        super();
        this.nodeId = nodeId;
        this.staticProperties = properties;
        this.registry = registry;
    }

    /**
     * @return the admins
     */
    public final List<String> getAdmins() {
        if (admins == null) {
            // TODO
        }
        return admins;
    }

    /**
     * @return the childs
     */
    public final List<NavNode> getChilds() {
        if (childs == null) {
            // TODO
        }
        return childs;
    }

    /**
     * @return the locked
     */
    public final Boolean getLocked() {
        if (locked == null) {
            // TODO
        }
        return locked;
    }

    /**
     * @return the nodeId
     */
    public final NodeRef getNodeRef() {
        if (nodeId == null) {
            // TODO
        }
        return nodeId;
    }

    /**
     * @return the parent
     */
    public final NavNode getParent() {
        if (parent == null) {
            // TODO
        }
        return parent;
    }

    /**
     * @return the path
     */
    public final SimplePath getPath() {
        if (path == null) {
            // TODO
        }
        return path;
    }

    /**
     * @return the permissions
     */
    public final Map<String, Boolean> getPermissions() {
        if (permissions == null) {
            // TODO
        }
        return permissions;
    }

    /**
     * @return the title
     */
    public final String getTitle() {
        if (title == null) {
            // TODO
        }
        return title;
    }

    /**
     * @return the type
     */
    public final NodeType getType() {
        if (type == null) {
            // TODO
        }
        return type;
    }

    /**
     * @return the available actions
     */
    public final Map<String, Object> getActions() {
        if (actions == null) {
            // TODO

            // 1.	get allConfiguredAction
            // 		List<Action> allActions = getRegsitry().getActionsBusinessSrv().getActions(this.nodeRef);
            // 2.	Evaluate the actions
            //		for(final Action action: allActions)
            //		{
            //			if(action.canExecute(this) // check rights
            //					&& action.evaluate(this)) // check the evaluator (why not a set of evaluator!!! to reuseCode and avoid to have an evaluator by action)
            //			{
            //				this.actions.put(action.getName(), action);
            //			}
            //		}

        }
        return actions;
    }

    /**
     * @return an action indexed by the name
     */
    public final Object getAction(final String name) {
        final Object action = getActions().get(name);

        if (action == null) {
            throw new BusinessValidationError("business_validation_noright_ref");
        } else {
            return action;
        }
    }

    /**
     * @return an action
     */
    public final void runAction(final String name) {
        getActions().get(name)/*.run(this)*/;
    }


    /**
     * @return the registry
     */
    public final BusinessRegistry getRegistry() {
        return registry;
    }

    /**
     * Register a property resolver for the named property.
     *
     * @param name     Name of the property this resolver is for
     * @param resolver Property resolver to register
     */
    public final void addPropertyResolver(String name, RuntimePropertyResolver resolver) {
        runtimeProperties.put(name, resolver);
    }

    //---------
    // --- Map implemation methods that delegate to the property map method calls.

    /**
     * @see java.util.Map#clear()
     */
    public void clear() {
        title = null;
        type = null;
        path = null;
        permissions = null;
        locked = null;
        parent = null;
        childs = null;
        admins = null;
        staticProperties = null;
        actions = null;
    }

    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    public PropertyItem get(Object key) {
        PropertyItem item = getProperties().get(key);
        if (item == null) {
            // if a property resolver exists for this property name then invoke it
            final RuntimePropertyResolver resolver = runtimeProperties.get(key);
            if (resolver != null) {
                final Serializable value = resolver.get(this);
                item = new ConfigurableProperty(key.toString(), value);
                // cache the result
                getProperties().put(key.toString(), item);
            }
        }

        return item;
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        return getProperties().containsKey(key) || runtimeProperties.containsKey(key);
    }

    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
        return getProperties().containsValue(value);
    }

    /**
     * @see java.util.Map#entrySet()
     */
    public Set<Entry<String, PropertyItem>> entrySet() {
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return getProperties().isEmpty();
    }

    /**
     * @see java.util.Map#keySet()
     */
    public Set<String> keySet() {
        return getProperties().keySet();
    }

    /**
     * @throws UnsupportedOperationException always
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public PropertyItem put(String key, PropertyItem value) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public void putAll(Map<? extends String, ? extends PropertyItem> t) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException always
     * @see java.util.Map#remove(java.lang.Object)
     */
    public PropertyItem remove(Object key) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.util.Map#size()
     */
    public int size() {
        return getProperties().size();
    }

    /**
     * @see java.util.Map#values()
     */
    public Collection<PropertyItem> values() {
        return getProperties().values();
    }

    /**
     * @return the staticProperties
     */
    private Map<String, PropertyItem> getProperties() {
        if (staticProperties == null) {
            staticProperties = getRegistry().getPropertiesBusinessSrv().getProperties(getNodeRef());
        }

        return staticProperties;
    }


}
