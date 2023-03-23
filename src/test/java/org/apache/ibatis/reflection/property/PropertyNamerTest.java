package org.apache.ibatis.reflection.property;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PropertyNamerTest {

    @Test
    public void test(){
        Assertions.assertEquals("lia", PropertyNamer.methodToProperty("getLia"));
        Assertions.assertEquals("lia", PropertyNamer.methodToProperty("getlia"));
        //第二个字符是大写的时候是不转换的
        Assertions.assertEquals("LLL", PropertyNamer.methodToProperty("getLLL"));
    }
}
