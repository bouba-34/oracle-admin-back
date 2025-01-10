package com.example.oracleadmin.controller;

import com.example.oracleadmin.service.BackupSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedule") // Notez le changement de préfixe pour les API REST
public class BackupScheduleController {

    private final BackupSchedulerService backupSchedulerService;

    @Autowired
    public BackupScheduleController(BackupSchedulerService backupSchedulerService) {
        this.backupSchedulerService = backupSchedulerService;
    }

    // Planifier une sauvegarde
    @PostMapping("/configure")
    public ResponseEntity<?> configureBackupSchedule(
            @RequestBody BackupScheduleRequest request) {
        String result = backupSchedulerService.scheduleBackup(request.getDateTime(), request.isIncremental());
        return ResponseEntity.ok(result); // Renvoie une réponse HTTP 200 avec le message
    }
}
