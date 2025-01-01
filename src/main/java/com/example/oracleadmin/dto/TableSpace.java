package com.example.oracleadmin.dto;

public class TableSpace {
    private String name;
    private String dataFilePath;
    private String size;
    private Boolean autoExtend;
    private String incrementSize;
    private String maxSize;

    public TableSpace(String tableSpaceName, String dataFilePath, String size, Boolean autoExtend, String incrementSize, String maxSize) {
        this.name = tableSpaceName;
        this.dataFilePath = dataFilePath;
        this.size = size;
        this.autoExtend = autoExtend;
        this.incrementSize = incrementSize;
        this.maxSize = maxSize;
    }

    public String getName() {
        return name;
    }

    public void setTableSpaceName(String tableSpaceName) {
        this.name = tableSpaceName;
    }

    public String getDataFilePath() {
        return dataFilePath;
    }

    public void setDataFilePath(String dataFilePath) {
        this.dataFilePath = dataFilePath;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Boolean getAutoExtend() {
        return autoExtend;
    }

    public void setAutoExtend(Boolean autoExtend) {
        this.autoExtend = autoExtend;
    }

    public String getIncrementSize() {
        return incrementSize;
    }

    public void setIncrementSize(String incrementSize) {
        this.incrementSize = incrementSize;
    }

    public String getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(String maxSize) {
        this.maxSize = maxSize;
    }
}
