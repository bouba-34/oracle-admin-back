package com.example.oracleadmin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;

@Service
public class BackupSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(BackupSchedulerService.class);
    private final BackupService backupService;
    private final TaskScheduler taskScheduler;
    private ScheduledFuture<?> scheduledTask;

    public BackupSchedulerService(BackupService backupService) {
        this.backupService = backupService;

        // Initialisation du TaskScheduler
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1); // Limite à une tâche simultanée
        scheduler.initialize();
        this.taskScheduler = scheduler;
    }

    /**
     * Planifie une sauvegarde automatique (complète ou incrémentielle).
     *
     * @param dateTime la date et heure de la planification (format "yyyy-MM-dd HH:mm:ss")
     * @param isIncremental vrai pour une sauvegarde incrémentielle, faux pour une complète
     * @return message de confirmation ou d'erreur
     */
    public String scheduleBackup(String dateTime, boolean isIncremental) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date startTime = dateFormat.parse(dateTime);

            // Annuler la tâche précédente si elle existe
            if (scheduledTask != null) {
                scheduledTask.cancel(false);
            }

            // Planifier la nouvelle tâche
            scheduledTask = taskScheduler.schedule(() -> {
                String result = backupService.runBackup(isIncremental);
                logger.info(result); // Log de la sortie de la sauvegarde
            }, startTime);

            return "Sauvegarde automatique planifiée pour le : " + dateTime;
        } catch (Exception e) {
            logger.error("Erreur lors de la planification de la sauvegarde", e);
            return "Erreur : Format de date invalide. Utilisez 'yyyy-MM-dd HH:mm:ss'.";
        }
    }

    /**
     * Annule une sauvegarde planifiée.
     *
     * @return message de confirmation
     */
    public String cancelScheduledBackup() {
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTask = null;
            return "Planification annulée avec succès.";
        }
        return "Aucune sauvegarde planifiée à annuler.";
    }
}
