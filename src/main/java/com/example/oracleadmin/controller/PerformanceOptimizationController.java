package com.example.oracleadmin.controller;
import com.example.oracleadmin.dto.ConnParam;
import com.example.oracleadmin.service.ConnectionManagementService;
import com.example.oracleadmin.service.PerformanceOptimizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/performance")
public class PerformanceOptimizationController {

    @Autowired
    private PerformanceOptimizationService performanceService;

    @Autowired
    private ConnectionManagementService connectionManagementService;

    // Connexion à la base de données Oracle
    private Connection connectToDatabase(String ip, String port, String username, String password, String serviceName) throws Exception {
        String url = "jdbc:oracle:thin:@" + ip + ":" + port + ":" + serviceName;
        return DriverManager.getConnection(url, username, password);
    }

    // Endpoint pour récupérer les requêtes lentes
    @GetMapping("/slow-queries")
    public ResponseEntity<?> getSlowQueries(
            @RequestParam String ip,
            @RequestParam String port,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String role,
            @RequestParam String serviceName) {
        ConnParam conn = new ConnParam(password, ip, port, username, serviceName, role);
        try (Connection connection = connectionManagementService.createConnection(conn)) {
            List<Map<String, Object>> slowQueries = performanceService.getSlowQueries(connection);
            return ResponseEntity.ok(slowQueries);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // Endpoint pour exécuter le SQL Tuning Advisor sur une requête lente
    @GetMapping("/tune-query")
    public ResponseEntity<?> runSQLTuningAdvisor(
            @RequestParam String ip,
            @RequestParam String port,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String serviceName,
            @RequestParam String role,
            @RequestParam String sqlId) {

        ConnParam conn = new ConnParam(password, ip, port, username, serviceName, role);

        try (Connection connection = connectionManagementService.createConnection(conn)) {
            String tuningReport = performanceService.runSQLTuningAdvisor(connection, sqlId);
            return ResponseEntity.ok(tuningReport);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // Endpoint pour recalculer les statistiques d'une table
    @PostMapping("/recalculate-stats")
    public ResponseEntity<?> recalculateStats(
            @RequestParam String ip,
            @RequestParam String port,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String serviceName,
            @RequestParam String role,
            @RequestParam String tableName) {

        ConnParam conn = new ConnParam(password, ip, port, username, serviceName, role);

        try (Connection connection = connectionManagementService.createConnection(conn)) {
            String result = performanceService.recalculateStats(connection, tableName);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
