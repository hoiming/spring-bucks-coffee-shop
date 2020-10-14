package com.haiming.springbuckscoffeeshop.controllers;

import com.haiming.springbuckscoffeeshop.beans.Coffee;
import com.haiming.springbuckscoffeeshop.beans.CoffeeOrder;
import com.haiming.springbuckscoffeeshop.services.CoffeeOrderService;
import com.haiming.springbuckscoffeeshop.services.CoffeeService;
import com.haiming.springbuckscoffeeshop.viewmodels.NewOrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class CoffeeOrderController {

    @Autowired
    private CoffeeService coffeeService;

    @Autowired
    private CoffeeOrderService orderService;

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public CoffeeOrder create(@RequestBody NewOrderRequest request){
        List<Coffee> coffeeList = coffeeService.findNameIn(request.getCoffeeNames());
        return orderService.createOrder(request.getCustomerName(), coffeeList.toArray(new Coffee[0]));

    }
}
