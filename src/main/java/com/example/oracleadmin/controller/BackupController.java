package com.example.oracleadmin.controller;

import com.example.oracleadmin.service.BackupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<String> restoreDatabase(@RequestBody RestoreRequest request) {
        String restoreDate = request.getRestoreDate(); // Extraction de la donnée JSON
        String result = backupService.restoreDatabaseToDate(restoreDate);
        return ResponseEntity.ok(result); // Retourner le résultat de la restauration
    }
}
