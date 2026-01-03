package com.aniemsao.customdungeons.manager;

import com.aniemsao.customdungeons.CustomDungeons;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private Connection connection;

    public void connect() {
        String type = CustomDungeons.getInstance().getConfig().getString("database.type", "sqlite");
        try {
            if (type.equalsIgnoreCase("mysql")) {
                String host = CustomDungeons.getInstance().getConfig().getString("database.host");
                int port = CustomDungeons.getInstance().getConfig().getInt("database.port");
                String db = CustomDungeons.getInstance().getConfig().getString("database.name");
                String user = CustomDungeons.getInstance().getConfig().getString("database.user");
                String pass = CustomDungeons.getInstance().getConfig().getString("database.password");
                connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + db, user, pass);
            } else {
                connection = DriverManager.getConnection("jdbc:sqlite:" + CustomDungeons.getInstance().getDataFolder() + "/data.db");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
