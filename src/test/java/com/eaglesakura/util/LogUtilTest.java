package com.eaglesakura.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class LogUtilTest {
    @Test
    public void ログ呼び出しが行える() {
        LogUtil.out("Test", "Hello World");
    }

    @Test(expected = IllegalArgumentException.class)
    public void デフォルトロガーにnullを渡すことはできない() {
        LogUtil.setLogger(null);
        fail();
    }
}
