package com.srorders.starter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;


@SpringBootTest
class SignGeneratorTest {

    @Autowired
    SignGenerator signGenerator;

    @Test
    public void testGetNum() throws InterruptedException {
        Map<String, Long> num = this.signGenerator.getNumber();
        System.out.println(num);
    }

    @Test
    public void testReversNumber() throws InterruptedException {
        Map<String, Long> item = this.signGenerator.getNumber();
        Map<String, Long> data = this.signGenerator.reverseNumber(item.get(SignGenerator.BITS_FULL_NAME));
        System.out.println(data);
    }
}