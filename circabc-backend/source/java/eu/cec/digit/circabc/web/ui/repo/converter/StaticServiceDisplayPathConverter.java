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
 * The display path of a file link or a folder link must start from the libray
 *
 * @author yanick pignot
 */
public class StaticServiceDisplayPathConverter
  extends DisplayPathAbstractConverter {

  /**
   * <p>The standard converter id for this converter.</p>
   */
  public static final String CONVERTER_ID =
    "eu.cec.digit.circabc.faces.StaticServiceDisplayPathConverter";

  public StaticServiceDisplayPathConverter() {
    super();
  }

  @Override
  protected FromElement getFromElement() {
    return FromElement.SERVICE;
  }

  @Override
  protected LinkType getLinkType() {
    return LinkType.NONE;
  }
}
