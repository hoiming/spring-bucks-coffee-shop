package com.haiming.springbuckscoffeeshop.services;

import com.haiming.springbuckscoffeeshop.beans.Coffee;
import com.haiming.springbuckscoffeeshop.repositories.CoffeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.exact;

@Service
@CacheConfig(cacheNames = "coffee")
public class CoffeeService {

    @Autowired
    private CoffeeRepository coffeeRepository;

    private static final String CACHE = "springbucks-coffee";

    @Autowired
    private RedisTemplate<String, Coffee> redisTemplate;
    public Optional<Coffee> findOneCoffee(String name){
        HashOperations<String, String, Coffee> hashOperations = redisTemplate.opsForHash();
        if(redisTemplate.hasKey(CACHE) && hashOperations.hasKey(CACHE, name)){
            System.out.println("Get coffee from redis. " + Optional.of(hashOperations.get(CACHE, name)));
            return Optional.of(hashOperations.get(CACHE, name));
        }
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withMatcher("name", exact().ignoreCase());
        Coffee coffeeExample = new Coffee();
        coffeeExample.setName(name);
        Optional<Coffee> coffee = coffeeRepository.findOne(Example.of(coffeeExample, exampleMatcher));
        coffee.ifPresent(c -> {
            System.out.println("Put coffee to redis " + coffee);
            hashOperations.put(CACHE, name, c);
            redisTemplate.expire(CACHE, 1, TimeUnit.MINUTES);
        });
        return coffee;
    }

    @Cacheable
    public List<Coffee> findAllCoffee(){
        return coffeeRepository.findAll();
    }

    @CacheEvict
    public void reloadCoffee(){

    }
}
