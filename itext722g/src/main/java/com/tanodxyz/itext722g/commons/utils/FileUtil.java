/*

    This file is part of the iText (R) project.
    Copyright (c) 1998-2022 iText Group NV
    Authors: Bruno Lowagie, Paulo Soares, et al.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package com.tanodxyz.itext722g.commons.utils;


import android.os.Build;

import com.tanodxyz.itext722g.IText722;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * This file is a helper class for internal usage only.
 * Be aware that its API and functionality may be changed in future.
 */
public final class FileUtil {

    private FileUtil() {
    }

    /**
     * Gets the default windows font directory.
     *
     * @return the default windows font directory
     */
    public static String getFontsDir() {
        try {
            return IText722.ANDROID_FONTS_DIR;
        } catch (SecurityException e) {
            Logger.getLogger(FileUtil.class.getName()).warning("Can't access "+IText722.ANDROID_FONTS_DIR+" to load fonts. ");
            return null;
        }
    }

    /**
     * Checks whether there is a file at the provided path.
     *
     * @param path the path to the file to be checked on existence
     * @return {@code true} if such a file exists, otherwise {@code false}
     */
    public static boolean fileExists(String path) {
        if (path != null) {
            File f = new File(path);
            return f.exists() && f.isFile();
        }
        return false;
    }

    /**
     * Checks whether there is a directory at the provided path.
     *
     * @param path the path to the directory to be checked on existence
     * @return {@code true} if such a directory exists, otherwise {@code false}
     */

    public static boolean directoryExists(String path) {
        if (path != null) {
            File f = new File(path);
            return f.exists() && f.isDirectory();
        }
        return false;
    }

    /**
     * Lists all the files located at the provided directory.
     *
     * @param path      path to the directory
     * @param recursive if {@code true}, files from all the subdirectories will be returned
     * @return all the files located at the provided directory
     */
    public static String[] listFilesInDirectory(String path, boolean recursive) {
        if (path != null) {
            File root = new File(path);
            if (root.exists() && root.isDirectory()) {
                File[] files = root.listFiles();
                if (files != null) {
                    // Guarantee invariant order in all environments
                    Arrays.sort(files, new CaseSensitiveFileComparator());
                    List<String> list = new ArrayList<>();
                    for (File file : files) {
                        if (file.isDirectory() && recursive) {
                            listAllFiles(file.getAbsolutePath(), list);
                        } else {
                            list.add(file.getAbsolutePath());
                        }
                    }
                    return list.toArray(new String[0]);
                }
            }
        }
        return null;
    }

    /**
     * Lists all the files located at the provided directory, which are accepted by the provided filter.
     *
     * @param outPath    path to the directory
     * @param fileFilter filter to accept files to be listed
     * @return all the files located at the provided directory, which are accepted by the provided filter
     */
    public static File[] listFilesInDirectoryByFilter(String outPath, FileFilter fileFilter) {
        File[] result = null;
        if (outPath != null && !outPath.isEmpty()) {
            result = new File(outPath).listFiles(fileFilter);
        }
        if (result != null) {
            // Guarantee invariant order in all environments
            Arrays.sort(result, new CaseSensitiveFileComparator());
        }
        return result;
    }

    private static void listAllFiles(String dir, List<String> list) {
        File[] files = new File(dir).listFiles();
        if (files != null) {
            // Guarantee invariant order in all environments
            Arrays.sort(files, new CaseSensitiveFileComparator());
            for (File file : files) {
                if (file.isDirectory()) {
                    listAllFiles(file.getAbsolutePath(), list);
                } else {
                    list.add(file.getAbsolutePath());
                }
            }
        }
    }

    public static PrintWriter createPrintWriter(OutputStream output,
                                                String encoding) throws UnsupportedEncodingException {
        return new PrintWriter(new OutputStreamWriter(output, encoding));
    }

    public static OutputStream getBufferedOutputStream(String filename) throws FileNotFoundException {
        return new BufferedOutputStream(new FileOutputStream(filename));
    }

    public static OutputStream wrapWithBufferedOutputStream(OutputStream outputStream) {
        if (outputStream instanceof ByteArrayOutputStream || (outputStream instanceof BufferedOutputStream)) {
            return outputStream;
        } else {
            return new BufferedOutputStream(outputStream);
        }
    }

    /**
     * Creates a temporary file at the provided path.
     *
     * @param path path to the temporary file to be created. If it is a directory, then the temporary file
     *             will be created at this directory
     * @return the created temporary file
     * @throws IOException signals that an I/O exception has occurred
     */
    public static File createTempFile(String path) throws IOException {
        File tempFile = new File(path);
        if (tempFile.isDirectory()) {
            tempFile = File.createTempFile("pdf", null, tempFile);
        }
        return tempFile;
    }

    public static FileOutputStream getFileOutputStream(File tempFile) throws FileNotFoundException {
        return new FileOutputStream(tempFile);
    }

    public static InputStream getInputStreamForFile(String path) throws IOException {
        return new FileInputStream(path);
    }

