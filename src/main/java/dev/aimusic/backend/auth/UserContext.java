package dev.aimusic.backend.auth;

import dev.aimusic.backend.user.dao.UserModel;

public class UserContext {
    private static final ThreadLocal<UserModel> current = new ThreadLocal<>();

    public static void set(UserModel user) {
        current.set(user);
    }

    public static UserModel get() {
        return current.get();
    }

    public static void clear() {
        current.remove();
    }
}
