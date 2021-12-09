package com.srorders.starter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author zero
 */
@Configuration
@EnableConfigurationProperties(UuidProperties.class)
@ConditionalOnClass({StringRedisTemplate.class, UuidProperties.class})
public class UuidConfiguration {

   @Bean
   @ConditionalOnMissingBean(SignGenerator.class)
   public SignGenerator signGenerator(UuidProperties  uuidProperties, StringRedisTemplate stringRedisTemplate) {
       return new SignGenerator(uuidProperties, stringRedisTemplate);
   }
}
