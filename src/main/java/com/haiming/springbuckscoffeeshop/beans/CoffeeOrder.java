package com.haiming.springbuckscoffeeshop.beans;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;


@Entity
@Table(name = "T_ORDER")
public class CoffeeOrder extends BaseEntity implements Serializable {
    private String customer;
    @ManyToMany
    @JoinTable(name = "T_COFFEE_ORDER")
    private List<Coffee> items;
    @Enumerated
    @Column(nullable = false)
    private OrderState state;

    public CoffeeOrder() {
    }
    public CoffeeOrder(Long id, String customer, List<Coffee> items, OrderState state, Date createTime, Date updatetime){
        this.id = id;
        this.customer = customer;
        this.items = items;
        this.createTime = createTime;
        this.updateTime = updatetime;
        this.state = state;
    }
}
