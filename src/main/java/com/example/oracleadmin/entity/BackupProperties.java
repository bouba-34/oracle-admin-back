package com.example.oracleadmin.entity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BackupProperties {

    @Value("${backup.docker-container}")
    private String dockerContainer;

    @Value("${backup.oracle-user}")
    private String oracleUser;

    @Value("${backup.oracle-password}")
    private String oraclePassword;

    @Value("${backup.oracle-host}")
    private String oracleHost;

    @Value("${backup.oracle-port}")
    private String oraclePort;

    @Value("${backup.oracle-service}")
    private String oracleService;

    @Value("${backup.path}")
    private String backupPath;

    // Getters
    public String getDockerContainer() { return dockerContainer; }
    public String getOracleUser() { return oracleUser; }
    public String getOraclePassword() { return oraclePassword; }
    public String getOracleHost() { return oracleHost; }
    public String getOraclePort() { return oraclePort; }
    public String getOracleService() { return oracleService; }
    public String getBackupPath() { return backupPath; }
}

