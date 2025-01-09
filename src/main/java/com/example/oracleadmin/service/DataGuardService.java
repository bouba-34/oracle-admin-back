package com.example.oracleadmin.service;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

@Service
public class DataGuardService {

    public Map<String, String> getDatabaseStatus(Connection connection) throws Exception {
        String query = "SELECT DATABASE_ROLE, OPEN_MODE FROM V$DATABASE";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            Map<String, String> status = new HashMap<>();
            if (rs.next()) {
                status.put("databaseRole", rs.getString("DATABASE_ROLE"));
                status.put("openMode", rs.getString("OPEN_MODE"));
            }
            return status;
        }
    }

    public String performSwitchover(Connection connection) throws Exception {
        String command = "ALTER DATABASE COMMIT TO SWITCHOVER TO STANDBY";
        try (PreparedStatement stmt = connection.prepareStatement(command)) {
            stmt.executeUpdate();
            return "Switchover performed successfully!";
        }
    }

    public String performFailover(Connection connection) throws Exception {
        String command = "ALTER DATABASE ACTIVATE PHYSICAL STANDBY";
        try (PreparedStatement stmt = connection.prepareStatement(command)) {
            stmt.executeUpdate();
            return "Failover performed successfully!";
        }
    }
}
