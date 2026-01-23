package hr.fer.hydro.config.core;

public class UserCoreLocalThread {
    private final static ThreadLocal<UserCore> users = new ThreadLocal<>();

    public static void setUserInfo(final Integer userId) {
        if (userId == null) {
            return;
        }
        users.set(new UserCore(userId));
    }

    public static Integer getUserId() {
        return users.get().userId();
    }

    public static void deleteUserInfo() {
        users.remove();
    }
}
