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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/** @author beaurpi */
public interface SimpleFtpClient {

    /**
     * * Init parameters to connect to FTP server
     *
     * @param host
     * @param port
     * @param username
     * @param password
     * @param path
     * @throws FTPException
     * @throws FTPIllegalReplyException
     * @throws IOException
     * @throws IllegalStateException
     */
    void initParameters(String host, Integer port, String username, String password, String path)
            throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException;

    /**
     * * list files of remote server
     *
     * @return
     * @throws FTPException
     * @throws FTPIllegalReplyException
     * @throws IOException
     * @throws IllegalStateException
     * @throws FTPListParseException
     * @throws FTPAbortedException
     * @throws FTPDataTransferException
     */
    String[] listFiles()
            throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException,
            FTPDataTransferException, FTPAbortedException, FTPListParseException;

    /** * disconnect from ftp */
    void logout();

    /**
     * * verify if one file is present on server
     *
     * @param fileName
     * @return
     * @throws FTPListParseException
     * @throws FTPAbortedException
     * @throws FTPDataTransferException
     * @throws FTPException
     * @throws FTPIllegalReplyException
     * @throws IOException
     * @throws IllegalStateException
     */
    Boolean fileExists(String fileName)
            throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException,
            FTPDataTransferException, FTPAbortedException, FTPListParseException;

    /**
     * * download file locally
     *
     * @param filename
     * @return
     * @throws IllegalStateException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws FTPIllegalReplyException
     * @throws FTPException
     * @throws FTPDataTransferException
     * @throws FTPAbortedException
     * @throws FTPListParseException
     */
    File downloadFile(String filename)
            throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException,
            FTPDataTransferException, FTPAbortedException, FTPListParseException;

    /**
     * * rename file in the remote ftp server
     *
     * @param fileName
     * @param newFileName
     * @throws FTPException
     * @throws FTPIllegalReplyException
     * @throws IOException
     * @throws IllegalStateException
     */
    void renameRemoteFile(String fileName, String newFileName)
            throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException;

    /**
     * *
     *
     * @return
     */
    String getFileName();
}
