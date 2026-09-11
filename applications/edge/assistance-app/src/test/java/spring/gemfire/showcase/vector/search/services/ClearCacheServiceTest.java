package spring.gemfire.showcase.vector.search.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClearCacheServiceTest {

    private ClearCacheService subject;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;


    @BeforeEach
    void setUp() {
        subject = new ClearCacheService(redisTemplate);
    }

    @Test
    void clearCache() {


        subject.clearCache();

        verify(redisTemplate).execute(any(RedisCallback.class));
    }
}