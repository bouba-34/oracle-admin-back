package com.example.oracleadmin.service;

import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

@Service
public class UserManagementService {

    // 1. Création d'un nouvel utilisateur
    public void createUser(Connection connection, String username, String password, String role, String quota, String defaultTablespace, String temporaryTablespace) throws SQLException {
        String createUserQuery = "CREATE USER C##" + username +
                " IDENTIFIED BY \"" + password + "\"" +
                " DEFAULT TABLESPACE " + defaultTablespace +
                " TEMPORARY TABLESPACE " + temporaryTablespace +
                (quota != null ? " QUOTA " + quota + "M ON " + defaultTablespace : "");

        System.out.println(createUserQuery);

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createUserQuery);
        }

        if (role != null && !role.isEmpty()) {
            grantRoleToUser(connection, username, role);
        }
    }

    // 2. Récupération de tous les utilisateurs
    public List<Map<String, Object>> getAllUsers(Connection connection) throws SQLException {
        List<Map<String, Object>> usersInfo = new ArrayList<>();
        String getUsersQuery = "SELECT USERNAME, DEFAULT_TABLESPACE, TEMPORARY_TABLESPACE, PROFILE FROM DBA_USERS WHERE USERNAME LIKE 'C##%'";
        String getRolesQuery = "SELECT GRANTEE, GRANTED_ROLE FROM DBA_ROLE_PRIVS WHERE GRANTEE LIKE 'C##%'";
        String getQuotasQuery = "SELECT TABLESPACE_NAME, BYTES, MAX_BYTES, USERNAME FROM DBA_TS_QUOTAS WHERE USERNAME LIKE 'C##%'";

        // Map to store roles
        Map<String, List<String>> userRolesMap = new HashMap<>();
        try (PreparedStatement rolesStmt = connection.prepareStatement(getRolesQuery);
             ResultSet rolesRs = rolesStmt.executeQuery()) {
            while (rolesRs.next()) {
                String grantee = rolesRs.getString("GRANTEE");
                String role = rolesRs.getString("GRANTED_ROLE");

                // Remove the "C##" prefix from grantee and role
                String formattedGrantee = grantee.startsWith("C##") ? grantee.substring(3) : grantee;
                String formattedRole = role.startsWith("C##") ? role.substring(3) : role;

                userRolesMap.computeIfAbsent(formattedGrantee, k -> new ArrayList<>()).add(formattedRole);
            }
        }

        // Map to store quotas
        Map<String, List<Map<String, Object>>> userQuotasMap = new HashMap<>();
        try (PreparedStatement quotasStmt = connection.prepareStatement(getQuotasQuery);
             ResultSet quotasRs = quotasStmt.executeQuery()) {
            while (quotasRs.next()) {
                String username = quotasRs.getString("USERNAME");

                Map<String, Object> quotaInfo = new HashMap<>();
                quotaInfo.put("tablespace_name", quotasRs.getString("TABLESPACE_NAME"));
                quotaInfo.put("bytes_mb", quotasRs.getLong("BYTES") / (1024 * 1024)); // Convert to MB
                quotaInfo.put("max_bytes_mb", quotasRs.getLong("MAX_BYTES") / (1024 * 1024)); // Convert to MB

                // Remove the "C##" prefix from username
                String formattedUsername = username.startsWith("C##") ? username.substring(3) : username;

                userQuotasMap.computeIfAbsent(formattedUsername, k -> new ArrayList<>()).add(quotaInfo);
            }
        }

        // Fetch users and aggregate all data
        try (PreparedStatement usersStmt = connection.prepareStatement(getUsersQuery);
             ResultSet usersRs = usersStmt.executeQuery()) {
            while (usersRs.next()) {
                Map<String, Object> userInfo = new HashMap<>();
                String username = usersRs.getString("USERNAME");

                // Remove the "C##" prefix from username
                String formattedUsername = username.startsWith("C##") ? username.substring(3) : username;

                userInfo.put("username", formattedUsername);
                userInfo.put("default_tablespace", usersRs.getString("DEFAULT_TABLESPACE"));
                userInfo.put("temporary_tablespace", usersRs.getString("TEMPORARY_TABLESPACE"));
                userInfo.put("roles", userRolesMap.getOrDefault(formattedUsername, Collections.emptyList()));
                userInfo.put("quotas", userQuotasMap.getOrDefault(formattedUsername, Collections.emptyList()));

                usersInfo.add(userInfo);
            }
        }

        return usersInfo;
    }




    // 3. Suppression d'un utilisateur
    public void dropUser(Connection connection, String username) throws SQLException {
        String dropUserQuery = "DROP USER C##" + username + " CASCADE";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(dropUserQuery);
        }
    }

    // 4. Modification d'un utilisateur
    public void updateUser(Connection connection, String username, String password, String role, String quota, String defaultTablespace, String temporaryTablespace) throws SQLException {
        if (password != null && !password.isEmpty()) {
            String alterPasswordQuery = "ALTER USER C##" + username + " IDENTIFIED BY " + password;
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate(alterPasswordQuery);
            }
        }

        if (defaultTablespace != null && !defaultTablespace.isEmpty()) {
            String alterTablespaceQuery = "ALTER USER C##" + username + " DEFAULT TABLESPACE " + defaultTablespace;
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate(alterTablespaceQuery);
            }
        }

        if (temporaryTablespace != null && !temporaryTablespace.isEmpty()) {
            String alterTemporaryTablespaceQuery = "ALTER USER C##" + username + " TEMPORARY TABLESPACE " + temporaryTablespace;
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate(alterTemporaryTablespaceQuery);
            }
        }

        if (quota != null && !quota.isEmpty()) {
            String alterQuotaQuery = "ALTER USER C##" + username + " QUOTA " + quota;
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate(alterQuotaQuery);
            }
        }

        if (role != null && !role.isEmpty()) {
            grantRoleToUser(connection, username, role);
        }
    }

    // 5. Attribution d'un rôle à un utilisateur
    public void grantRoleToUser(Connection connection, String username, String role) throws SQLException {
        String grantRoleQuery = "GRANT C##" + role + " TO C##" + username;

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(grantRoleQuery);
        }
    }

    // 6. Retrait d'un rôle d'un utilisateur
    public void revokeRoleFromUser(Connection connection, String username, String role) throws SQLException {
        String revokeRoleQuery = "REVOKE " + role + " FROM C##" + username;

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(revokeRoleQuery);
        }
    }
}
