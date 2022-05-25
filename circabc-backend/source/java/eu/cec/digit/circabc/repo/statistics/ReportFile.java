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
package eu.cec.digit.circabc.repo.statistics;

import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.web.app.servlet.DownloadContentServlet;

import java.math.BigDecimal;

/** @author beaurpi */
public class ReportFile {

    private String name;
    private FileInfo fileInfo;
    private String downloadUrl;
    private String sizeAsString;

    public ReportFile() {
        // TODO Auto-generated constructor stub
    }

    public ReportFile(FileInfo f, String name) {
        this.fileInfo = f;
        this.name = name;
        convertFileInfoSize();
        downloadUrl = getUrl(f);
    }

    /** */
    private void convertFileInfoSize() {
        BigDecimal size = new BigDecimal(fileInfo.getContentData().getSize() / 1024);

        this.sizeAsString = size.setScale(2, BigDecimal.ROUND_DOWN).toString() + "kbs";
    }

    /** @return the name */
    public String getName() {
        return name;
    }

    /** @param name the name to set */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the fileInfo */
    public FileInfo getFileInfo() {
        return fileInfo;
    }

    /** @param fileInfo the fileInfo to set */
    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
        convertFileInfoSize();
    }

    /** @return the downloadUrl */
    public String getDownloadUrl() {
        return downloadUrl;
    }

    /** @param downloadUrl the downloadUrl to set */
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    private String getUrl(FileInfo fileInfo) {

        return DownloadContentServlet.generateBrowserURL(fileInfo.getNodeRef(), fileInfo.getName());
    }

    /** @return the sizeAsString */
    public String getSizeAsString() {
        return sizeAsString;
    }

    /** @param sizeAsString the sizeAsString to set */
    public void setSizeAsString(String sizeAsString) {
        this.sizeAsString = sizeAsString;
    }
}
