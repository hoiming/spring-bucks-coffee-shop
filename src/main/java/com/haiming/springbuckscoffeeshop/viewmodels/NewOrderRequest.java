package com.haiming.springbuckscoffeeshop.viewmodels;

import java.util.List;

public class NewOrderRequest {
    private String customerName;
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
