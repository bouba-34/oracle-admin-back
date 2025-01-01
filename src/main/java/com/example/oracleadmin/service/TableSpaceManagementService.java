package com.example.oracleadmin.service;

import com.example.oracleadmin.dto.TableSpace;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

@Service
public class TableSpaceManagementService {

    // Récupérer toutes les informations des tablespaces commençant par 'C##'
    public List<Map<String, Object>> getAllTableSpaces(Connection connection) throws SQLException {
        List<Map<String, Object>> tableSpacesInfo = new ArrayList<>();

        String query = """
                SELECT TABLESPACE_NAME, BLOCK_SIZE, INITIAL_EXTENT, NEXT_EXTENT, STATUS, CONTENTS, EXTENT_MANAGEMENT,
                       ALLOCATION_TYPE, SEGMENT_SPACE_MANAGEMENT
                FROM DBA_TABLESPACES
                """;
        //WHERE TABLESPACE_NAME LIKE 'C##%'

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> tableSpaceInfo = new HashMap<>();
                tableSpaceInfo.put("name", rs.getString("TABLESPACE_NAME"));
                tableSpaceInfo.put("block_size", rs.getInt("BLOCK_SIZE"));
                tableSpaceInfo.put("initial_extent", rs.getInt("INITIAL_EXTENT"));
                tableSpaceInfo.put("next_extent", rs.getInt("NEXT_EXTENT"));
                tableSpaceInfo.put("status", rs.getString("STATUS"));
                tableSpaceInfo.put("contents", rs.getString("CONTENTS"));
                tableSpaceInfo.put("extent_management", rs.getString("EXTENT_MANAGEMENT"));
                tableSpaceInfo.put("allocation_type", rs.getString("ALLOCATION_TYPE"));
                tableSpaceInfo.put("segment_space_management", rs.getString("SEGMENT_SPACE_MANAGEMENT"));

                tableSpacesInfo.add(tableSpaceInfo);
            }
        }

        return tableSpacesInfo;
    }

    // Créer un nouveau tablespace
    public void createTableSpace(Connection connection, TableSpace tableSpace) throws SQLException {
        // Construction de la requête
        String query = "CREATE TABLESPACE " + tableSpace.getName() +
                " DATAFILE '" + tableSpace.getDataFilePath() +
                "' SIZE " + tableSpace.getSize() + "M " +
                (tableSpace.getAutoExtend()
                        ? "AUTOEXTEND ON NEXT " + tableSpace.getIncrementSize() + "M MAXSIZE " + tableSpace.getMaxSize() + "M"
                        : "");

        // Exécution de la requête
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(query);
        }
    }


    // Supprimer un tablespace
    public void dropTableSpace(Connection connection, String tableSpaceName, boolean includingContentsAndDataFiles) throws SQLException {
        String query = "DROP TABLESPACE " + tableSpaceName;

        if (includingContentsAndDataFiles) {
            query += " INCLUDING CONTENTS AND DATAFILES";
        }

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(query);
        }
    }

    // Modifier un tablespace (ajouter un fichier ou augmenter la taille du fichier existant)
    public void modifyTableSpace(Connection connection, String tableSpaceName, String dataFilePath, int newSizeInMB) throws SQLException {
        String query = "ALTER DATABASE DATAFILE '" + dataFilePath + "' RESIZE " + newSizeInMB + "M";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(query);
        }
    }

    // Ajouter un fichier à un tablespace
    public void addDataFileToTableSpace(Connection connection, String tableSpaceName, String newDataFilePath, int sizeInMB) throws SQLException {
        String query = "ALTER TABLESPACE " + tableSpaceName + " ADD DATAFILE '" + newDataFilePath + "' SIZE " + sizeInMB + "M";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(query);
        }
    }

    // Récupérer l'utilisation des fichiers d'un tablespace
    public List<Map<String, Object>> getTableSpaceFileUsage(Connection connection, String tableSpaceName) throws SQLException {
        List<Map<String, Object>> fileUsageInfo = new ArrayList<>();

        String query = """
                SELECT FILE_NAME, BYTES/1024/1024 AS SIZE_MB, MAXBYTES/1024/1024 AS MAX_SIZE_MB, AUTOEXTENSIBLE
                FROM DBA_DATA_FILES
                WHERE TABLESPACE_NAME = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, tableSpaceName);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("file_name", rs.getString("FILE_NAME"));
                    fileInfo.put("size_mb", rs.getInt("SIZE_MB"));
                    fileInfo.put("max_size_mb", rs.getInt("MAX_SIZE_MB"));
                    fileInfo.put("autoextensible", rs.getString("AUTOEXTENSIBLE"));

                    fileUsageInfo.add(fileInfo);
                }
            }
        }

        return fileUsageInfo;
    }
}
