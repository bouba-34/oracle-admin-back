package com.example.oracleadmin.service;

import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

@Service
public class RoleManagementService {

    public List<Map<String, Object>> getAllRoles(Connection connection) throws SQLException {
        // Initialisation de la liste pour contenir les informations de rôle
        List<Map<String, Object>> rolesInfo = new ArrayList<>();
        Map<String, Map<String, Object>> rolesMap = new HashMap<>();

        // Requêtes pour récupérer les privilèges système et objets
        String systemPrivilegesQuery = "SELECT GRANTEE, PRIVILEGE FROM DBA_SYS_PRIVS WHERE GRANTEE LIKE 'C##%'";
        String objectPrivilegesQuery = "SELECT GRANTEE, TABLE_NAME, PRIVILEGE FROM DBA_TAB_PRIVS WHERE GRANTEE LIKE 'C##%'";
        String allRolesQuery = "SELECT ROLE FROM DBA_ROLES WHERE ROLE LIKE 'C##%'";

        // Récupérer tous les rôles qui commencent par 'C##'
        try (PreparedStatement allRolesStmt = connection.prepareStatement(allRolesQuery);
             ResultSet allRolesRs = allRolesStmt.executeQuery()) {

            while (allRolesRs.next()) {
                String grantee = allRolesRs.getString("ROLE");

                // Ajouter chaque rôle dans le rolesMap si ce n'est pas déjà fait
                rolesMap.putIfAbsent(grantee, new HashMap<>());
                Map<String, Object> roleInfo = rolesMap.get(grantee);

                // Initialiser les collections pour privilèges et tables si elles n'existent pas
                roleInfo.putIfAbsent("system_privilege", new HashSet<String>());
                roleInfo.putIfAbsent("object_privilege", new HashSet<String>());
                roleInfo.putIfAbsent("table_names", new HashSet<String>());
            }
        }

        // Récupérer les privilèges système
        try (PreparedStatement systemStmt = connection.prepareStatement(systemPrivilegesQuery);
             ResultSet systemRs = systemStmt.executeQuery()) {

            while (systemRs.next()) {
                String grantee = systemRs.getString("GRANTEE");
                String privilege = systemRs.getString("PRIVILEGE");

                rolesMap.putIfAbsent(grantee, new HashMap<>());
                Map<String, Object> roleInfo = rolesMap.get(grantee);

                // Ajouter les privilèges système
                roleInfo.putIfAbsent("system_privilege", new HashSet<String>());
                ((Set<String>) roleInfo.get("system_privilege")).add(privilege);
            }
        }

        // Récupérer les privilèges objets
        try (PreparedStatement objectStmt = connection.prepareStatement(objectPrivilegesQuery);
             ResultSet objectRs = objectStmt.executeQuery()) {

            while (objectRs.next()) {
                String grantee = objectRs.getString("GRANTEE");
                String privilege = objectRs.getString("PRIVILEGE");

                rolesMap.putIfAbsent(grantee, new HashMap<>());
                Map<String, Object> roleInfo = rolesMap.get(grantee);

                // Ajouter les privilèges objets
                roleInfo.putIfAbsent("object_privilege", new HashSet<String>());
                ((Set<String>) roleInfo.get("object_privilege")).add(privilege);
            }
        }

        // Récupérer les noms des tables sans doublons
        try (PreparedStatement tableStmt = connection.prepareStatement(objectPrivilegesQuery);
             ResultSet tableRs = tableStmt.executeQuery()) {

            while (tableRs.next()) {
                String grantee = tableRs.getString("GRANTEE");
                String tableName = tableRs.getString("TABLE_NAME");

                rolesMap.putIfAbsent(grantee, new HashMap<>());
                Map<String, Object> roleInfo = rolesMap.get(grantee);

                // Ajouter les tables
                roleInfo.putIfAbsent("table_names", new HashSet<String>());
                ((Set<String>) roleInfo.get("table_names")).add(tableName);
            }
        }

        // Ajouter le nom du rôle (sans le préfixe C##) et convertir les sets en listes pour le résultat final
        for (Map.Entry<String, Map<String, Object>> entry : rolesMap.entrySet()) {
            String grantee = entry.getKey();
            Map<String, Object> roleInfo = entry.getValue();
            roleInfo.put("name", grantee.replace("C##", ""));

            // Convertir les sets en listes pour le résultat final
            if (roleInfo.containsKey("system_privilege")) {
                roleInfo.put("system_privilege", new ArrayList<>((Set<String>) roleInfo.get("system_privilege")));
            }
            if (roleInfo.containsKey("object_privilege")) {
                roleInfo.put("object_privilege", new ArrayList<>((Set<String>) roleInfo.get("object_privilege")));
            }
            if (roleInfo.containsKey("table_names")) {
                roleInfo.put("table_names", new ArrayList<>((Set<String>) roleInfo.get("table_names")));
            }

            // Ajouter à la liste des résultats
            rolesInfo.add(roleInfo);
        }

        return rolesInfo;
    }



    // 1. Création d'un nouveau rôle
    public void createRole(Connection connection, String roleName) throws SQLException {
        String createRoleQuery = "CREATE ROLE C##" + roleName;

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createRoleQuery);
        }
    }

    // 2. Suppression d'un rôle
    public void dropRole(Connection connection, String roleName) throws SQLException {
        String dropRoleQuery = "DROP ROLE C##" + roleName;

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(dropRoleQuery);
        }
    }

    // 3. Affecter des privilèges système à un rôle
    public void grantSystemPrivilege(Connection connection, String roleName, String privilege) throws SQLException {
        String grantPrivilegeQuery = "GRANT " + privilege + " TO C##" + roleName;

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(grantPrivilegeQuery);
        }
    }

    // 4. Retirer des privilèges système d'un rôle
    public void revokeSystemPrivilege(Connection connection, String roleName, String privilege) throws SQLException {
        String revokePrivilegeQuery = "REVOKE " + privilege + " FROM C##" + roleName;

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(revokePrivilegeQuery);
        }
    }

    // 5. Affecter des privilèges objets à un rôle
    public void grantObjectPrivilege(Connection connection, String roleName, String privilege, String tableName) throws SQLException {
        String grantObjectPrivilegeQuery = "GRANT " + privilege + " ON " + tableName + " TO C##" + roleName;

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(grantObjectPrivilegeQuery);
        }
    }

    // 6. Retirer des privilèges objets d'un rôle
    public void revokeObjectPrivilege(Connection connection, String roleName, String privilege, String tableName) throws SQLException {
        String revokeObjectPrivilegeQuery = "REVOKE " + privilege + " ON " + tableName + " FROM C##" + roleName;

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(revokeObjectPrivilegeQuery);
        }
    }
}
