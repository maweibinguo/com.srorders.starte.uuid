package com.srorders.starter;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SignGenerator {

    /**
     * 申请64位内存
     */
    public static final int BITS_FULL = 64;

    /**
     * uuid
     */
    public static final String BITS_FULL_NAME = "id";

    /**
     * 1位符号位
     */
    public static final int BITS_PREFIX = 1;

    /**
     * 41时间位
     */
    public static final int BITS_TIME = 41;

    /**
     * 时间位名称
     */
    public static final String BITS_TIME_NAME = "diffTime";

    /**
     * 产生的时间
     */
    public static final String BITS_GENERATE_TIME_NAME = "generateTime";

    /**
     * 5个服务器位
     */
    public static final int BITS_SERVER = 5;

    /**
     * 服务位名称
     */
    public static final String BITS_SERVER_NAME = "serverId";

    /**
     * 5个worker位
     */
    public static final int BITS_WORKER = 5;

    /**
     * worker位名称
     */
    public static final String BITS_WORKER_NAME = "workerId";

    /**
     * 12个自增位
     */
    public static final int BITS_SEQUENCE = 12;

    /**
     * 自增位名称
     */
    public static final String BITS_SEQUENCE_NAME = "sequenceNumber";


    /**
     * uuid配置
     */
    private UuidProperties uuidProperties;

    /**
     * redis client
     */
    private StringRedisTemplate redisTemplate;

    /**
     * 构造
     *
     * @param uuidProperties
     */
    public SignGenerator(UuidProperties uuidProperties, StringRedisTemplate redisTemplate) {
        this.uuidProperties = uuidProperties;
        this.redisTemplate = redisTemplate;
    }

    private long getStaterOffsetTime() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(uuidProperties.getOffsetTime(), dateTimeFormatter).toInstant(OffsetDateTime.now().getOffset())
                .toEpochMilli();
    }

    /**
     * 获取uuid
     *
     * @return
     */
    public Map<String, Long> getNumber() throws InterruptedException {
        HashMap<String, Long> result = new HashMap<>();
        do {
            long id = 0L;
            long diffTime = Instant.now().toEpochMilli() - this.getStaterOffsetTime();
            long maxDiffTime = (long) (Math.pow(2, BITS_TIME) - 1);

            if (diffTime > maxDiffTime) {
                throw new RuntimeException(String.format("the offsetTime: %s is too small", uuidProperties.getOffsetTime()));
            }

            // 对时间位进行计算
            int shift = BITS_FULL - BITS_PREFIX - BITS_TIME;
            id |= diffTime << shift;
            result.put(BITS_TIME_NAME, diffTime);

            // 对server进行计算
            shift = shift - BITS_SERVER;
            id |= uuidProperties.getServerId() << shift;
            result.put(BITS_SERVER_NAME, uuidProperties.getServerId());

            // 对worker进行计算
            shift = shift - BITS_WORKER;
            id |= uuidProperties.getWorkerId() << shift;
            result.put(BITS_WORKER_NAME, uuidProperties.getWorkerId());

            // 对sequence进行计算
            Long sequence = this.getSequence("uuid_" + diffTime);
            long maxSequence = (long) (Math.pow(2, BITS_SEQUENCE) - 1);
            if (sequence > maxSequence) {
                Thread.sleep(1);
            } else {
                id |= sequence;
                result.put(BITS_SEQUENCE_NAME, sequence);
                result.put(BITS_FULL_NAME, id);
                return result;
            }
        } while (true);
    }

    /**
     * 获取自增id
     *
     * @param id
     * @return
     */
    private Long getSequence(String id) {
        String lua = " local sequenceKey = KEYS[1]; " +
                "local sequenceNumber = redis.call(\"incr\", sequenceKey); " +
                "redis.call(\"pexpire\", sequenceKey, 100); " +
                "return sequenceNumber";
        RedisScript<Long> redisScript = RedisScript.of(lua, Long.class);
        return redisTemplate.execute(redisScript, Collections.singletonList(id));
    }

    /**
     * 反解id
     *
     * @param id
     * @return
     */
    public Map<String, Long> reverseNumber(Long id) {
        HashMap<String, Long> result = new HashMap<>();

        //time
        int shift = BITS_FULL - BITS_PREFIX - BITS_TIME;
        Long diffTime = (id >> shift) & (long) (Math.pow(2, BITS_TIME) - 1);
        result.put(BITS_TIME_NAME, diffTime);

        //generateTime
        Long generateTime = diffTime + this.getStaterOffsetTime();
        result.put(BITS_GENERATE_TIME_NAME, generateTime);

        //server
        shift = shift - BITS_SERVER;
        Long server = (id >> shift) & (long) (Math.pow(2, BITS_SERVER) - 1);
        result.put(BITS_SERVER_NAME, server);

        //worker
        shift = shift - BITS_WORKER;
        Long worker = (id >> shift) & (long) (Math.pow(2, BITS_WORKER) - 1);
        result.put(BITS_WORKER_NAME, worker);

        //sequence
        Long sequence = id & (long) (Math.pow(2, BITS_SEQUENCE) - 1);
        result.put(BITS_SEQUENCE_NAME, sequence);
        return result;
    }
}
