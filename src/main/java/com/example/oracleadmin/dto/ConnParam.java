package com.example.oracleadmin.dto;

public class ConnParam {
    private String ip;
    private String port;

    private String username;
    private String password;

    private String serviceName;
    private String role;

    public ConnParam(String password, String ip, String port, String username, String serviceName, String role) {
        this.password = password;
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.serviceName = serviceName;
        this.role = role;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "ConnParam{" +
                "ip='" + ip + '\'' +
                ", port='" + port + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
