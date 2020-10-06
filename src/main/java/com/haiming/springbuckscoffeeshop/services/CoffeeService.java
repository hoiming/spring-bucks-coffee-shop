package com.haiming.springbuckscoffeeshop.services;

import com.haiming.springbuckscoffeeshop.beans.Coffee;
import com.haiming.springbuckscoffeeshop.repositories.CoffeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import java.util.Optional;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.exact;

public class CoffeeService {

    @Autowired
    private CoffeeRepository coffeeRepository;

    public Optional<Coffee> findOneCoffee(String name){
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withMatcher("name", exact().ignoreCase());
        Coffee coffeeExample = new Coffee();
        coffeeExample.setName(name);
        Optional<Coffee> coffee = coffeeRepository.findOne(Example.of(coffeeExample, exampleMatcher));
        return coffee;
    }
}
