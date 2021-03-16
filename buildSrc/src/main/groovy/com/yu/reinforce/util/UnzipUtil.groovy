package com.yu.reinforce.util

import java.util.zip.ZipEntry
import java.util.zip.ZipFile


/**
 * Unzip the file by keyword.
 *
 * @param zipFile The ZIP file.
 * @param destDir The destination directory.
 * @param keyword The keyboard.
 * @return the unzipped files
 * @throws IOException if unzip unsuccessfully
 */
static List<File> unzipFileByKeyword(final File zipFile, final File destDir, final String keyword)
        throws IOException {
    if (zipFile == null || destDir == null) return null;
    List<File> files = new ArrayList<>();
    ZipFile zip = new ZipFile(zipFile);
    Enumeration<?> entries = zip.entries();
    try {
        if (keyword == null || keyword == "") {
            while (entries.hasMoreElements()) {
                ZipEntry entry = ((ZipEntry) entries.nextElement());
                String entryName = entry.getName().replace("\\", "/");
                if (entryName.contains("../")) {
                    continue;
                }
                if (!unzipChildFile(destDir, files, zip, entry, entryName)) return files;
            }
        } else {
            while (entries.hasMoreElements()) {
                ZipEntry entry = ((ZipEntry) entries.nextElement());
                String entryName = entry.getName().replace("\\", "/");
                if (entryName.contains("../")) {
                    continue;
                }
                if (entryName.contains(keyword)) {
                    if (!unzipChildFile(destDir, files, zip, entry, entryName)) return files;
                }
            }
        }
    } finally {
        zip.close();
    }
    return files;
}

private static boolean unzipChildFile(final File destDir,
                                      final List<File> files,
                                      final ZipFile zip,
                                      final ZipEntry entry,
                                      final String name) throws IOException {
    File file = new File(destDir, name);
    files.add(file);
    if (entry.isDirectory()) {
        return createOrExistsDir(file);
    } else {
        if (!createOrExistsFile(file)) return false;
        InputStream ins = null;
        OutputStream out = null;
        try {
            ins = new BufferedInputStream(zip.getInputStream(entry));
            out = new BufferedOutputStream(new FileOutputStream(file));
            byte[] buffer = new byte[8192];
            int len;
            while ((len = ins.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } finally {
            if (ins != null) {
                ins.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }
    return true;
}

private static boolean createOrExistsFile(final File file) {
    if (file == null) return false;
    if (file.exists()) return file.isFile();
    if (!createOrExistsDir(file.getParentFile())) return false;
    try {
        return file.createNewFile();
    } catch (IOException e) {
        e.printStackTrace();
        return false;
    }
}

private static boolean createOrExistsDir(final File file) {
    return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
}

