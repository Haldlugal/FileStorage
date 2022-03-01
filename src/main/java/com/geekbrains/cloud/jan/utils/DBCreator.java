package com.geekbrains.cloud.jan.utils;

import java.sql.*;

public class DBCreator {

    private static Connection connection;
    private static Statement statement;

    public static void main(String[] args) throws SQLException {
        connect();
        createTable();
        insertInitialUsers();
//        clearTable();
        disconnect();
    }

    private static void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:FileCloudStorage.sqlite");
        statement = connection.createStatement();
    }

    private static void disconnect() {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            if (connection != null) {
                connection.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void createTable() throws SQLException {
        statement.executeUpdate("create table if not exists users (\n" +
                "    id integer primary key autoincrement not null,\n" +
                "    login text not null,\n" +
                "    password text not null,\n" +
                "    nickname text not null\n" +
                ");");
    }

    private static void clearTable() {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM users")) {
            ps.executeBatch();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void insertInitialUsers() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "insert into users (login, password, nickname) " +
                        "values (?, ?, ?)")) {
            for (int i = 0; i < 10; i++) {
                ps.setString(1, "test" + i);
                ps.setString(2, "1");
                ps.setString(3, "test" + i);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
