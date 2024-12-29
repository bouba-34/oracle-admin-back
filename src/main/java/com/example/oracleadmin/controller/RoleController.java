package com.example.oracleadmin.controller;

import com.example.oracleadmin.dto.ConnParam;
import com.example.oracleadmin.entity.UserConnection;
import com.example.oracleadmin.service.ConnectionManagementService;
import com.example.oracleadmin.service.RoleManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    ConnectionManagementService connectionManagementService;

    @Autowired
    RoleManagementService roleManagementService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllRoles(
            @RequestParam String ip,
            @RequestParam String port,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String serviceName,
            @RequestParam String role) {

        // Créer l'objet de paramètres de connexion
        ConnParam conn = new ConnParam(password, ip, port, username, serviceName, role);

        System.out.println("Paramètres de connexion : " + conn);

        try (Connection connection = connectionManagementService.createConnection(conn)) {
            // Appeler le service pour récupérer les rôles
            List<Map<String, Object>> roles = roleManagementService.getAllRoles(connection);
            return ResponseEntity.ok(roles);

        } catch (SQLException e) {
            // Gérer une erreur SQL
            Map<String, String> errorResponse = Map.of("error", "Database error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);

        } catch (Exception e) {
            // Gérer toute autre exception inattendue
            Map<String, String> errorResponse = Map.of("error", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createRole(
            @RequestParam String ip,
            @RequestParam String port,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String serviceName,
            @RequestParam String role,
            @RequestParam String roleName) {

        // Créer les paramètres de connexion
        ConnParam conn = new ConnParam(password, ip, port, username, serviceName, role);

        // Log (sans informations sensibles comme le mot de passe)
        System.out.println("Tentative de création du rôle avec les paramètres : " + conn);

        try (Connection connection = connectionManagementService.createConnection(conn)) {
            // Créer le rôle
            roleManagementService.createRole(connection, roleName);

            // Retour de succès
            Map<String, String> response = Map.of("message", "Role created successfully");
            return ResponseEntity.ok(response);

        } catch (SQLException e) {
            // Retourner une erreur SQL
            Map<String, String> errorResponse = Map.of("error", "Error creating role: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);

        } catch (Exception e) {
            // Gérer les erreurs inattendues
            Map<String, String> errorResponse = Map.of("error", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteRole(
            @RequestParam String ip,
            @RequestParam String port,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String serviceName,
            @RequestParam String role,
            @RequestParam String roleName) {

        // Créer les paramètres de connexion
        ConnParam conn = new ConnParam(password, ip, port, username, serviceName, role);

        // Log pour traçabilité (sans informations sensibles)
        System.out.println("Tentative de suppression du rôle : " + roleName);

        try (Connection connection = connectionManagementService.createConnection(conn)) {
            // Supprimer le rôle
            roleManagementService.dropRole(connection, roleName);

            // Retourner un succès
            Map<String, String> response = Map.of("message", "Role deleted successfully");
            return ResponseEntity.ok(response);

        } catch (SQLException e) {
            // Gérer les erreurs SQL
            Map<String, String> errorResponse = Map.of("error", "Error deleting role: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);

        } catch (Exception e) {
            // Gérer les erreurs inattendues
            Map<String, String> errorResponse = Map.of("error", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @PostMapping("/grant/system")
    public ResponseEntity<Map<String, String>> grantSystemPrivilege(
            @RequestParam String ip,
            @RequestParam String port,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String serviceName,
            @RequestParam String role,
            @RequestParam String roleName,
            @RequestParam String privilege) {

        // Créer les paramètres de connexion
        ConnParam conn = new ConnParam(password, ip, port, username, serviceName, role);

        // Log pour traçabilité
        System.out.println("Tentative d'attribution du privilège système : " + privilege + " au rôle : " + roleName);

        try (Connection connection = connectionManagementService.createConnection(conn)) {
            // Appeler le service pour attribuer le privilège
            roleManagementService.grantSystemPrivilege(connection, roleName, privilege);

            // Retourner un succès
            Map<String, String> response = Map.of("message", "System privilege granted successfully");
            return ResponseEntity.ok(response);

        } catch (SQLException e) {
            // Gérer les erreurs SQL
            Map<String, String> errorResponse = Map.of("error", "Error granting system privilege: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);

        } catch (Exception e) {
            // Gérer les erreurs inattendues
            Map<String, String> errorResponse = Map.of("error", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @PostMapping("/revoke/system")
    public ResponseEntity<Map<String, String>> revokeSystemPrivilege(
            @RequestParam String ip,
            @RequestParam String port,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String serviceName,
            @RequestParam String role,
            @RequestParam String roleName,
            @RequestParam String privilege) {

        // Créer les paramètres de connexion
        ConnParam conn = new ConnParam(password, ip, port, username, serviceName, role);

        // Log pour traçabilité
        System.out.println("Tentative de révocation du privilège système : " + privilege + " pour le rôle : " + roleName);

        try (Connection connection = connectionManagementService.createConnection(conn)) {
            // Appeler le service pour révoquer le privilège
            roleManagementService.revokeSystemPrivilege(connection, roleName, privilege);

            // Retourner un succès
            Map<String, String> response = Map.of("message", "System privilege revoked successfully");
            return ResponseEntity.ok(response);

        } catch (SQLException e) {
            // Gérer les erreurs SQL
            Map<String, String> errorResponse = Map.of("error", "Error revoking system privilege: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);

        } catch (Exception e) {
            // Gérer les erreurs inattendues
            Map<String, String> errorResponse = Map.of("error", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @PostMapping("/grant/object")
    public ResponseEntity<Map<String, String>> grantObjectPrivilege(
            @RequestParam String ip,
            @RequestParam String port,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String serviceName,
            @RequestParam String role,
            @RequestParam String roleName,
            @RequestParam String privilege,
            @RequestParam String tableName) {

        // Création des paramètres de connexion
        ConnParam conn = new ConnParam(password, ip, port, username, serviceName, role);

        // Log pour traçabilité
        System.out.println("Tentative d'attribution du privilège objet : " + privilege +
                " sur la table : " + tableName + " pour le rôle : " + roleName);

        try (Connection connection = connectionManagementService.createConnection(conn)) {
            // Appel du service pour accorder le privilège objet
            roleManagementService.grantObjectPrivilege(connection, roleName, privilege, tableName);

            // Réponse en cas de succès
            Map<String, String> response = Map.of("message", "Object privilege granted successfully");
            return ResponseEntity.ok(response);

        } catch (SQLException e) {
            // Gérer les erreurs SQL
            Map<String, String> errorResponse = Map.of("error", "Error granting object privilege: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);

        } catch (Exception e) {
            // Gérer les erreurs inattendues
            Map<String, String> errorResponse = Map.of("error", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @PostMapping("/revoke/object")
    public ResponseEntity<Map<String, String>> revokeObjectPrivilege(
            @RequestParam String ip,
            @RequestParam String port,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String serviceName,
            @RequestParam String role,
            @RequestParam String roleName,
            @RequestParam String privilege,
            @RequestParam String tableName) {

        // Création des paramètres de connexion
        ConnParam conn = new ConnParam(password, ip, port, username, serviceName, role);

        // Log pour traçabilité
        System.out.println("Tentative de révocation du privilège objet : " + privilege +
                " sur la table : " + tableName + " pour le rôle : " + roleName);

        try (Connection connection = connectionManagementService.createConnection(conn)) {
            // Appel du service pour révoquer le privilège objet
            roleManagementService.revokeObjectPrivilege(connection, roleName, privilege, tableName);

            // Réponse en cas de succès
            Map<String, String> response = Map.of("message", "Object privilege revoked successfully");
            return ResponseEntity.ok(response);

        } catch (SQLException e) {
            // Gérer les erreurs SQL
            Map<String, String> errorResponse = Map.of("error", "Error revoking object privilege: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);

        } catch (Exception e) {
            // Gérer les erreurs inattendues
            Map<String, String> errorResponse = Map.of("error", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
