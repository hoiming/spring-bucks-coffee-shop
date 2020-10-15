package com.haiming.springbuckscoffeeshop.viewmodels;

import org.joda.money.Money;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class NewCoffeeRequest {
    @NotEmpty
    private String name;
    @NotNull
    private Money price;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Money getPrice() {
        return price;
    }

    public void setPrice(Money price) {
        this.price = price;
    }
}
