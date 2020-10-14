package com.haiming.springbuckscoffeeshop.repositories;

import com.haiming.springbuckscoffeeshop.beans.Coffee;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CoffeeRepository extends BaseRepository<Coffee, Long> {
    List<Coffee> findByNameInOrderById(List<String> list);
}
