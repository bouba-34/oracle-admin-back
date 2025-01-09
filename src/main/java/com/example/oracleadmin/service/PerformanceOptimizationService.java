package com.example.oracleadmin.service;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Service
public class PerformanceOptimizationService {

    // Récupérer les requêtes lentes
    public List<Map<String, Object>> getSlowQueries(Connection connection) throws SQLException {
        List<Map<String, Object>> slowQueries = new ArrayList<>();

        String query = "SELECT SQL_ID, SQL_TEXT, ELAPSED_TIME, EXECUTIONS, LAST_ACTIVE_TIME " +
                "FROM V$SQL WHERE ELAPSED_TIME > 1000000";  // Exemple d'une requête lente

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> queryDetails = new HashMap<>();
                String sqlId = rs.getString("SQL_ID");
                String sqlText = rs.getString("SQL_TEXT");
                long elapsedTime = rs.getLong("ELAPSED_TIME");
                long executions = rs.getLong("EXECUTIONS");
                Timestamp lastExecutionTime = rs.getTimestamp("LAST_ACTIVE_TIME");

                queryDetails.put("sqlId", sqlId);
                queryDetails.put("executionTime", elapsedTime / 1000000.0);  // Conversion en secondes
                queryDetails.put("queryText", sqlText);
                queryDetails.put("numberOfExecutions", executions);
                queryDetails.put("lastExecutionTime", lastExecutionTime != null ? lastExecutionTime.toString() : "N/A");

                slowQueries.add(queryDetails);
            }
        }

        return slowQueries;
    }

    // Exécuter le SQL Tuning Advisor pour une requête lente donnée
    public String runSQLTuningAdvisor(Connection connection, String sqlId) throws SQLException {
        String taskName = "TuningTask_" + sqlId;

        // Étape 1: Créer une tâche de tuning
        String createTaskQuery =
                "DECLARE " +
                        "   v_sql_tune_task_id VARCHAR2(100); " +
                        "BEGIN " +
                        "   v_sql_tune_task_id := DBMS_SQLTUNE.create_tuning_task( " +
                        "       sql_id      => ?, " +
                        "       scope       => DBMS_SQLTUNE.scope_comprehensive, " +
                        "       time_limit  => 1000, " +
                        "       task_name   => ?, " +
                        "       description => 'Tuning task for SQL ID: ' || ?); " +
                        "END;";

        try (PreparedStatement stmt = connection.prepareStatement(createTaskQuery)) {
            stmt.setString(1, sqlId);
            stmt.setString(2, taskName);
            stmt.setString(3, sqlId);
            stmt.execute();
        }

        // Étape 2: Exécuter la tâche de tuning
        String executeTaskQuery =
                "BEGIN " +
                        "   DBMS_SQLTUNE.execute_tuning_task(task_name => ?); " +
                        "END;";

        try (PreparedStatement stmt = connection.prepareStatement(executeTaskQuery)) {
            stmt.setString(1, taskName);
            stmt.execute();
        }

        // Étape 3: Récupérer le rapport du SQL Tuning Advisor
        String reportQuery = "SELECT DBMS_SQLTUNE.report_tuning_task(?) AS report FROM dual";
        String report = null;

        try (PreparedStatement stmt = connection.prepareStatement(reportQuery)) {
            stmt.setString(1, taskName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    report = rs.getString("report");
                }
            }
        }

        // Étape 4: Supprimer la tâche
        String dropTaskQuery =
                "BEGIN " +
                        "   DBMS_SQLTUNE.drop_tuning_task(task_name => ?); " +
                        "END;";

        try (PreparedStatement stmt = connection.prepareStatement(dropTaskQuery)) {
            stmt.setString(1, taskName);
            stmt.execute();
        }

        // Retourner le rapport ou un message si aucune recommandation n'est disponible
        return report != null ? report : "No recommendations available.";
    }




    // Recalculer les statistiques des tables et index
    public String recalculateStats(Connection connection, String tableName) throws SQLException {
        String recalculationQuery = "BEGIN " +
                "DBMS_STATS.gather_table_stats(ownname => USER, tabname => ?, cascade => TRUE); " +
                "END;";

        try (PreparedStatement stmt = connection.prepareStatement(recalculationQuery)) {
            stmt.setString(1, tableName);
            stmt.executeUpdate();
        }

        return "Statistics recalculated successfully for table: " + tableName;
    }
}
