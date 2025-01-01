package com.example.oracleadmin.controller;

import com.example.oracleadmin.dto.ConnParam;
import com.example.oracleadmin.entity.UserConnection;
import com.example.oracleadmin.service.ConnectionManagementService;
import com.example.oracleadmin.service.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    ConnectionManagementService connectionManagementService;

    @Autowired
    UserManagementService userManagementService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createUser(
            @RequestBody ConnParam connParam,
            @RequestParam String newUsername,
            @RequestParam String newPassword,
            @RequestParam String newRole,
            @RequestParam String defaultTablespace,
            @RequestParam String temporaryTablespace,
            @RequestParam String quota) {

        ConnParam conn = new ConnParam(connParam.getPassword(), connParam.getIp(), connParam.getPort(), connParam.getUsername(), connParam.getServiceName(), connParam.getRole());

        try (Connection connection = connectionManagementService.createConnection(conn)) {
            userManagementService.createUser(connection, newUsername, newPassword,newRole, quota, defaultTablespace, temporaryTablespace);
            Map<String, String> response = Map.of("message", "User created successfully");
            return ResponseEntity.ok(response);

        } catch (SQLException e) {
            Map<String, String> errorResponse = Map.of("error", "Error creating user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);

        } catch (Exception e) {
            Map<String, String> errorResponse = Map.of("error", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers(
            @RequestParam String ip,
            @RequestParam String port,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String serviceName,
            @RequestParam String role) {

        ConnParam conn = new ConnParam(password, ip, port, username, serviceName, role);

        try (Connection connection = connectionManagementService.createConnection(conn)) {
            List<Map<String, Object>> users = userManagementService.getAllUsers(connection);
            return ResponseEntity.ok(users);

        } catch (SQLException e) {
            Map<String, String> errorResponse = Map.of("error", "Database error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);

        } catch (Exception e) {
            Map<String, String> errorResponse = Map.of("error", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteUser(
            @RequestParam String ip,
            @RequestParam String port,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String serviceName,
            @RequestParam String role,
            @RequestParam String targetUsername) {

        ConnParam conn = new ConnParam(password, ip, port, username, serviceName, role);

        try (Connection connection = connectionManagementService.createConnection(conn)) {
            userManagementService.dropUser(connection, targetUsername);
            Map<String, String> response = Map.of("message", "User deleted successfully");
            return ResponseEntity.ok(response);

        } catch (SQLException e) {
            Map<String, String> errorResponse = Map.of("error", "Error deleting user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);

        } catch (Exception e) {
            Map<String, String> errorResponse = Map.of("error", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, String>> updateUser(
            @RequestParam String ip,
            @RequestParam String port,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String serviceName,
            @RequestParam String role,
            @RequestParam String targetUsername,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String newRole,
            @RequestParam(required = false) String dafaultTablespace,
            @RequestParam(required = false) String temporaryTablespace,
            @RequestParam(required = false) String quota) {

        ConnParam conn = new ConnParam(password, ip, port, username, serviceName, role);

        try (Connection connection = connectionManagementService.createConnection(conn)) {
            userManagementService.updateUser(connection, targetUsername, newPassword,newRole, quota, dafaultTablespace, temporaryTablespace);
            Map<String, String> response = Map.of("message", "User updated successfully");
            return ResponseEntity.ok(response);

        } catch (SQLException e) {
            Map<String, String> errorResponse = Map.of("error", "Error updating user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);

        } catch (Exception e) {
            Map<String, String> errorResponse = Map.of("error", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
