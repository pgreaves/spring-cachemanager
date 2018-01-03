package pgreaves.cachemanager;

import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "caching")
class CacheSettings {

    private Map<String, Spec> spec;

    public Map<String, Spec> getSpec() {
        return spec;
    }

    public void setSpec(Map<String, Spec> spec) {
        this.spec = spec;
    }

    public static class Spec {

        private String timeout;

        private int max = 100;

        private CacheType type;

        public String getTimeout() {
            return timeout;
        }

        public void setTimeout(String timeout) {
            this.timeout = timeout;
        }

        public int getMax() {
            return max;
        }

        public void setMax(int max) {
            this.max = max;
        }

        public CacheType getType() {
            return type;
        }

        public void setType(CacheType type) {
            this.type = type;
        }
    }
}
