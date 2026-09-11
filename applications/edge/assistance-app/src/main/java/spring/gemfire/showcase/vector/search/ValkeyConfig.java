package spring.gemfire.showcase.vector.search;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@EnableCaching
public class ValkeyConfig {

    @Bean
    RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory)
    {
        var template = new RedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
    JedisConnectionFactory redisConnectionFactory()
    {
        return new JedisConnectionFactory();
    }
}
