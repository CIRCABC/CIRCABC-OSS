package eu.cec.digit.circabc.repo.app;

import eu.cec.digit.circabc.service.app.CircabcService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class ApplicationListenerBean implements ApplicationListener<ContextRefreshedEvent> {

    private static final Log logger = LogFactory.getLog(ApplicationListenerBean.class);
    private AtomicBoolean isExecutedLoadModel = new AtomicBoolean(false);
    private AtomicBoolean isExecutedCopyESAPI = new AtomicBoolean(false);
    private AtomicBoolean isExecutedSyncGroupLogos = new AtomicBoolean(false);
    private Resource antisamyEsapi;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event != null) {

            if (isExecutedLoadModel.compareAndSet(false, true)) {
                ApplicationContext applicationContext = event.getApplicationContext();
                CircabcService circabcService =
                        (CircabcService) applicationContext.getBean("CircabcService");
                if (circabcService.syncEnabled()) {
                    circabcService.loadModel();
                }
            }
            if (isExecutedCopyESAPI.compareAndSet(false, true)) {
                final String esapiDir = System.getProperty("user.home") + File.separator + "esapi";
                final boolean mkdirs = new java.io.File(esapiDir).mkdirs();
                if (mkdirs && logger.isInfoEnabled()) {
                    logger.info("ESAPI home folder created");
                }
                try {
                    final InputStream is = antisamyEsapi.getInputStream();
                    FileOutputStream fos =
                            new FileOutputStream(
                                    new java.io.File(esapiDir + File.separator + "antisamy-esapi.xml"));
                    IOUtils.copy(is, fos);
                } catch (IOException e) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Error when copy antisamy-esapi.xml to user home esapi folder", e);
                    }
                }
            }

            if (isExecutedSyncGroupLogos.compareAndSet(false, true)) {
                ApplicationContext applicationContext = event.getApplicationContext();
                CircabcService circabcService =
                        (CircabcService) applicationContext.getBean("CircabcService");
                if (circabcService.syncEnabled()) {
                    circabcService.syncGroupLogos();
                }
            }
        }
    }

    public void setAntisamyEsapi(Resource antisamyEsapi) {
        this.antisamyEsapi = antisamyEsapi;
    }
}
