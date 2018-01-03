package pgreaves.cachemanager;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

@ConditionalOnMissingBean(CacheManager.class)
@EnableConfigurationProperties(CacheSettings.class)
@Configuration
public class CacheAutoConfiguration {

    @ConditionalOnMissingBean(CacheManager.class)
    @Bean
    CacheManager cacheManager(CacheSettings settings) {
        SimpleCacheManager manager = new SimpleCacheManager();

        List<Cache> caches = settings.getSpec().entrySet().stream()
                .map(entry -> buildCache(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        manager.setCaches(caches);

        return manager;
    }

    private Cache buildCache(String name, CacheSettings.Spec spec) {
        CacheType type = spec.getType();

        switch (type) {
            case CAFFEINE:
                return newCaffeineCache(name, spec);
            case NONE:
            case COUCHBASE:
            case EHCACHE:
            case GENERIC:
            case HAZELCAST:
            case INFINISPAN:
            case JCACHE:
            case REDIS:
            case SIMPLE:
            case GUAVA:
                throw new NotImplementedException();
            default:
                throw new IllegalArgumentException("Unknown cache type: " + type);
        }
    }

    private CaffeineCache newCaffeineCache(String name, CacheSettings.Spec settings) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder();

        long expirationTime = parseDuration("timeout", settings.getTimeout());
        TimeUnit expirationTimeUnit = parseTimeUnit("timeout", settings.getTimeout());
        builder.expireAfterWrite(expirationTime, expirationTimeUnit);

        builder.maximumSize(settings.getMax());
        builder.ticker(ticker());

        return new CaffeineCache(name, builder.build());
    }

    @Bean
    Ticker ticker() {
        return Ticker.systemTicker();
    }

    /**
     * Returns a parsed long value.
     */
    static long parseLong(String key, String value) {
        assertArgument(key, value);

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format(
                    "key %s value was set to %s, must be a long", key, value), e);
        }
    }

    /**
     * Returns a parsed duration value.
     */
    static long parseDuration(String key, String value) {
        assertArgument(key, value);
        return parseLong(key, value.substring(0, value.length() - 1));
    }

    /**
     * Returns a parsed {@link TimeUnit} value.
     */
    static TimeUnit parseTimeUnit(String key, String value) {
        assertArgument(key, value);

        char lastChar = Character.toLowerCase(value.charAt(value.length() - 1));

        switch (lastChar) {
            case 'd':
                return TimeUnit.DAYS;
            case 'h':
                return TimeUnit.HOURS;
            case 'm':
                return TimeUnit.MINUTES;
            case 's':
                return TimeUnit.SECONDS;
            default:
                throw new IllegalArgumentException(String.format(
                        "key %s invalid format; was %s, must end with one of [dDhHmMsS]", key, value));
        }
    }

    private static void assertArgument(String key, String value) {
        notNull(value, "value of key " + key + " ommited");
        isTrue(!value.isEmpty(), "value of key " + key + " ommited");
    }
}
