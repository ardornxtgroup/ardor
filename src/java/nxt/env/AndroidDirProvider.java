package nxt.env;

public class AndroidDirProvider extends DesktopUserDirProvider {
    /**
     * This value is injected by com.jelurida.mobile.ardor.MainActivity
     */
    private static String homeDir;

    public static void setHomeDir(String homeDir) {
        AndroidDirProvider.homeDir = homeDir;
    }

    @Override
    public String getUserHomeDir() {
        if (homeDir == null) {
            throw new IllegalStateException("Home dir value was not injected properly");
        }
        return homeDir;
    }
}
