package hr.fer.hydro.config.core;
public class UserLocalThread {
    private final static ThreadLocal<UserInfo> coreUserThreadLocals = new ThreadLocal<>();

    public static void setUserInfo(final Integer userId) {
        if (userId == null){
            return;
        }
        coreUserThreadLocals.set(new UserInfo(userId));
    }

    public static Integer getUserId(){
        return coreUserThreadLocals.get().userId();
    }

    public static void deleteUserInfo(){
        coreUserThreadLocals.remove();
    }
}
