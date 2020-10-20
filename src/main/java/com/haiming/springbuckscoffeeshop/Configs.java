package com.haiming.springbuckscoffeeshop;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.haiming.springbuckscoffeeshop.beans.Coffee;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.TimeZone;

@Configuration
public class Configs {

    @Bean
    public Hibernate5Module hibernate5Module(){
        return new Hibernate5Module();
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer(){
        return builder ->{
            builder.indentOutput(true);
            builder.timeZone(TimeZone.getTimeZone("Asin/Shanghai"));
        };
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder){
        return builder.build();
    }

}
