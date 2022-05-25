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
package eu.cec.digit.circabc.repo.module;

import org.alfresco.repo.importer.ImporterBootstrap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Properties;

public class ImporterModuleComponent extends org.alfresco.repo.module.ImporterModuleComponent {

    private static final Log logger = LogFactory.getLog(ImporterModuleComponent.class);

    @Override
    protected void checkProperties() {
        // TODO Auto-generated method stub
        super.checkProperties();
    }

    @Override
    protected void executeInternal() throws Throwable {
        // TODO Auto-generated method stub
        if (logger.isErrorEnabled()) {
            logger.info("Circabc Importing Process starting");
        }

        super.executeInternal();
        if (logger.isErrorEnabled()) {
            logger.info("Circabc Importing Process finished");
        }
    }

    @Override
    public void setBootstrapView(final Properties bootstrapView) {
        // TODO Auto-generated method stub
        super.setBootstrapView(bootstrapView);
    }

    @Override
    public void setBootstrapViews(final List<Properties> bootstrapViews) {
        // TODO Auto-generated method stub
        super.setBootstrapViews(bootstrapViews);
    }

    @Override
    public void setImporter(final ImporterBootstrap importer) {
        // TODO Auto-generated method stub
        super.setImporter(importer);
    }
}
