package com.eaglesakura.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * ログ出力を制御する。
 */
public final class LogUtil {
    private static Logger sLogger = null;
    private static Map<String, LogOpt> sOptions = new HashMap<>();

    public static final int LOGGER_LEVEL_INFO = 1;
    public static final int LOGGER_LEVEL_DEBUG = 2;
    public static final int LOGGER_LEVEL_ERROR = 3;

    private static class LogOpt {
        boolean enable = true;
        int level = LOGGER_LEVEL_INFO;
        Logger logger = sLogger;
    }

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

    public interface Logger {
        void out(int level, String tag, String msg);
    }

    /**
     * Android用Logger
     */
    public static class AndroidLogger implements Logger {
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
                    case LOGGER_LEVEL_INFO:
                        method = i;
                        break;
                    case LOGGER_LEVEL_ERROR:
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
    static class BasicLogger implements Logger {
        @Override
        public void out(int level, String tag, String msg) {
            StackTraceElement[] trace = new Exception().getStackTrace();
            StackTraceElement elem = trace[Math.min(trace.length - 1, 2)];
            if (level == LOGGER_LEVEL_ERROR) {
                System.err.println(String.format("%s[%d] | %s | %s", elem.getFileName(), elem.getLineNumber(), tag, msg));
            } else {
                System.out.println(String.format(
                        level == LOGGER_LEVEL_DEBUG ? "[DBG] | %s[%d] | %s | %s" : "%s[%d] | %s | %s"
                        , elem.getFileName(), elem.getLineNumber(), tag, msg));
            }
        }
    }

    private static synchronized LogOpt getOption(String tag) {
        LogOpt opt = sOptions.get(tag);
        if (opt == null) {
            opt = new LogOpt();
            sOptions.put(tag, opt);
        }
        return opt;
    }

    /**
     * ロガーを設定する。
     */
    public static void setLogger(Logger logger) {
        if (logger == null) {
            throw new IllegalArgumentException();
        }
        LogUtil.sLogger = logger;
    }

    /**
     * 出力タグのログ出力先を指定する
     */
    public static void setLogLevel(String tag, int level) {
        getOption(tag).level = level;
    }

    /**
     * タグ単位のロガーを指定する
     */
    public static void setLogger(String tag, Logger logger) {
        if (logger == null) {
            logger = sLogger;
        }
        getOption(tag).logger = logger;
    }

    /**
     * ログ出力の有無を指定する
     *
     * @param tag     対象タグ
     * @param enabled 出力する場合はtrue
     */
    public static void setLogEnable(String tag, boolean enabled) {
        getOption(tag).enable = enabled;
    }

    public static void out(String tag, String fmt, Object... args) {
        LogOpt opt = getOption(tag);
        if (!opt.enable) {
            return;
        }

        opt.logger.out(opt.level, tag, String.format(fmt, args));
    }

    public static void out(String tag, Exception e) {
        LogOpt opt = getOption(tag);
        if (!opt.enable) {
            return;
        }

        e.printStackTrace();
    }

    @Deprecated
    public static void log(String fmt, Object... args) {
        String tag = ".lib";
        LogOpt opt = getOption(tag);
        if (!opt.enable) {
            return;
        }

        opt.logger.out(opt.level, tag, String.format(fmt, args));
    }

    @Deprecated
    public static void log(Exception e) {
        LogOpt opt = getOption(".lib");
        if (!opt.enable) {
            return;
        }
        e.printStackTrace();
    }
}
