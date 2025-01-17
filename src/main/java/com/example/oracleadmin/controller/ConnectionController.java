package com.example.oracleadmin.controller;

import com.example.oracleadmin.dto.ConnParam;
import com.example.oracleadmin.entity.UserConnection;
import com.example.oracleadmin.service.ConnectionManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.util.List;

@RestController
@RequestMapping("/api/connections")
public class ConnectionController {

    @Autowired
    private ConnectionManagementService connectionService;

    @PostMapping("/save")
    public String saveConnection(@RequestBody UserConnection connection) {
        connectionService.saveConnection(connection);
        return "Connexion sauvegardée avec succès : " + connection.getConnectionName();
    }

    @GetMapping("/{connectionName}")
    public UserConnection getConnection(@PathVariable String connectionName) {
        return connectionService.getConnectionByName(connectionName);
    }

    @GetMapping("/all")
    public List<UserConnection> getAllConnections() {
        return connectionService.getAllConnections();
    }

    @DeleteMapping("/{id}")
    public String deleteConnection(@PathVariable Long id) {
        //connectionService.deleteConnectionByName(id);
        connectionService.deleteConnectionById(id);
        return "Connexion supprimée : " + id;
    }

    @GetMapping("/user/{clientId}")
    public List<UserConnection> getConnectionsByClientId(@PathVariable String clientId) {
        return connectionService.getConnectionsByClientId(clientId);
    }

    @PostMapping("/test")
    public String connectToDatabase(@RequestBody ConnParam param) {
        try (Connection connection = connectionService.createConnection(param)) {
            //System.out.println(connection);
            if (connection.isValid(5)) {
                return "success";
            } else {
                return "failed";
            }
        } catch (Exception e) {
            return "Erreur : " + e.getMessage();
        }
    }

    /*@PostMapping("/execute/{connectionName}")
    public String executeActionOnConnection(@PathVariable String connectionName, @RequestBody String sql) {
        UserConnection connectionDetails = connectionService.getConnectionByName(connectionName);

        try (Connection connection = connectionService.createConnection(connectionDetails)) {
            connection.createStatement().execute(sql);
            return "Action exécutée avec succès.";
        } catch (Exception e) {
            return "Erreur : " + e.getMessage();
        }
    }*/

}
