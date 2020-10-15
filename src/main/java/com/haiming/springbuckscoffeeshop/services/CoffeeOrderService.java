package com.haiming.springbuckscoffeeshop.services;

import com.haiming.springbuckscoffeeshop.beans.Coffee;
import com.haiming.springbuckscoffeeshop.beans.CoffeeOrder;
import com.haiming.springbuckscoffeeshop.beans.OrderState;
import com.haiming.springbuckscoffeeshop.repositories.CoffeeOrderRepository;
import com.haiming.springbuckscoffeeshop.repositories.CoffeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class CoffeeOrderService {
    @Autowired
    private CoffeeOrderRepository orderRepository;

    public CoffeeOrder createOrder(String customer, Coffee... coffees){
        CoffeeOrder coffeeOrder = new CoffeeOrder();
        coffeeOrder.setCustomer(customer);
        coffeeOrder.setItems(Arrays.asList(coffees));
        coffeeOrder.setState(OrderState.INIT);
        CoffeeOrder saved = orderRepository.save(coffeeOrder);
        return saved;
    }

    public boolean updateState(CoffeeOrder order, OrderState state){
        if(state.compareTo(order.getState()) <= 0){
            System.out.println("Wrong order state transition " + order.getState() + " -> " + state);
            return false;
        }
        order.setState(state);
        orderRepository.save(order);
        return true;
    }

    public CoffeeOrder get(Long id){
        return orderRepository.getOne(id);
    }
}
