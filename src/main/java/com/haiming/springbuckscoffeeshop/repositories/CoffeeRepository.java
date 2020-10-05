package com.haiming.springbuckscoffeeshop.repositories;

import com.haiming.springbuckscoffeeshop.beans.Coffee;
import org.springframework.data.repository.CrudRepository;

public interface CoffeeRepository extends BaseRepository<Coffee, Long> {
}
