package com.example.oracleadmin.controller;

import com.example.oracleadmin.dto.ConnParam;
import com.example.oracleadmin.service.ConnectionManagementService;
import com.example.oracleadmin.service.DataGuardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.util.Map;

@RestController
@RequestMapping("/api/dataguard")
public class DataGuardController {

    @Autowired
    private DataGuardService dataGuardService;
    @Autowired
    private ConnectionManagementService connectionManagementService;

    @PostMapping("/status")
    public Map<String, String> getStatus(
            @RequestParam String ip,
            @RequestParam String port,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String serviceName,
            @RequestParam String role
            ) {
        ConnParam conn = new ConnParam(password, ip, port, username, serviceName, role);
        try (Connection connection = connectionManagementService.createConnection(conn)) {
            return dataGuardService.getDatabaseStatus(connection);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving status: " + e.getMessage(), e);
        }
    }

    /*@PostMapping("/switchover")
    public String performSwitchover(
            @RequestParam String url,
            @RequestParam String username,
            @RequestParam String password) {
        try (Connection connection = dataGuardService.createConnection(url, username, password)) {
            return dataGuardService.performSwitchover(connection);
        } catch (Exception e) {
            throw new RuntimeException("Error performing switchover: " + e.getMessage(), e);
        }
    }

    @PostMapping("/failover")
    public String performFailover(
            @RequestParam String url,
            @RequestParam String username,
            @RequestParam String password) {
        try (Connection connection = dataGuardService.createConnection(url, username, password)) {
            return dataGuardService.performFailover(connection);
        } catch (Exception e) {
            throw new RuntimeException("Error performing failover: " + e.getMessage(), e);
        }
    }*/
}

