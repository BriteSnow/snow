/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.britesnow.snow.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URLEncoder;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {
	static private Logger logger = LoggerFactory.getLogger(FileUtil.class);

	static private Properties mimeTypeProperties = null;

	static {
		String mimeTypePath = "extramimetypes.properties";
		try {
			Properties props = new Properties();
			InputStream is = FileUtil.class.getResourceAsStream (mimeTypePath);
			props.load(is);
			mimeTypeProperties = props;
		} catch (Exception e) {
			logger.error("Could not load the property [" + mimeTypePath + "] because " + e.getMessage(),e);
		}
	}

	
	/**
	 * Return the list of Files (excluding folders) for this Folder for the given extensions. <br />
	 * Note 1: The list will be ordered by name via Java to make it consistent across file systems. <br />
	 * Note 2: The extensions are case-insensitive.
	 *  
	 * @param folder
	 * @param extensions 
	 */
	public static File[] getFiles(File folder,String... extensions){
	    final Set<String> exts = new HashSet<String>();
	    for (String ext : extensions){
	        exts.add(ext.toLowerCase());
	    }
	    File[] files = folder.listFiles(new FileFilter(){

            @Override
            public boolean accept(File file) {
                if (file.isDirectory()){
                    return false;
                }
                String name = file.getName();
                String ext = getFileNameAndExtension(name)[1];
                return exts.contains(ext.toLowerCase());
            }
	        
	    });
	    
	    Arrays.sort(files, new Comparator<File>() {

            @Override
            public int compare(File f1, File f2) {
                // TODO Auto-generated method stub
                return f1.getName().compareTo(f2.getName());
            }

        });
	    
	    return files;
	}
	
	
	/**
	 * Write a full string to a file. Use PrintWriter and BufferedReader
	 * 
	 * @param content
	 * @param dest
	 * @throws IOException
	 */
	public static void saveStringToFile(String content, File dest) throws IOException {
		Writer out = null;
		StringReader sr = null;
		PrintWriter pw = null;

		try {
			sr = new StringReader(content);
			BufferedReader br = new BufferedReader(sr);
			// FileWriter fw = new FileWriter(f);

			FileOutputStream fos = new FileOutputStream(dest);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
			BufferedWriter bw = new BufferedWriter(osw);
			out = new PrintWriter(bw);
			pw = (PrintWriter) out;
			String s;
			while ((s = br.readLine()) != null) {
				pw.println(s);
			}
			pw.flush();
		} finally {
			sr.close();
			out.close();
			try {
				pw.close();
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
	}

	/**
	 * Get the content of a file as a string. Return null if an exception occurs
	 * 
	 * @param f
	 * @return The content of the file as string. Null if the file is null or
	 *         does not exist. Null if exception occurs.
	 */
	public static String getFileContentAsString(File f) {

		if (f == null || !f.exists()) {
			return null;
		}
		StringWriter sw = new StringWriter();

		BufferedReader br = null;
		PrintWriter pw = null;
		try {
			FileInputStream fis = new FileInputStream(f);
			InputStreamReader ir = new InputStreamReader(fis, "UTF-8");
			br = new BufferedReader(ir);

			pw = new PrintWriter(sw);
			String s = null;
			while ((s = br.readLine()) != null) {
				pw.println(s);
			}
			pw.flush();
			// char[] buffer = new char[RouterServlet.BUFFER_SIZE];
			// int readLength = fr.read(buffer);
			// while (readLength != -1) {
			// w.write(buffer, 0, readLength);
			// readLength = fr.read(buffer);
			// }
		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		} finally {

			try {
				br.close();
				pw.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}

		}
		return sw.toString();
	}

	/* --------- ZIP Utils --------- */
	/**
	 * Return the ZipContent of a ZipEntry as a string.
	 * 
	 * @param zipFile
	 * @param entryName
	 * @return
	 * @throws Exception
	 */
	public static String getZipContentAsString(File zipFile, String entryName) throws Exception {
		StringWriter sw = new StringWriter();

		ZipFile zip = new ZipFile(zipFile);
		ZipEntry zipEntry = zip.getEntry(entryName);
		InputStream is = zip.getInputStream(zipEntry);
		InputStreamReader ir = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(ir);
		PrintWriter pw = new PrintWriter(sw);

		try {
			String s = null;
			while ((s = br.readLine()) != null) {
				pw.println(s);
			}
			pw.flush();
		} finally {

			br.close();
			pw.close();
			zip.close();

		}

		return sw.toString();
	}

	/**
	 * Clone a zip file and overwrite with the entryContentByEntryNameMap
	 * content. Note, added file might have an extra "line break". TODO: Fix the
	 * line break issue (an line break will be added on the overwritted string
	 * files)
	 * 
	 * @param orgZip
	 * @param destZip
	 * @param entryContentByEntryNameMap
	 * @throws IOException
	 */
	public static void cloneZipAndOverwriteStringContent(File orgZip, File destZip,
			Map<String, String> entryContentByEntryNameMap) throws IOException {
		byte[] buf = new byte[1024];

		ZipInputStream zin = null;
		ZipOutputStream out = null;
		try {
			// make sure the destZip folder exists, otherwise, create it.
			File destFolder = destZip.getParentFile();
			if (!destFolder.exists()) {
				destFolder.mkdirs();
			}

			zin = new ZipInputStream(new FileInputStream(orgZip));
			out = new ZipOutputStream(new FileOutputStream(destZip));

			/* --------- Write the unchanged entries --------- */
			ZipEntry entry = zin.getNextEntry();
			while (entry != null) {
				String name = entry.getName();

				// if the entry is not in the new content map, then add the
				// orgZip entry
				if (!entryContentByEntryNameMap.containsKey(name)) {
					// Add ZIP entry to output stream.
					out.putNextEntry(new ZipEntry(name));
					// Transfer bytes from the ZIP file to the output file
					int len;
					while ((len = zin.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
				}
				entry = zin.getNextEntry();
			}
			// Close the input stream (we do not need to read anymore)
			zin.close();
			/* --------- /Write the unchanged entries --------- */

			// Compress the files
			OutputStreamWriter osw = new OutputStreamWriter(out);
			for (String entryName : entryContentByEntryNameMap.keySet()) {
				String stringContent = entryContentByEntryNameMap.get(entryName);

				// Add ZIP entry to output stream.
				out.putNextEntry(new ZipEntry(entryName));

				osw.write(stringContent);
				osw.flush();

				// Complete the entry
				out.closeEntry();
			}
			out.close();
		} finally {

			if (zin != null) {
				zin.close();
			}
			if (out != null) {
				out.close();
			}

		}

	}

	// NOT TESTED YET. Source: http://snippets.dzone.com/posts/show/3468
	@SuppressWarnings("unused")
	private static void addFilesToExistingZip(File zipFile, File[] files) throws IOException {
		// get a temp file
		File tempFile = File.createTempFile(zipFile.getName(), null);
		// delete it, otherwise you cannot rename your existing zip to it.
		tempFile.delete();

		boolean renameOk = zipFile.renameTo(tempFile);
		if (!renameOk) {
			throw new RuntimeException("could not rename the file " + zipFile.getAbsolutePath() + " to "
					+ tempFile.getAbsolutePath());
		}
		byte[] buf = new byte[1024];

		ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));

		ZipEntry entry = zin.getNextEntry();
		while (entry != null) {
			String name = entry.getName();
			boolean notInFiles = true;
			for (File f : files) {
				if (f.getName().equals(name)) {
					notInFiles = false;
					break;
				}
			}
			if (notInFiles) {
				// Add ZIP entry to output stream.
				out.putNextEntry(new ZipEntry(name));
				// Transfer bytes from the ZIP file to the output file
				int len;
				while ((len = zin.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
			}
			entry = zin.getNextEntry();
		}
		// Close the streams
		zin.close();
		// Compress the files
		for (int i = 0; i < files.length; i++) {
			InputStream in = new FileInputStream(files[i]);
			// Add ZIP entry to output stream.
			out.putNextEntry(new ZipEntry(files[i].getName()));
			// Transfer bytes from the file to the ZIP file
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			// Complete the entry
			out.closeEntry();
			in.close();
		}
		// Complete the ZIP file
		out.close();
		tempFile.delete();
	}

	/* --------- /Zip Utils --------- */

	/**
	 * Change the fileName with a optional fileNameSuffix (which will be added
	 * before the extension) and a optional new Extension.<br />
	 * <br />
	 * 
	 * Note: The extension is found by using lastIndexOf('.')
	 * 
	 * @param fileName
	 *            The file name including extension (can include path as well)
	 * @param fileNameSuffix
	 *            If not null, will be added before the extension.
	 * @param newExtension
	 *            If not null, will replace the fileName extension. If null, the
	 *            fileName extension will be kept.
	 * @return
	 */
	public static String changeFileName(String fileName, String fileNameSuffix, String newExtension) {
		String[] nameAndExt = getFileNameAndExtension(fileName);
		StringBuilder sb = new StringBuilder(nameAndExt[0]);
		if (fileNameSuffix != null) {
			sb.append(fileNameSuffix);
		}
		if (newExtension != null) {
			sb.append(newExtension);
		} else {
			sb.append(nameAndExt[1]);
		}
		return sb.toString();
	}

	/**
	 * Return a String[2] with the first element being the file path/name
	 * (before the last '.'), and the second element as the extension (from the
	 * last '.' on). If no extension, return an empty string. <br />
	 * <br />
	 * 
	 * Note: The extension is found by using lastIndexOf('.')
	 * 
	 * @param fullFileName
	 * @return
	 */
	public static String[] getFileNameAndExtension(String fullFileName) {
		String[] fileNameAndExtension = new String[2];

		int lastDotIdx = fullFileName.lastIndexOf('.');

		if (lastDotIdx != -1) {
			fileNameAndExtension[0] = fullFileName.substring(0, lastDotIdx);
			fileNameAndExtension[1] = fullFileName.substring(lastDotIdx);
		} else {
			fileNameAndExtension[0] = fullFileName;
			fileNameAndExtension[1] = "";
		}

		return fileNameAndExtension;
	}

	/**
	 * Return a String[2] with the first element being the file path (up to and
	 * including the last '/'), and the second element as the file name
	 * (including extension). If the fullFileName has no path, then [0] is empty
	 * string, and [1] if the file name <br />
	 * 
	 * Note: Supports only '/' file deliminator.
	 * 
	 * @param fullFileName
	 * @return
	 */
	public static String[] getFilePathAndName(String fullFileName) {
		String[] filePathAndName = new String[2];
		int lastSlashIdx = fullFileName.lastIndexOf('/');
		if (lastSlashIdx != -1) {
			filePathAndName[0] = fullFileName.substring(0, lastSlashIdx + 1);
			filePathAndName[1] = fullFileName.substring(lastSlashIdx + 1);
		} else {
			filePathAndName[0] = "";
			filePathAndName[1] = fullFileName;
		}
		return filePathAndName;
	}
	
	
	public static String encodeFileName(String fullFileName){
        // NOTE: this should be generalized.
        String[] filePathAndName = FileUtil.getFilePathAndName(fullFileName);
        String href = fullFileName;
        try {
            // NOTE: we need to also replace the "+" by "%20" otherwise the file name will include the "+"
            filePathAndName[1] = URLEncoder.encode(filePathAndName[1], "UTF-8").replace("+", "%20");
            href = new StringBuilder(filePathAndName[0]).append(filePathAndName[1]).toString();
            return href;
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage());
            return href;
        }	    
	}

	/**
	 * Split in subfolders any numbers '[' ']'. The path "firms" is just an
	 * example.
	 * 
	 * NOTE: Limitation support only one [..] per fullPath (for now)
	 * 
	 * "/firms/[1]" > "/firms/01" "/firms/[10]" > "/firms/10"
	 * "/firms/[101] > "/firms/01/10"
	 * 
	 * @param fullPath
	 *            a path, with and optional "[0-9]" id fagments, that will be
	 *            split in sub-folders
	 * @return
	 */
	public static String splitIdFolder2(String fullPath, char fileSeparatorChar) {
		if (fullPath == null) {
			return null;
		}

		int idxStartBracket = fullPath.indexOf('[');
		int idxEndBracket = fullPath.indexOf(']');

		if (idxStartBracket == -1 || idxEndBracket == -1) {

			return fullPath;

		} else {
			String prefix = fullPath.substring(0, idxStartBracket);
			String suffix = (fullPath.length() > idxEndBracket + 1) ? fullPath.substring(idxEndBracket + 1) : "";

			StringBuilder sb = new StringBuilder(prefix);

			String pathId = fullPath.substring(idxStartBracket + 1, idxEndBracket);

			// add "0" prefix if the pathId does not have a pair number of
			// character
			int idxPathId = 0;
			if ((pathId.length() % 2) != 0) {
				sb.append('0');
				sb.append(pathId.charAt(0));
				idxPathId++;

				// add separator only if it is not the last path section
				if (idxPathId < pathId.length()) {
					sb.append(fileSeparatorChar);
				}
			}

			while (idxPathId < pathId.length()) {
				sb.append(pathId.charAt(idxPathId));
				idxPathId++;
				sb.append(pathId.charAt(idxPathId));
				idxPathId++;

				// add separator only if it is not the last path section
				if (idxPathId < pathId.length()) {
					sb.append(fileSeparatorChar);
				}
			}

			if (suffix.length() > 0 && suffix.charAt(0) == fileSeparatorChar) {
				sb.append("_dir");
				sb.append(suffix);
			} else {
				sb.append(suffix);
			}

			return sb.toString();
		}

	}

	/**
	 * Some extra mime type that might not be available in the servletContext. <br />
	 * Note this is not all the mime type, and you should first try to get the mimeType from the servletContext. <br />
	 * This is just a fall back in the case the server does not have the latest mimeType such as office .docx, ...
	 * 
	 * @param fileName Full file name with the extension. If null, this methods return null.
	 * @return the found mimeType or null if not found (or if fileName is null) 
	 */
	public static String getExtraMimeType(String fileName){
    	if (fileName != null){
    		String ext = getFileNameAndExtension(fileName)[1];
    		return mimeTypeProperties.getProperty(ext);
    	}
    	return null;
    }
}
