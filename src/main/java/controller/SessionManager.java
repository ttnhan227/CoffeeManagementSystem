//keeping login session instead of having to log in every time the app is opened
package controller;

import java.io.*;
import java.util.Properties;

public class SessionManager {
    private static final String SESSION_FILE = "session.properties";
    private static final SessionManager instance = new SessionManager();
    private Properties properties;

    private SessionManager() {
        properties = new Properties();
        loadSession();
    }

    public static SessionManager getInstance() {
        return instance;
    }

    public void saveSession(int userId, String userFullName, String userName, String userEmail, String userStatus, int userAdmin) {
        properties.setProperty("userId", String.valueOf(userId));
        properties.setProperty("userFullName", userFullName);
        properties.setProperty("userName", userName);
        properties.setProperty("userEmail", userEmail);
        properties.setProperty("userStatus", userStatus);
        properties.setProperty("userAdmin", String.valueOf(userAdmin));

        try (FileOutputStream out = new FileOutputStream(SESSION_FILE)) {
            properties.store(out, "User Session");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadSession() {
        File sessionFile = new File(SESSION_FILE);
        if (sessionFile.exists()) {
            try (FileInputStream in = new FileInputStream(sessionFile)) {
                properties.load(in);
                
                // Restore session to UserSessionController
                UserSessionController.setUserId(Integer.parseInt(properties.getProperty("userId", "-1")));
                UserSessionController.setUserFullName(properties.getProperty("userFullName", ""));
                UserSessionController.setUserName(properties.getProperty("userName", ""));
                UserSessionController.setUserEmail(properties.getProperty("userEmail", ""));
                UserSessionController.setUserStatus(properties.getProperty("userStatus", ""));
                UserSessionController.setUserAdmin(Integer.parseInt(properties.getProperty("userAdmin", "0")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void clearSession() {
        File sessionFile = new File(SESSION_FILE);
        if (sessionFile.exists()) {
            sessionFile.delete();
        }
        properties.clear();
    }

    public boolean hasActiveSession() {
        return !properties.isEmpty() && properties.getProperty("userId", "-1") != "-1";
    }

    public int getUserAdmin() {
        return Integer.parseInt(properties.getProperty("userAdmin", "0"));
    }
}
