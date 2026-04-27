package org.example;

import org.example.dao.UserDao;
import org.example.model.Role;
import org.example.model.User;

public class Main {
    public static void main(String[] args) {
        System.out.println("OTP Service started");

        UserDao userDao = new UserDao();

        User user = new User("user1", "test_hash", Role.USER);
        User createdUser = userDao.create(user);

        System.out.println("Created user:");
        System.out.println(createdUser);

        System.out.println("Admin exists: " + userDao.adminExists());

        System.out.println("Non-admin users:");
        for (User nonAdmin : userDao.findAllNonAdmins()) {
            System.out.println(nonAdmin);
        }
    }
}