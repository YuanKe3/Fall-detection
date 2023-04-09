package com.example.arr_pose1;

import org.junit.Test;

import static org.junit.Assert.*;

import android.content.Context;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    private String FAKE_STRING = "HELLO WORLD";
    private Context mockContext;

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
    @Test
    public void mul_isCorrect() {
        assertEquals(10, 13 - 3);
    }
}