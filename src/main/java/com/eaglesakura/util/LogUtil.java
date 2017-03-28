package com.eaglesakura.util;

import com.eaglesakura.log.Logger;

/**
 * ログ出力を制御する。
 *
 * このクラスは互換性のために残されている。
 * {@link Logger} を推奨する。
 */
@Deprecated
public class LogUtil extends Logger {
    public static final int LOGGER_LEVEL_INFO = 1;
    public static final int LOGGER_LEVEL_DEBUG = 2;
    public static final int LOGGER_LEVEL_ERROR = 3;

    /**
     * ロガーを設定する。
     */
    @Deprecated
    public static void setLogger(Impl logger) {
        // not work!
    }

    @Deprecated
    public static void out(String tag, String fmt, Object... args) {
        out(LEVEL_DEBUG, tag, fmt, args);
    }

}
