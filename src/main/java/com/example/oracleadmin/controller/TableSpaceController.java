package com.example.oracleadmin.controller;

import com.example.oracleadmin.dto.ConnParam;
import com.example.oracleadmin.dto.TableSpace;
import com.example.oracleadmin.service.ConnectionManagementService;
import com.example.oracleadmin.service.TableSpaceManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tablespaces")
public class TableSpaceController {

    @Autowired
    private TableSpaceManagementService tableSpaceManagementService;

    @Autowired
    private ConnectionManagementService connectionManagementService;

    // Exemple de configuration pour la connexion à Oracle Database
    /*private Connection getConnection() throws SQLException {
        String url = "jdbc:oracle:thin:@localhost:1521:xe";
        String username = "system";
        String password = "oracle";
        return DriverManager.getConnection(url, username, password);
    }*/

    // 1. Récupérer tous les tablespaces
    @GetMapping("/all")
    public List<Map<String, Object>> getAllTableSpaces(@RequestParam String ip,
                                                       @RequestParam String port,
                                                       @RequestParam String username,
                                                       @RequestParam String password,
                                                       @RequestParam String serviceName,
                                                       @RequestParam String role) throws SQLException {
        ConnParam param = new ConnParam(password, ip, port, username, serviceName, role);
        try (Connection connection = connectionManagementService.createConnection(param)) {
            return tableSpaceManagementService.getAllTableSpaces(connection);
        }
    }

    // 2. Créer un nouveau tablespace
    @PostMapping("/create")
    public String createTableSpace(@RequestBody TableSpace tableSpace, @RequestParam String ip,
                                   @RequestParam String port,
                                   @RequestParam String username,
                                   @RequestParam String password,
                                   @RequestParam String serviceName,
                                   @RequestParam String role) throws SQLException {
        ConnParam param = new ConnParam(password, ip, port, username, serviceName, role);
        try (Connection connection = connectionManagementService.createConnection(param)) {
            tableSpaceManagementService.createTableSpace(connection, tableSpace);
            return "Tablespace " + tableSpace.getName() + " created successfully!";
        }
    }

    // 3. Supprimer un tablespace
    @DeleteMapping("/{name}")
    public String deleteTableSpace(@PathVariable String name, @RequestParam(defaultValue = "false") boolean includingContents,
                                   @RequestParam String ip,
                                   @RequestParam String port,
                                   @RequestParam String username,
                                   @RequestParam String password,
                                   @RequestParam String serviceName,
                                   @RequestParam String role) throws SQLException {
        ConnParam param = new ConnParam(password, ip, port, username, serviceName, role);
        try (Connection connection = connectionManagementService.createConnection(param)) {
            tableSpaceManagementService.dropTableSpace(connection, name, includingContents);
            return "Tablespace " + name + " deleted successfully!";
        }
    }

    /*
    // 4. Modifier un tablespace
    @PutMapping("/{name}/resize")
    public String modifyTableSpace(@PathVariable String name, @RequestParam int newSize) throws SQLException {
        try (Connection connection = getConnection()) {
            tableSpaceManagementService.modifyTableSpace(connection, name, newSize);
            return "Tablespace " + name + " resized successfully to " + newSize + "M!";
        }
    }

    // 5. Ajouter un fichier à un tablespace
    @PostMapping("/{name}/add-file")
    public String addDataFileToTableSpace(@PathVariable String name, @RequestBody Map<String, Object> fileDetails) throws SQLException {
        String filePath = (String) fileDetails.get("filePath");
        int size = (int) fileDetails.get("size");

        try (Connection connection = getConnection()) {
            tableSpaceManagementService.addDataFileToTableSpace(connection, name, filePath, size);
            return "File " + filePath + " added to tablespace " + name + " with size " + size + "M!";
        }
    }*/

    // 6. Récupérer l'utilisation des fichiers d'un tablespace
    @GetMapping("/{name}/file-usage")
    public List<Map<String, Object>> getTableSpaceFileUsage(@PathVariable String name, @RequestParam String ip,
                                                            @RequestParam String port,
                                                            @RequestParam String username,
                                                            @RequestParam String password,
                                                            @RequestParam String serviceName,
                                                            @RequestParam String role) throws SQLException {
        ConnParam param = new ConnParam(password, ip, port, username, serviceName, role);
        try (Connection connection = connectionManagementService.createConnection(param)) {
            return tableSpaceManagementService.getTableSpaceFileUsage(connection, name);
        }
    }
}
