package com.haiming.springbuckscoffeeshop.controllers;

import com.haiming.springbuckscoffeeshop.beans.Coffee;
import com.haiming.springbuckscoffeeshop.services.CoffeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/coffee")
public class CoffeeController {

    @Autowired
    private CoffeeService coffeeService;

    @GetMapping("/")
    public List<Coffee> getall(){
        return coffeeService.findAllCoffee();
    }
}
