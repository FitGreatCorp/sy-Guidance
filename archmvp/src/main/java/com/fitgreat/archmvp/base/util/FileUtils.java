package com.fitgreat.archmvp.base.util;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * 文件管理类
 * <p>
 * Created by seraphzxz on 2016/7/15.
 */
public class FileUtils {
    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                fs.flush();
                fs.close();
                inStream.close();
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        }

    }

    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            file.delete();
        }
    }


    /**
     * 复制整个文件夹内容
     *
     * @param oldPath String 原文件路径 如：c:/fqf
     * @param newPath String 复制后路径 如：f:/fqf/ff
     * @return boolean
     */
    public void copyFolder(String oldPath, String newPath) {

        try {
            (new File(newPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹
            File a = new File(oldPath);
            String[] file = a.list();
            File temp = null;
            for (int i = 0; i < file.length; i++) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file[i]);
                } else {
                    temp = new File(oldPath + File.separator + file[i]);
                }

                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + "/" +
                            (temp.getName()));
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if (temp.isDirectory()) {//如果是子文件夹
                    copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
                }
            }
        } catch (Exception e) {
            System.out.println("复制整个文件夹内容操作出错");
            e.printStackTrace();
        }
    }

    /**
     * @param path 文件夹路径
     */
    public static void isExist(String path) {
        File file = new File(path);
        //判断文件夹是否存在,如果不存在则创建文件夹
        if (!file.exists()) {
            file.mkdir();
        }
    }

    //path 格式  code  文件名字   format  后缀名
    public static boolean isFile(String path, String code, String format) {
        String spath = Environment.getExternalStorageDirectory() + path + code + format;
        try {
            File f = new File(spath);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * 压缩文件,文件夹
     *
     * @param srcFilePath 要压缩的文件/文件夹名字
     * @param zipFilePath 指定压缩的目的和名字
     * @throws Exception
     */
    public static void zipFolder(String srcFilePath, String zipFilePath) throws Exception {
        //创建Zip包
        java.util.zip.ZipOutputStream outZip =
                new java.util.zip.ZipOutputStream(new FileOutputStream(zipFilePath));

        //打开要输出的文件
        File file = new File(srcFilePath);

        //压缩
        zipFiles(file.getParent() + File.separator, file.getName(), outZip);

        //完成,关闭
        outZip.finish();
        outZip.close();

    }//end of func

    /**
     * 压缩文件
     *
     * @param folderPath
     * @param filePath
     * @param zipOut
     * @throws Exception
     */
    private static void zipFiles(String folderPath, String filePath,
                                 java.util.zip.ZipOutputStream zipOut) throws Exception {
        if (zipOut == null) {
            return;
        }
        File file = new File(folderPath + filePath);

        //判断是不是文件
        if (file.isFile()) {
            java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(filePath);
            FileInputStream inputStream = new FileInputStream(file);
            zipOut.putNextEntry(zipEntry);

            int len;
            byte[] buffer = new byte[4096];

            while ((len = inputStream.read(buffer)) != -1) {
                zipOut.write(buffer, 0, len);
            }

            zipOut.closeEntry();
        } else {
            //文件夹的方式,获取文件夹下的子文件
            String[] fileList = file.list();

            //如果没有子文件, 则添加进去即可
            if (fileList.length <= 0) {
                java.util.zip.ZipEntry zipEntry =
                        new java.util.zip.ZipEntry(filePath + File.separator);
                zipOut.putNextEntry(zipEntry);
                zipOut.closeEntry();
            }

            //如果有子文件, 遍历子文件
            for (int i = 0; i < fileList.length; i++) {
                zipFiles(folderPath, filePath + File.separator + fileList[i], zipOut);
            }//end of for
        }//end of if
    }//end of func

    // 将字符串写入到文本文件中
    public void writeTxtToFile(String strcontent, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);
        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

    // 生成文件
    public File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    // 生成文件夹
    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
    }

    public static ArrayList<String> getFileNameList(File path) {
        ArrayList<String> FileNameList = new ArrayList<>();
        File[] files = path.listFiles();
        if (null != files) {
            for (int i = 0; i < files.length; i++) {
                FileNameList.add(files[i].getName());
            }
        }
        return FileNameList;
    }

    public static String chageFileName(String filePath) {
        File file = new File(filePath);
        //前面路径必须一样才能修改成功
        String path = filePath.substring(0, filePath.lastIndexOf("/") + 1) + System.currentTimeMillis() + filePath.substring(filePath.lastIndexOf("."));
        File newFile = new File(path);
        file.renameTo(newFile);
        return newFile.getAbsolutePath();
    }


    // storage, G M K B
    public static String convertStorage(String path) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        long size = new File(path).length();
        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else {
            return String.format("%d B", size);
        }
    }


    /**
     * 从TXT文件中读取String
     *
     * @param filepath
     * @return
     */
    public static String readTxt(String filepath) {
        String text = "";
        File file = new File(filepath);
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            text = sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }

    /**
     * 读取本地文件二进制字节
     */
    public static byte[] readLocalFile(String fileName) throws IOException {
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
        File file = new File(fileName);
        if (file == null || !file.exists()) {
            return null;
        }
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] data = toByteArray(fileInputStream);
        fileInputStream.close();
        return data;
    }

    public static byte[] toByteArray(InputStream in) throws IOException {
        if (in == null) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 4];
        int n = 0;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }

}