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
package eu.cec.digit.circabc.web.ui.repo.converter;


/**
 * The display path of a file link or a folder link must start from circabc root
 *
 * @author yanick pignot
 */
public class CircabcDisplayPathConverter extends DisplayPathAbstractConverter {

    /**
     * <p>The standard converter id for this converter.</p>
     */
    public static final String CONVERTER_ID = "eu.cec.digit.circabc.faces.CircabcDisplayPathConverter";


    public CircabcDisplayPathConverter() {
        super();
    }

    @Override
    protected FromElement getFromElement() {
        return FromElement.CIRCABC_ROOT;
    }

    @Override
    protected LinkType getLinkType() {
        return LinkType.DOWNLOAD_BROWSE;
    }
}
