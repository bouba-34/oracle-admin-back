package com.example.oracleadmin.controller;

import com.example.oracleadmin.service.BackupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/backup")
public class BackupController {

    private final BackupService backupService;

    @Autowired
    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    // Endpoint pour exécuter une sauvegarde
    @PostMapping("/run")
    public ResponseEntity<String> runBackup(@RequestBody BackupRequest request) {
        boolean isIncremental = request.isIncremental(); // Extraction de la donnée JSON
        String result = backupService.runBackup(isIncremental);
        return ResponseEntity.ok(result); // Retourner le résultat dans le corps de la réponse
    }

    // Endpoint pour récupérer l'historique des sauvegardes
    @GetMapping("/history")
    public ResponseEntity<String> getBackupHistory() {
        String history = backupService.getBackupHistory();
        return ResponseEntity.ok(history); // Retourner l'historique
    }

    // Endpoint pour restaurer la base de données à une date spécifique
    @PostMapping("/restore")
    public ResponseEntity<Map<String, Object>> restoreDatabase(@RequestBody RestoreRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Validation de la requête
            if (request.getRestoreDate() == null || request.getRestoreDate().isEmpty()) {
                response.put("status", "error");
                response.put("message", "La date de restauration est manquante ou invalide.");
                return ResponseEntity.badRequest().body(response);
            }

            // Appel du service pour restaurer la base
            String result = backupService.restoreDatabaseToDate(request.getRestoreDate());

            response.put("status", result.startsWith("Restauration réussie") ? "success" : "error");
            response.put("message", result);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Gestion des erreurs
            response.put("status", "error");
            response.put("message", "Erreur lors de la restauration : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}