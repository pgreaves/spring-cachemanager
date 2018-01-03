# spring-cachemanager
Configurable Spring caches

## Configuration
In configuration file (application.yml) put:
```
...
caching:
  spec:
    cache_1:
      type: caffeine
      max: 256
      timeout: 1234567s
    cache_2:
      type: caffeine
      max: 100
      timeout: 30d
...
```
