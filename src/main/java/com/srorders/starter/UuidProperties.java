package com.srorders.starter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author zero
 */
@ConfigurationProperties("spring.uuid")
@Data
public class UuidProperties {

    private Long serverId = 0L;

    private Long workerId = 0L;

    private String offsetTime = "2021-12-07 00:00:00";
}
