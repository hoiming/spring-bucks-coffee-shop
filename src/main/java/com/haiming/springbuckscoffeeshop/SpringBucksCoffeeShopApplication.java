package com.haiming.springbuckscoffeeshop;

import com.haiming.springbuckscoffeeshop.beans.Coffee;
import com.haiming.springbuckscoffeeshop.beans.CoffeeOrder;
import com.haiming.springbuckscoffeeshop.beans.OrderState;
import com.haiming.springbuckscoffeeshop.repositories.CoffeeOrderRepository;
import com.haiming.springbuckscoffeeshop.repositories.CoffeeRepository;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableJpaRepositories
public class SpringBucksCoffeeShopApplication implements CommandLineRunner {

	@Autowired
	private CoffeeRepository coffeeRepository;

	@Autowired
	private CoffeeOrderRepository coffeeOrderRepository;
	public static void main(String[] args) {
		SpringApplication.run(SpringBucksCoffeeShopApplication.class, args);
	}

	@Override
	@Transactional
	public void run(String... args) throws Exception {
		initOrders();
		findOrders();
	}

	private void findOrders() {
		System.out.println("loading coffee menu");
		coffeeRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
				.forEach(c -> System.out.println(c));
		List<CoffeeOrder> list = coffeeOrderRepository.findTop3ByOrderByUpdateTimeDescIdAsc();
		System.out.println("findTop3ByOrderByUpdateTimeDescIdAsc: " + getJoinedOrderId(list));
		list = coffeeOrderRepository.findByCustomerOrderById("Li Lei");
		System.out.println("findByCustomerOrderById: " + getJoinedOrderId(list));
		list.forEach(o -> {
			System.out.println("Order " + o.getId());
			o.getItems().forEach(i -> System.out.println(i));
		});
		list = coffeeOrderRepository.findByItems_Name("latte");
		System.out.println("findByItems_Name: " + getJoinedOrderId(list));

	}

	private String getJoinedOrderId(List<CoffeeOrder> list) {
		return list.stream().map(o -> o.getId().toString()).collect(Collectors.joining(","));
	}

	private void initOrders(){
		Coffee espresso = new Coffee();
		espresso.setName("espresso");
		espresso.setPrice(Money.of(CurrencyUnit.of("CNY"), 20.0));
		coffeeRepository.save(espresso);
		Coffee latte = new Coffee();
		latte.setName("latte");
		latte.setPrice(Money.of(CurrencyUnit.of("CNY"), 30.0));
		coffeeRepository.save(latte);

		CoffeeOrder order = new CoffeeOrder();
		order.setCustomer("Li Lei");
		order.setItems(Arrays.asList(espresso, latte));
		order.setState(OrderState.INIT);
		coffeeOrderRepository.save(order);
	}
}
