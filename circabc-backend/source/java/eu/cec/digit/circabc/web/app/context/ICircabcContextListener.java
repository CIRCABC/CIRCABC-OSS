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
package eu.cec.digit.circabc.web.app.context;

/**
 * Beans supporting the ICircabcContextListener interface are registered against this class. Then
 * Beans which wish to indicate that the UI should refresh itself i.e. dump all cached data and
 * settings, call the UICircabcContextService.igRootChanged() to inform all registered instances of
 * the change.
 * <p>
 * Registered beans will also be informed of changes in location, for example when the current ig
 * root, circabc... changes or when the user has changed area i.e. from company home to my home.
 *
 * @author yanick pignot
 */
public interface ICircabcContextListener {

    /**
     * Method called by UICircabcContextService.circabcLeaved() to inform all registered beans that
     * the user don't longer navigate into circabc
     */
    void circabcLeaved();

    /**
     * Method called by UICircabcContextService.circabcEntered() to inform all registered beans that
     * the user was not in circabc and now it enters into.
     */
    void circabcEntered();

    /**
     * Method called by UICircabcContextService.categoryHeaderChanged() to inform all registered beans
     * that the user change of Category Header
     */
    void categoryHeaderChanged();

    /**
     * Method called by UICircabcContextService.categoryChanged() to inform all registered beans that
     * the user change of Category
     */
    void categoryChanged();

    /**
     * Method called by UICircabcContextService.igRootChanged() to inform all registered beans that
     * the user change of IG root
     */
    void igRootChanged();

    /**
     * Method called by UICircabcContextService.igServiceChanged() to inform all registered beans that
     * the user change of IG Service
     */
    void igServiceChanged();

}
