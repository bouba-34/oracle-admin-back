package com.example.oracleadmin.service;

import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

@Service
public class SecurityManagementService {

    // 1. Configurer une politique de chiffrement TDE
    public void configureTDEPolicy(Connection connection, String tablespaceName, String encryptionAlgorithm) throws SQLException {
        String tdeQuery = "ALTER TABLESPACE " + tablespaceName + " ENCRYPTION ONLINE USING '" + encryptionAlgorithm +  "' ENCRYPT";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(tdeQuery);
        }
    }

    // 2. Activer l'audit de sécurité
    public void enableSecurityAudit(Connection connection, String auditType) throws SQLException {
        String auditQuery = "AUDIT " + auditType + ";";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(auditQuery);
        }
    }

    // 3. Désactiver l'audit de sécurité
    public void disableSecurityAudit(Connection connection, String auditType) throws SQLException {
        String noauditQuery = "NOAUDIT " + auditType + ";";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(noauditQuery);
        }
    }

    // 4. Ajouter une politique VPD
    public void addVPDPolicy(Connection connection, String schemaName, String tableName, String policyName, String policyFunction) throws SQLException {
        String addPolicyQuery = "BEGIN " +
                "DBMS_RLS.ADD_POLICY( " +
                "object_schema => ?, " +
                "object_name => ?, " +
                "policy_name => ?, " +
                "function_schema => ?, " +
                "policy_function => ?); " +
                "END;";

        try (PreparedStatement stmt = connection.prepareStatement(addPolicyQuery)) {
            stmt.setString(1, schemaName);
            stmt.setString(2, tableName);
            stmt.setString(3, policyName);
            stmt.setString(4, schemaName); // Supposant que function_schema = schemaName
            stmt.setString(5, policyFunction);

            stmt.execute();
        }
    }


    // 5. Supprimer une politique VPD
    public void removeVPDPolicy(Connection connection, String schemaName, String tableName, String policyName) throws SQLException {
        String removePolicyQuery = "BEGIN " +
                "DBMS_RLS.DROP_POLICY( " +
                "object_schema => '" + schemaName + "', " +
                "object_name => '" + tableName + "', " +
                "policy_name => '" + policyName + "'); " +
                "END;";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(removePolicyQuery);
        }
    }

    // 6. Lister les politiques VPD existantes
    public List<Map<String, String>> listVPDPolicies(Connection connection) throws SQLException {
        String query = "SELECT OBJECT_OWNER, OBJECT_NAME, POLICY_NAME, FUNCTION FROM DBA_POLICIES";

        List<Map<String, String>> policies = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, String> policy = new HashMap<>();
                policy.put("object_owner", rs.getString("OBJECT_OWNER"));
                policy.put("object_name", rs.getString("OBJECT_NAME"));
                policy.put("policy_name", rs.getString("POLICY_NAME"));
                policy.put("function", rs.getString("FUNCTION")); // Correspond à "policy_function"
                policies.add(policy);
            }
        }

        return policies;
    }

}
