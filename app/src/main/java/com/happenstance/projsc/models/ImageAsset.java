package com.happenstance.projsc.models;

/**
 * Author CodeBoy722
 *
 * Custom class for holding data of images on the device external storage
 */
public class ImageAsset {

    private String fileName;
    private String filePath;
    private long longDateModified;
    private boolean isFolder;

    public ImageAsset(String fileName, String filePath, long longDateModified, boolean isFolder) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.longDateModified = longDateModified;
        this.isFolder = isFolder;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getLongDateModified() {
        return longDateModified;
    }

    public void setLongDateModified(long longDateModified) {
        this.longDateModified = longDateModified;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFolder(boolean folder) {
        isFolder = folder;
    }

    //    public Boolean getSelected() {
//        return selected;
//    }
//
//    public void setSelected(Boolean selected) {
//        this.selected = selected;
//    }
}