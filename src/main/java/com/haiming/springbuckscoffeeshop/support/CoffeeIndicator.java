package com.haiming.springbuckscoffeeshop.support;

import com.haiming.springbuckscoffeeshop.services.CoffeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CoffeeIndicator implements HealthIndicator {

    @Autowired
    private CoffeeService coffeeService;
    @Override
    public Health getHealth(boolean includeDetails) {
        long count = coffeeService.getCoffeeCount();
        Health health;
        if(count > 0){
            health = Health.up()
                    .withDetail("count", count)
                    .withDetail("message", "We have enough coffee")
                    .build();
        }else{
            health = Health.down()
                    .withDetail("count", "0")
                    .withDetail("message", "we are run out of coffee")
                    .build();
        }
        return health;
    }

    @Override
    public Health health() {
        return null;
    }
}
