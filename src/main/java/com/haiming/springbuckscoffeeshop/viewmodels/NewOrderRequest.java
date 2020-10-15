package com.haiming.springbuckscoffeeshop.viewmodels;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public class NewOrderRequest {
    @NotNull
    private String customerName;
    @Size(min = 1)
    private List<String> coffeeNames;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public List<String> getCoffeeNames() {
        return coffeeNames;
    }

    public void setCoffeeNames(List<String> coffeeNames) {
        this.coffeeNames = coffeeNames;
    }
}
