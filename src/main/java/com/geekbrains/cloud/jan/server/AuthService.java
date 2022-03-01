package com.geekbrains.cloud.jan.server;


import com.geekbrains.cloud.jan.model.User;

import java.util.Optional;

/**
 * сервис аутеннтификации
 */
public interface AuthService {

    /**
     * Запустить сервис
     */
    void start();

    /**
     * Отключить сервис.
     */
    void stop();

    /**
     * Получить никнейм по логину/паролю
     * @param login
     * @param pass
     * @return никнейм если найден или null, если такого нет
     */
    Optional<User> getNickByLoginAndPass(String login, String pass);

    boolean changeNickname(String id, String newNick);
}
