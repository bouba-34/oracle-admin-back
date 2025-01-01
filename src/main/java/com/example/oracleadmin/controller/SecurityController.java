package com.example.oracleadmin.controller;

import com.example.oracleadmin.dto.ConnParam;
import com.example.oracleadmin.service.SecurityManagementService;
import com.example.oracleadmin.service.ConnectionManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@RestController
@RequestMapping("/api/security")
public class SecurityController {

    @Autowired
    private ConnectionManagementService connectionManagementService;

    @Autowired
    private SecurityManagementService securityManagementService;

    @PostMapping("/tde/configure")
    public ResponseEntity<Map<String, String>> configureTDE(
            @RequestParam String tablespaceName,
            @RequestParam String encryptionAlgorithm,
            @RequestBody ConnParam connParam
            ) {

        System.out.println(tablespaceName);

        ConnParam conn = new ConnParam(connParam.getPassword(), connParam.getIp(), connParam.getPort(), connParam.getUsername(), connParam.getServiceName(), connParam.getRole());

        try (Connection connection = connectionManagementService.createConnection(conn)) {
            securityManagementService.configureTDEPolicy(connection, tablespaceName, encryptionAlgorithm);
            return ResponseEntity.ok(Map.of("message", "TDE configured successfully."));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error configuring TDE: " + e.getMessage()));
        }
    }

    @PostMapping("/audit/enable")
    public ResponseEntity<Map<String, String>> enableAuditing(
            @RequestParam String ip,
            @RequestParam String port,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String serviceName,
            @RequestParam String role) {

        ConnParam conn = new ConnParam(password, ip, port, username, serviceName, role);

        try (Connection connection = connectionManagementService.createConnection(conn)) {
            //securityManagementService.enableAuditing(connection);
            return ResponseEntity.ok(Map.of("message", "Auditing enabled successfully."));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error enabling auditing: " + e.getMessage()));
        }
    }

    @PostMapping("/audit/disable")
    public ResponseEntity<Map<String, String>> disableAuditing(
            @RequestParam String ip,
            @RequestParam String port,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String serviceName,
            @RequestParam String role) {

        ConnParam conn = new ConnParam(password, ip, port, username, serviceName, role);

        try (Connection connection = connectionManagementService.createConnection(conn)) {
            //securityManagementService.disableAuditing(connection);
            return ResponseEntity.ok(Map.of("message", "Auditing disabled successfully."));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error disabling auditing: " + e.getMessage()));
        }
    }

    @PostMapping("/vpd/add-policy")
    public ResponseEntity<Map<String, String>> addVPDPolicy(
            @RequestParam String ip,
            @RequestParam String port,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String serviceName,
            @RequestParam String role,
            @RequestParam String policyName,
            @RequestParam String tableName,
            @RequestParam String predicate) {

        ConnParam conn = new ConnParam(password, ip, port, username, serviceName, role);

        try (Connection connection = connectionManagementService.createConnection(conn)) {
            //securityManagementService.addVPDPolicy(connection, policyName, tableName, predicate);
            return ResponseEntity.ok(Map.of("message", "VPD policy added successfully."));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error adding VPD policy: " + e.getMessage()));
        }
    }

    @DeleteMapping("/vpd/remove-policy")
    public ResponseEntity<Map<String, String>> removeVPDPolicy(
            @RequestParam String ip,
            @RequestParam String port,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String serviceName,
            @RequestParam String role,
            @RequestParam String policyName,
            @RequestParam String tableName) {

        ConnParam conn = new ConnParam(password, ip, port, username, serviceName, role);

        try (Connection connection = connectionManagementService.createConnection(conn)) {
            //securityManagementService.removeVPDPolicy(connection, policyName, tableName);
            return ResponseEntity.ok(Map.of("message", "VPD policy removed successfully."));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error removing VPD policy: " + e.getMessage()));
        }
    }

    @GetMapping("/vpd/list-policies")
    public ResponseEntity<?> listVPDPolicies(
            @RequestParam String ip,
            @RequestParam String port,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String serviceName,
            @RequestParam String role) {

        ConnParam conn = new ConnParam(password, ip, port, username, serviceName, role);

        try (Connection connection = connectionManagementService.createConnection(conn)) {
            var policies = securityManagementService.listVPDPolicies(connection);
            return ResponseEntity.ok(policies);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error listing VPD policies: " + e.getMessage()));
        }
    }
}
