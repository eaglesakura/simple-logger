package com.eaglesakura.log;

import org.junit.Test;

public class LogUtilTest {
    @Test
    public void ログ呼び出しが行える() {
        Logger.out(Logger.LEVEL_DEBUG, "Test", "Hello World");
    }
}
