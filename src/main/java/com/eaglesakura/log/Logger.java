package com.eaglesakura.log;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * ログ出力を制御する。
 */
public class Logger {
    private static Impl sLogger = null;
    public static final int LEVEL_INFO = 1;
    public static final int LEVEL_DEBUG = 2;
    public static final int LEVEL_ERROR = 3;

    /**
     * Androidのpackageが存在したら、Android用ロガーを利用する
     */
    static {
        initLogger();
    }

    static void initLogger() {
        if (sLogger == null) {
            try {
                sLogger = new AndroidLogger(Class.forName("android.util.Log"));
            } catch (Exception e) {
                sLogger = new BasicLogger();
            }
        }
    }

    public interface Impl {
        void out(int level, String tag, String msg);
    }

    /**
     * Android用Logger
     */
    public static class AndroidLogger implements Impl {
        private Class<?> clazz;
        private Method i;
        private Method d;
        private Method w;
        private boolean stackInfo = false;

        public AndroidLogger(Class<?> logClass) {
            this.clazz = logClass;
            try {
                this.i = clazz.getMethod("i", String.class, String.class);
                this.d = clazz.getMethod("d", String.class, String.class);
                this.w = clazz.getMethod("w", String.class, String.class);
            } catch (Exception e) {

            }
        }

        public AndroidLogger setStackInfo(boolean stackInfo) {
            this.stackInfo = stackInfo;
            return this;
        }

        protected int getStackDepth() {
            return 2;
        }

        @Override
        public void out(int level, String tag, String msg) {
            try {
                String message;
                Method method;
                switch (level) {
                    case LEVEL_INFO:
                        method = i;
                        break;
                    case LEVEL_ERROR:
                        method = w;
                        break;
                    default:
                        method = d;
                        break;
                }
                if (stackInfo) {
                    StackTraceElement[] trace = new Exception().getStackTrace();
                    StackTraceElement elem = trace[Math.min(trace.length - 1, getStackDepth())];
                    message = String.format("%s[%d] : %s", elem.getFileName(), elem.getLineNumber(), msg);
                    method.invoke(clazz, tag, message);
                } else {
                    method.invoke(clazz, tag, msg);
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * その他のシステム用標準Logger
     */
    static class BasicLogger implements Impl {
        @Override
        public void out(int level, String tag, String msg) {
            StackTraceElement[] trace = new Exception().getStackTrace();
            StackTraceElement elem = trace[Math.min(trace.length - 1, 2)];
            if (level == LEVEL_ERROR) {
                System.err.println(String.format("%s[%d] | %s | %s", elem.getFileName(), elem.getLineNumber(), tag, msg));
            } else {
                System.out.println(String.format(
                        level == LEVEL_DEBUG ? "[DBG] | %s[%d] | %s | %s" : "%s[%d] | %s | %s"
                        , elem.getFileName(), elem.getLineNumber(), tag, msg));
            }
        }
    }

    public static class RobolectricLogger implements Impl {

        protected int getStackDepth() {
            return 2;
        }

        @Override
        public void out(int level, String tag, String msg) {

            switch (level) {
                case Logger.LEVEL_INFO:
                    tag = "I/" + tag;
                    break;
                case Logger.LEVEL_ERROR:
                    tag = "E/" + tag;
                    break;
                default:
                    tag = "D/" + tag;
                    break;
            }

            try {
                StackTraceElement[] trace = new Exception().getStackTrace();
                StackTraceElement elem = trace[Math.min(trace.length - 1, getStackDepth())];
                if (level == Logger.LEVEL_ERROR) {
                    System.err.println(String.format("%s | %s[%d] : %s", tag, elem.getFileName(), elem.getLineNumber(), msg));
                } else {
                    System.out.println(String.format("%s | %s[%d] : %s", tag, elem.getFileName(), elem.getLineNumber(), msg));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void out(int level, String tag, String fmt, Object... args) {
        sLogger.out(level, tag, String.format(Locale.US, fmt, args));
    }

    public static void out(String tag, Exception e) {
        e.printStackTrace();
    }

    /**
     * ロガーを更新する
     */
    public static void setLogger(Impl logger) {
        sLogger = logger;
    }
}
