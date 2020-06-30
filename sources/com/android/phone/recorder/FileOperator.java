package com.android.phone.recorder;

import android.content.Context;
import android.util.Log;
import com.android.phone.recorder.FileInfo.ItemComparator;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class FileOperator {
    private static ItemComparator mItemComparator = new ItemComparator();

    private static class RecordFilenameFilter implements FilenameFilter {
        private RecordFilenameFilter() {
        }

        public boolean accept(File dir, String filename) {
            if (filename == null || !filename.toLowerCase(Locale.US).endsWith("amr")) {
                return false;
            }
            return true;
        }
    }

    public static ArrayList<FileInfo> getRecordFileList(Context context, String scanPath) {
        ArrayList<FileInfo> fileinfoLists = new ArrayList<>();
        initRecordFolder(Utils.getInternalStorage(), scanPath, fileinfoLists);
        initRecordFolder(Utils.getExternalStorage(), scanPath, fileinfoLists);
        return fileinfoLists;
    }

    public static ArrayList<FileInfo> initRecordFolder(String path, String scanPath, ArrayList<FileInfo> fileinfoLists) {
        ArrayList<FileInfo> arrayList = fileinfoLists;
        try {
            StringBuilder sb = new StringBuilder();
            try {
                sb.append(path);
                sb.append("/");
                try {
                    sb.append(scanPath);
                    File dir = new File(sb.toString());
                    if ((!dir.exists() || !dir.isDirectory()) && !dir.mkdirs()) {
                        Log.v("FileOperator", "initRecordFolder : can create the folder.");
                        return null;
                    }
                    File[] files = dir.listFiles(new RecordFilenameFilter());
                    if (files == null) {
                        return null;
                    }
                    for (File file : files) {
                        String absolutePath = file.getAbsolutePath();
                        String name = file.getName();
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(file.length());
                        sb2.append("");
                        FileInfo fileInfo = new FileInfo(absolutePath, name, sb2.toString(), file.lastModified());
                        FileInfo fileInfo2 = fileInfo;
                        if (file.length() > 0) {
                            arrayList.add(fileInfo2);
                        }
                    }
                    Collections.sort(arrayList, mItemComparator);
                    return arrayList;
                } catch (ClassCastException e) {
                    Log.d("FileOperator", "initRecordFolder : ClassCastException");
                    return null;
                } catch (Exception e2) {
                    Log.d("FileOperator", "initRecordFolder : Exception");
                    return null;
                }
            } catch (ClassCastException e3) {
                String str = scanPath;
                Log.d("FileOperator", "initRecordFolder : ClassCastException");
                return null;
            } catch (Exception e4) {
                String str2 = scanPath;
                Log.d("FileOperator", "initRecordFolder : Exception");
                return null;
            }
        } catch (ClassCastException e5) {
            String str3 = path;
            String str4 = scanPath;
            Log.d("FileOperator", "initRecordFolder : ClassCastException");
            return null;
        } catch (Exception e6) {
            String str5 = path;
            String str22 = scanPath;
            Log.d("FileOperator", "initRecordFolder : Exception");
            return null;
        }
    }

    public static void moveFileDir(Context context) {
        String InternalStorage = Utils.getSdCardRootPath(context, false);
        String ExternalStorage = Utils.getSdCardRootPath(context, true);
        if (InternalStorage != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("moveFileDir InternalStorage = ");
            sb.append(InternalStorage);
            Log.d("FileOperator", sb.toString());
            moveFileDir(InternalStorage);
        }
        if (ExternalStorage != null) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("moveFileDir ExternalStorage = ");
            sb2.append(ExternalStorage);
            Log.d("FileOperator", sb2.toString());
            moveFileDir(ExternalStorage);
        }
    }

    public static void moveFileDir(String path) {
        StringBuilder sb = new StringBuilder();
        sb.append(path);
        sb.append(File.separator);
        sb.append("record");
        sb.append(File.separator);
        String srcPath = sb.toString();
        StringBuilder sb2 = new StringBuilder();
        sb2.append(path);
        sb2.append(File.separator);
        sb2.append(Utils.RECORD_FOLDER);
        sb2.append(File.separator);
        if (!moveFileDirAndRename(srcPath, sb2.toString())) {
            StringBuilder sb3 = new StringBuilder();
            sb3.append(path);
            sb3.append("move fail!");
            Log.e("FileOperator", sb3.toString());
        }
    }

    private static boolean moveFileDirAndRename(String srcPath, String dstPath) {
        boolean rt = false;
        File dstFile = new File(dstPath);
        if (dstFile.exists() || dstFile.mkdirs()) {
            if (isEmpty(dstFile)) {
                moveFileByRenameto(srcPath, dstPath);
            } else {
                File[] oldFiles = traverseFile(srcPath);
                if (oldFiles == null) {
                    return false;
                }
                for (File file : oldFiles) {
                    if (file != null) {
                        if (file.isDirectory()) {
                            String path = file.getPath();
                            StringBuilder sb = new StringBuilder();
                            sb.append(dstPath);
                            sb.append(file.getName());
                            sb.append(File.separator);
                            rt = moveFileDirAndRename(path, sb.toString());
                        } else {
                            boolean isExist = isFileExist(file.getName(), dstPath);
                            if (!file.isFile() || !isExist) {
                                String path2 = file.getPath();
                                StringBuilder sb2 = new StringBuilder();
                                sb2.append(dstPath);
                                sb2.append(file.getName());
                                rt = moveFileByRenameto(path2, sb2.toString());
                            } else {
                                int i = 1;
                                while (file.isFile() && isExist) {
                                    String filename = fileRename(file.getName(), i);
                                    i++;
                                    isExist = isFileExist(filename, dstPath);
                                    if (!isExist) {
                                        String path3 = file.getPath();
                                        StringBuilder sb3 = new StringBuilder();
                                        sb3.append(dstPath);
                                        sb3.append(filename);
                                        rt = moveFileByRenameto(path3, sb3.toString());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            deleteEmptyFolder(srcPath);
            return rt;
        }
        Log.e("FileOperator", "mkdir failed");
        return false;
    }

    private static boolean isFileExist(String filename, String dstPath) {
        StringBuilder sb = new StringBuilder();
        sb.append(dstPath);
        sb.append(filename);
        return new File(sb.toString()).exists();
    }

    private static boolean moveFileByRenameto(String oldPath, String newPath) {
        boolean rename = new File(oldPath).renameTo(new File(newPath));
        if (!rename) {
            Log.e("FileOperator", "rename byPath failed");
        }
        return rename;
    }

    private static boolean isEmpty(File file) {
        String[] list = file.list();
        if (!file.exists() || !file.isDirectory() || list == null || list.length != 0) {
            return false;
        }
        return true;
    }

    private static File[] traverseFile(String path) {
        return new File(path).listFiles();
    }

    private static String fileRename(String fileName, int i) {
        StringBuffer newFileName = new StringBuffer();
        if (fileName != null) {
            String[] fileNames = fileName.split("\\.");
            if (fileNames.length >= 2) {
                StringBuilder sb = new StringBuilder();
                int length = fileNames.length - 2;
                sb.append(fileNames[length]);
                sb.append("_");
                sb.append(i);
                fileNames[length] = sb.toString();
            } else if (fileNames.length == 1) {
                StringBuilder sb2 = new StringBuilder();
                int length2 = fileNames.length - 1;
                sb2.append(fileNames[length2]);
                sb2.append("_");
                sb2.append(i);
                fileNames[length2] = sb2.toString();
            }
            for (int size = fileNames.length; size > 0; size--) {
                if (size > 1) {
                    newFileName.append(fileNames[fileNames.length - size]);
                    newFileName = newFileName.append(".");
                } else {
                    newFileName = newFileName.append(fileNames[fileNames.length - size]);
                }
            }
        }
        return newFileName.toString();
    }

    private static void deleteEmptyFolder(String path) {
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if ((childFiles == null || childFiles.length == 0) && !file.delete()) {
                Log.e("FileOperator", "delete failed");
            }
        }
    }
}
