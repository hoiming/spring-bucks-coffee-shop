package com.haiming.springbuckscoffeeshop.repositories;

import com.haiming.springbuckscoffeeshop.documents.Coffee;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CoffeeMongoRepository extends MongoRepository<Coffee, Long> {
    List<Coffee> findByName(String name);
}
