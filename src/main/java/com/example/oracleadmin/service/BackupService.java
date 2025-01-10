package com.example.oracleadmin.service;

import com.example.oracleadmin.entity.BackupProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
            String[] command = {
                    "docker", "exec", properties.getDockerContainer(), "/bin/bash", "-c",
                    "rman target / <<EOF\nLIST BACKUP;\nEXIT;\nEOF"
            };
            Process process = executeCommand(command);
            String output = readProcessOutput(process);
            logger.info("Historique des sauvegardes récupéré : {}", output);
            return output;
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de l'historique des sauvegardes", e);
            return "Erreur lors de la récupération de l'historique des sauvegardes : " + e.getMessage();
        }
    }


    // Méthode pour restaurer la base de données à une date spécifique
    public String restoreDatabaseToDate(String restoreDate) {
        try {
            validateProperties(); // Vérification des propriétés nécessaires
            logger.info("Lancement de la restauration à la date : {}", restoreDate);

            // Validation et formatage de la date pour RMAN
            String formattedDate = validateAndFormatDate(restoreDate);

            // Construction de la commande RMAN
            String command = String.format(
                    "rman target \"%s/%s@//%s:%s/%s\" <<EOF\n"
                            + "RUN {\n"
                            + "    ALLOCATE CHANNEL c1 DEVICE TYPE DISK;\n"
                            + "    RESTORE DATABASE SKIP TABLESPACE 'TEST_TB';\n"
                            + "    RECOVER DATABASE UNTIL TIME '%s';\n"  // Retirer TO_DATE
                            + "    RELEASE CHANNEL c1;\n"
                            + "}\n"
                            + "EXIT;\nEOF",
                    escape(properties.getOracleUser()), escape(properties.getOraclePassword()),
                    properties.getOracleHost(), properties.getOraclePort(),
                    properties.getOracleService(), formattedDate
            );

            // Exécution de la commande
            String[] dockerCommand = {
                    "docker", "exec", properties.getDockerContainer(), "/bin/bash", "-c", command
            };

            Process process = executeCommand(dockerCommand);
            String output = readProcessOutput(process);
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                logger.info("Restauration réussie : {}", output);
                return "Succès :\n" + output;
            } else {
                logger.error("Échec de la restauration, code de sortie : {}. Log : {}", exitCode, output);
                return "Erreur :\n" + output;
            }

        } catch (Exception e) {
            logger.error("Exception lors de la restauration", e);
            return "Exception : " + e.getMessage();
        }
    }


    private String validateAndFormatDate(String restoreDate) throws IllegalArgumentException {
        // Expression régulière pour valider le format "YYYY-MM-DD HH:MM:SS"
        String dateRegex = "^(\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2})$";

        // Validation du format de la date
        if (!restoreDate.matches(dateRegex)) {
            throw new IllegalArgumentException("Date invalide. Utilisez le format 'YYYY-MM-DD HH:MM:SS'.");
        }

        try {
            // Validation de la date avec SimpleDateFormat
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setLenient(false); // Désactive le comportement permissif
            Date parsedDate = sdf.parse(restoreDate);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Date invalide. Impossible de parser la date.", e);
        }

        // Retourner la date telle quelle, car elle est déjà dans le format attendu par RMAN
        return restoreDate;
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
