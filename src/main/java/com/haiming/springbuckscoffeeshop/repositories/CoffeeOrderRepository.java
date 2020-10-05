package com.haiming.springbuckscoffeeshop.repositories;

import com.haiming.springbuckscoffeeshop.beans.CoffeeOrder;


import java.util.List;


public interface CoffeeOrderRepository extends BaseRepository<CoffeeOrder, Long> {
    List<CoffeeOrder> findByCustomerOrderById(String customer);
    List<CoffeeOrder> findByItems_Name(String name);
}
