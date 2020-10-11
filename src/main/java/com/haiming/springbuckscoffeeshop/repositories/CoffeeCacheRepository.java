package com.haiming.springbuckscoffeeshop.repositories;

import com.haiming.springbuckscoffeeshop.beans.Coffee;
import com.haiming.springbuckscoffeeshop.beans.CoffeeCache;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CoffeeCacheRepository extends CrudRepository<CoffeeCache, Long> {
    Optional<CoffeeCache> findOneByName(String name);
}
