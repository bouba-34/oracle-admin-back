package com.example.oracleadmin.service;

import com.example.oracleadmin.dto.ConnParam;
import com.example.oracleadmin.entity.UserConnection;
import com.example.oracleadmin.repository.UserConnectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

@Service
public class ConnectionManagementService {

    @Autowired
    private UserConnectionRepository userConnectionRepository;

    public void saveConnection(UserConnection connection) {
        userConnectionRepository.save(connection);
    }

    public UserConnection getConnectionByName(String connectionName) {
        return userConnectionRepository.findByConnectionName(connectionName)
                .orElseThrow(() -> new RuntimeException("Connexion introuvable : " + connectionName));
    }

    public List<UserConnection> getAllConnections() {
        return userConnectionRepository.findAll();
    }

    public void deleteConnectionByName(String connectionName) {
        userConnectionRepository.deleteByConnectionName(connectionName);
    }

    public List<UserConnection> getConnectionsByClientId(String clientId) {
        return userConnectionRepository.findAllByClientId(clientId);
    }

    public void deleteConnectionById(Long connectionId) {
        userConnectionRepository.deleteById(connectionId);
    }

    public Connection createConnection(ConnParam param) throws SQLException {
        String jdbcUrl = String.format(
                "jdbc:oracle:thin:@//%s:%s/%s",
                param.getIp(),
                param.getPort(),
                param.getServiceName()
        );

        //System.out.println("connection url : " + jdbcUrl);

        String info = param.getUsername().equalsIgnoreCase("sys")  ? param.getUsername() + " as " + param.getRole() : "c##" + param.getUsername() + " as " + (param.getRole().equalsIgnoreCase("sysdba") ? param.getRole() : "c##" + param.getRole());

        System.out.println(info);

        return DriverManager.getConnection(
                jdbcUrl,
                info,
                param.getPassword()
        );
    }
}
