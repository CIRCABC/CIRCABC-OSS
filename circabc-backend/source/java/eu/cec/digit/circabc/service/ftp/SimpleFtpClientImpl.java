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
package eu.cec.digit.circabc.service.ftp;

import it.sauronsoftware.ftp4j.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;

/** @author beaurpi */
public class SimpleFtpClientImpl implements SimpleFtpClient {

    /** A logger for the class */
    private static final Log logger = LogFactory.getLog(SimpleFtpClientImpl.class);
    private String host;
    private Integer port;
    private String username;
    private String password;
    private String path;
    private FTPClient ftpClient;
    private String directory;
    private String filename;

    @Override
    public void initParameters(
            String host, Integer port, String username, String password, String path)
            throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {

        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.path = path;

        ftpClient = new FTPClient();

        ftpClient.connect(host, port);
        ftpClient.setPassive(true);
        ftpClient.login(username, password);

        if (path.length() > 0) {

            if (path.contains("/")) {

                int lastIndexOfSlash = path.lastIndexOf('/');

                this.directory = path.substring(0, lastIndexOfSlash);

                if (lastIndexOfSlash < path.lastIndexOf('.')) {
                    this.setFilename(path.substring(lastIndexOfSlash + 1));
                }
                ftpClient.changeDirectory(directory);
            } else {
                this.setFilename(path);
                this.directory = "";
            }
        }
    }

    private Boolean isConfigured() {
        return ftpClient.isConnected();
    }

    @Override
    public String[] listFiles()
            throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException,
            FTPDataTransferException, FTPAbortedException, FTPListParseException {

        String[] listOfFiles = null;

        if (isConfigured()) {
            listOfFiles = ftpClient.listNames();
        }

        return listOfFiles;
    }

    /** @return the host */
    public String getHost() {
        return host;
    }

    /** @param host the host to set */
    public void setHost(String host) {
        this.host = host;
    }

    /** @return the port */
    public Integer getPort() {
        return port;
    }

    /** @param port the port to set */
    public void setPort(Integer port) {
        this.port = port;
    }

    /** @return the username */
    public String getUsername() {
        return username;
    }

    /** @param username the username to set */
    public void setUsername(String username) {
        this.username = username;
    }

    /** @return the password */
    public String getPassword() {
        return password;
    }

    /** @param password the password to set */
    public void setPassword(String password) {
        this.password = password;
    }

    /** @return the path */
    public String getPath() {
        return path;
    }

    /** @param path the path to set */
    public void setPath(String path) {
        this.path = path;
    }

    /** @return the ftpClient */
    public FTPClient getFtpClient() {
        return ftpClient;
    }

    /** @param ftpClient the ftpClient to set */
    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }

    /** @return the directory */
    public String getDirectory() {
        return directory;
    }

    /** @param directory the directory to set */
    public void setDirectory(String directory) {
        this.directory = directory;
    }

    /** @return the filename */
    public String getFilename() {
        return filename;
    }

    /** @param filename the filename to set */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public void logout() {

        try {

            ftpClient.disconnect(true);

        } catch (IllegalStateException | FTPException | FTPIllegalReplyException | IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error(e);
            }
        }
    }

    @Override
    public Boolean fileExists(String fileName)
            throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException,
            FTPDataTransferException, FTPAbortedException, FTPListParseException {

        Boolean result = false;

        for (String file : ftpClient.listNames()) {
            if (file.equals(fileName)) {
                result = true;
            }
        }

        return result;
    }

    /** * do not forget to close temp file after using it ! */
    public File downloadFile(String filename)
            throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException,
            FTPDataTransferException, FTPAbortedException, FTPListParseException {
        File localFile = File.createTempFile(filename, ".tmp");

        if (!filename.isEmpty()) {
            if (fileExists(filename)) {
                ftpClient.download(filename, localFile);
            }
        }

        return localFile;
    }

    @Override
    public void renameRemoteFile(String fileName, String newFileName)
            throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {

        ftpClient.rename(fileName, newFileName);
    }

    @Override
    public String getFileName() {

        return filename;
    }
}
