package com.example.oracleadmin.service;

import com.example.oracleadmin.entity.BackupProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class BackupService {

    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);
    private final BackupProperties properties;

    public BackupService(BackupProperties properties) {
        this.properties = properties;
    }

    // Méthode pour exécuter une sauvegarde
    public String runBackup(boolean isIncremental) {
        try {
            validateProperties();

            logger.info("Lancement de la sauvegarde {} avec les propriétés : {}",
                    isIncremental ? "incrémentielle" : "complète", properties);

            // Modifier la commande selon le type de sauvegarde (incrémentielle ou complète)
            String backupType = isIncremental ? "INCREMENTAL LEVEL 1" : "INCREMENTAL LEVEL 0";
            String command = String.format(
                    "rman target \"%s/%s@//%s:%s/%s\" <<EOF\nBACKUP %s DATABASE FORMAT '%s';\nEXIT;\nEOF",
                    escape(properties.getOracleUser()), escape(properties.getOraclePassword()),
                    properties.getOracleHost(), properties.getOraclePort(),
                    properties.getOracleService(), backupType, properties.getBackupPath()
            );

            String[] dockerCommand = {
                    "docker", "exec", properties.getDockerContainer(), "/bin/bash", "-c", command
            };

            Process process = executeCommand(dockerCommand);

            String output = readProcessOutput(process);

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                logger.info("Backup réussi : {}", output);
                return "Backup réussi:\n" + output;
            } else {
                logger.error("Erreur lors du backup, code de sortie : {}. Log : {}", exitCode, output);
                return "Erreur lors du backup:\n" + output;
            }

        } catch (Exception e) {
            logger.error("Exception lors de l'exécution du backup", e);
            return "Exception lors de l'exécution du backup: " + e.getMessage();
        }
    }

    // Méthode pour récupérer l'historique des sauvegardes
    public String getBackupHistory() {
        try {
            // La commande doit être exécutée via Docker, donc il faut la passer sous forme d'un tableau
            String[] command = {
                    "docker", "exec", properties.getDockerContainer(), "/bin/bash", "-c",
                    "rman target / <<EOF\nLIST BACKUP;\nEXIT;\nEOF"
            };
            Process process = executeCommand(command); // Passez la commande sous forme de tableau de chaînes
            String output = readProcessOutput(process);
            logger.info("Historique des sauvegardes récupéré : {}", output);
            return output;
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de l'historique des sauvegardes", e);
            return "Erreur lors de la récupération de l'historique des sauvegardes : " + e.getMessage();
        }
    }

    public String restoreDatabaseToDate(String restoreDate) {
        try {
            validateProperties();

            // Log de la commande de restauration
            logger.info("Lancement de la restauration à la date : {}", restoreDate);

            // Correction du formatage de la date pour RMAN
            String formattedDate = String.format(
                    "'TO_DATE(''%s'', ''YYYY-MM-DD HH24:MI:SS'')'", restoreDate
            );

            // Correction de la commande RMAN
            String command = String.format(
                    "rman target \"%s/%s@//%s:%s/%s\" <<EOF\nRESTORE DATABASE UNTIL TIME %s;\nRECOVER DATABASE UNTIL TIME %s;\nEXIT;\nEOF",
                    escape(properties.getOracleUser()), escape(properties.getOraclePassword()),
                    properties.getOracleHost(), properties.getOraclePort(),
                    properties.getOracleService(), formattedDate, formattedDate
            );

            String[] dockerCommand = {
                    "docker", "exec", properties.getDockerContainer(), "/bin/bash", "-c", command
            };

            // Exécuter la commande de restauration
            Process process = executeCommand(dockerCommand);
            String output = readProcessOutput(process);

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                logger.info("Restauration réussie : {}", output);
                return "Restauration réussie :\n" + output;
            } else {
                logger.error("Erreur lors de la restauration, code de sortie : {}. Log : {}", exitCode, output);
                return "Erreur lors de la restauration :\n" + output;
            }

        } catch (Exception e) {
            logger.error("Exception lors de l'exécution de la restauration", e);
            return "Exception lors de l'exécution de la restauration : " + e.getMessage();
        }
    }



    // Méthode pour exécuter la commande Docker
    private Process executeCommand(String[] command) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }

    // Lecture de la sortie de la commande
    private String readProcessOutput(Process process) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            return output.toString();
        }
    }

    // Validation des propriétés
    private void validateProperties() {
        if (properties == null ||
                properties.getDockerContainer() == null || properties.getDockerContainer().isEmpty() ||
                properties.getOracleUser() == null || properties.getOracleUser().isEmpty() ||
                properties.getOraclePassword() == null || properties.getOraclePassword().isEmpty() ||
                properties.getOracleHost() == null || properties.getOracleHost().isEmpty() ||
                properties.getOraclePort() == null || properties.getOraclePort().isEmpty() ||
                properties.getOracleService() == null || properties.getOracleService().isEmpty() ||
                properties.getBackupPath() == null || properties.getBackupPath().isEmpty()) {
            throw new IllegalArgumentException("Les propriétés de sauvegarde sont incomplètes ou invalides.");
        }
    }

    // Échappement des caractères spéciaux
    private String escape(String input) {
        return input.replace("'", "\\'").replace("\"", "\\\"");
    }
}

