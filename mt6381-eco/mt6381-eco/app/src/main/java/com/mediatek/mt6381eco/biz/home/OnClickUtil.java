package com.mediatek.mt6381eco.biz.home;

public class OnClickUtil {

    private static final long INTERVAL = 500;

    private static long lastTime;

    public static boolean isTooFast() {
        long nowTime = System.currentTimeMillis();
        long interval = nowTime - lastTime;
        if (interval > 0 && interval < INTERVAL) {
            return true;
        }
        lastTime = System.currentTimeMillis();
        return false;
    }

    public static boolean isTooFast(long INTERVAL) {
        long nowTime = System.currentTimeMillis();
        long interval = nowTime - lastTime;
        if (interval > 0 && interval < INTERVAL) {
            return true;
        }
        lastTime = System.currentTimeMillis();
        return false;
    }

    public static boolean isTooFast(int OWN_INTERVAL) {
        long nowTime = System.currentTimeMillis();
        long interval = nowTime - lastTime;
        if (interval > 0 && interval < OWN_INTERVAL) {
            return true;
        }
        lastTime = System.currentTimeMillis();
        return false;
    }
}
