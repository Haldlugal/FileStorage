package com.geekbrains.cloud.jan.server;

import com.geekbrains.cloud.jan.model.User;

import java.sql.*;
import java.util.Optional;

public class DbAuthService implements AuthService{

    private static Connection connection;

    @Override
    public void start() {
        try {
            connect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void stop() {
        disconnect();
    }

    @Override
    public Optional<User> getNickByLoginAndPass(String login, String pass) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE login = ? AND password = ?");
            statement.setString(1, login);
            statement.setString(2, pass);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()){
                String nickname = resultSet.getString("nickname");
                String id = resultSet.getString("id");
                return Optional.of(new User(id , nickname));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }

    public boolean changeNickname(String id, String newNick) {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE users SET nickname = ? WHERE id = ?");
            statement.setString(1, newNick);
            statement.setString(2, id);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:FileCloudStorage.sqlite");
    }

    private static void disconnect() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
