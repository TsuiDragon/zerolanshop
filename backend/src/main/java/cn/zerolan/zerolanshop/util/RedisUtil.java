package cn.zerolan.zerolanshop.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ============================ 字符串 =============================

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? clazz.cast(value) : null;
    }

    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public boolean expire(String key, long timeout, TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
    }

    public Long getExpire(String key, TimeUnit unit) {
        return redisTemplate.getExpire(key, unit);
    }

    // ============================ 哈希 =============================

    public void hSet(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    public void hSetAll(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    public Object hGet(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public Long hDelete(String key, Object... hashKeys) {
        return redisTemplate.opsForHash().delete(key, hashKeys);
    }

    public boolean hHasKey(String key, String hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    // ============================ 列表 =============================

    public Long lPush(String key, Object value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    public Long lPushAll(String key, Collection<Object> values) {
        return redisTemplate.opsForList().leftPushAll(key, values);
    }

    public Long rPush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    public Object lPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    public Object rPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    public List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    // ============================ 集合 =============================

    public Long sAdd(String key, Object... members) {
        return redisTemplate.opsForSet().add(key, members);
    }

    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public Long sRemove(String key, Object... members) {
        return redisTemplate.opsForSet().remove(key, members);
    }

    public Boolean sIsMember(String key, Object member) {
        return redisTemplate.opsForSet().isMember(key, member);
    }

    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    // ============================ 有序集合 =============================

    public Boolean zAdd(String key, Object member, double score) {
        return redisTemplate.opsForZSet().add(key, member, score);
    }

    public Long zRemove(String key, Object... members) {
        return redisTemplate.opsForZSet().remove(key, members);
    }

    public Set<Object> zRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    public Set<Object> zRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    public Long zRank(String key, Object member) {
        return redisTemplate.opsForZSet().rank(key, member);
    }

    public Double zScore(String key, Object member) {
        return redisTemplate.opsForZSet().score(key, member);
    }

    // ============================ 分布式锁 =============================

    public boolean tryLock(String key, String value, long timeout, TimeUnit unit) {
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
        return Boolean.TRUE.equals(success);
    }

    public void unlock(String key) {
        redisTemplate.delete(key);
    }

    // ============================ 自增/自减 =============================

    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }
}