    public static RandomAccessFile getRandomAccessFile(File tempFile) throws FileNotFoundException {
        return new RandomAccessFile(tempFile, "rw");
    }

    /**
     * Creates a directory at the provided path.
     *
     * @param outPath path to the directory to be created
     */
    public static void createDirectories(String outPath) {
        new File(outPath).mkdirs();
    }

    public static String getParentDirectoryUri(File file) throws MalformedURLException {
        String parentDirectoryUri = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            parentDirectoryUri = file != null ? Paths.get(file.getParent()).toUri().toURL().toExternalForm() : "";
        } else {
            parentDirectoryUri = file != null ? URI.create(file.getParent()).toURL().toExternalForm() : "";
        }
        return parentDirectoryUri;
    }

    /**
     * Deletes a file and returns whether the operation succeeded.
     * Note that only *files* are supported, not directories.
     *
     * @param file file to be deleted
     * @return true if file was deleted successfully, false otherwise
     */
    public static boolean deleteFile(File file) {
        return file.delete();
    }

    /**
     * Returns an URL of the parent directory for the resource.
     *
     * @param url of resource
     * @return parent directory path| the same path if a catalog`s url is passed;
     * @throws URISyntaxException if this URL is not formatted strictly according
     *                            to RFC2396 and cannot be converted to a URI.
     */
    public static String parentDirectory(URL url) throws URISyntaxException {
        return url.toURI().resolve(".").toString();
    }

    /**
     * Creates a temporary file.
     *
     * @param tempFilePrefix  the prefix of the copied file's name
     * @param tempFilePostfix the postfix of the copied file's name
     * @return the path to the copied file
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static File createTempFile(String tempFilePrefix, String tempFilePostfix) throws IOException {
        return File.createTempFile(tempFilePrefix, tempFilePostfix);
    }

    /**
     * Creates a temporary copy of a file.
     *
     * @param file            the path to the file to be copied
     * @param tempFilePrefix  the prefix of the copied file's name
     * @param tempFilePostfix the postfix of the copied file's name
     * @return the path to the copied file
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static String createTempCopy(String file, String tempFilePrefix, String tempFilePostfix)
            throws IOException {
        String replacementFilePath = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Path tempFile = Files.createTempFile(tempFilePrefix, tempFilePostfix);
                replacementFilePath = tempFile.toString();
                Path pathToPassedFile = Paths.get(file);
                Files.copy(pathToPassedFile, tempFile, StandardCopyOption.REPLACE_EXISTING);
            } else {
                File sourceFile = new File(file);
                File tempFile = File.createTempFile(tempFilePrefix, tempFilePostfix, IText722.getCacheDir());
                replacementFilePath = tempFile.toString();
                copyFileUsingChannel(sourceFile, tempFile);
            }
        } catch (IOException e) {
            if (null != replacementFilePath) {
                FileUtil.removeFiles(new String[]{replacementFilePath.toString()});
            }
            throw e;
        }
        return replacementFilePath.toString();
    }

    public static void copyFileUsingChannel(File source, File dest) throws IOException {
        FileChannel sourceChannel = null;
        FileChannel destChannel = null;
        try {
            sourceChannel = new FileInputStream(source).getChannel();
            destChannel = new FileOutputStream(dest).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } finally {
            sourceChannel.close();
            destChannel.close();
        }
    }

    /**
     * Creates a copy of a file.
     *
     * @param inputFile  the path to the file to be copied
     * @param outputFile the path, to which the passed file should be copied
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static void copy(String inputFile, String outputFile)
            throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Files.copy(Paths.get(inputFile), Paths.get(outputFile), StandardCopyOption.REPLACE_EXISTING);
        } else {
            copyFileUsingChannel(new File(inputFile), new File(outputFile));
        }
    }

    /**
     * Creates a temporary directory.
     *
     * @param tempFilePrefix the prefix of the temporary directory's name
     * @return the path to the temporary directory
     * @throws IOException signals that an I/O exception has occurred.
     */
    public static String createTempDirectory(String tempFilePrefix)
            throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Files.createTempDirectory(tempFilePrefix).toString();
        } else {
            File tempFile = File.createTempFile("temp", ".tmp", IText722.getCacheDir());
            String parent = tempFile.getParent();
            deleteFile(tempFile);
            return parent;
        }
    }

    /**
     * Removes all of the passed files.
     *
     * @param paths paths to files, which should be removed
     * @return {@code true} if all the files have been successfully removed, {@code false} otherwise
     */
    public static boolean removeFiles(String[] paths) {
        boolean allFilesAreRemoved = true;
        for (String path : paths) {
            try {
                if (null != path) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Files.delete(Paths.get(path));
                    } else {
                        deleteFile(new File(path));
                    }
                }
            } catch (Exception e) {
                allFilesAreRemoved = false;
            }
        }
        return allFilesAreRemoved;
    }

    private static class CaseSensitiveFileComparator implements Comparator<File> {
        @Override
        public int compare(File f1, File f2) {
            return f1.getPath().compareTo(f2.getPath());
        }
    }
}
