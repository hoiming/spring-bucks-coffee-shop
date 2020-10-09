package com.haiming.springbuckscoffeeshop;

import com.haiming.springbuckscoffeeshop.beans.Coffee;
import com.haiming.springbuckscoffeeshop.beans.CoffeeOrder;
import com.haiming.springbuckscoffeeshop.beans.OrderState;
import com.haiming.springbuckscoffeeshop.converter.MoneyReadConverter;
import com.haiming.springbuckscoffeeshop.repositories.CoffeeMongoRepository;
import com.haiming.springbuckscoffeeshop.repositories.CoffeeOrderRepository;
import com.haiming.springbuckscoffeeshop.repositories.CoffeeRepository;
import com.haiming.springbuckscoffeeshop.services.CoffeeService;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableJpaRepositories
@EnableCaching(proxyTargetClass = true)
public class SpringBucksCoffeeShopApplication implements CommandLineRunner {

	@Autowired
	private CoffeeRepository coffeeRepository;

	@Autowired
	private CoffeeMongoRepository coffeeMongoRepository;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private CoffeeOrderRepository coffeeOrderRepository;

	@Autowired
	private JedisPool jedisPool;

	@Autowired
	private CoffeeService coffeeService;

	@Bean
	@ConfigurationProperties("redis")
	public JedisPoolConfig jedisPoolConfig(){
		return new JedisPoolConfig();
	}
	@Bean(destroyMethod = "close")
	public JedisPool jedisPool(@Value("${redis.host}") String host){
		return new JedisPool(jedisPoolConfig(), host);
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringBucksCoffeeShopApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		initOrders();
		testCacheSupport();
	}

	private void testCacheSupport() {
		System.out.println("Count: " + coffeeService.findAllCoffee().size());
		for(int i = 0 ; i < 5; i++){
			System.out.println("reading from cache");
			coffeeService.findAllCoffee();
		}
		coffeeService.reloadCoffee();
		System.out.println("Reading after refresh");
		coffeeService.findAllCoffee().forEach(c -> System.out.println(c));
	}

	private void testJedis() {
		try(Jedis jedis = jedisPool.getResource()){
			coffeeService.findAllCoffee().forEach(c ->
					jedis.hset("springbucks-menu", c.getName(), Long.toString(c.getPrice().getAmountMinorLong()))
			);
			Map<String, String> menu = jedis.hgetAll("springbucks-menu");
			System.out.println(menu);
			String price = jedis.hget("springbucks-menu", "espresso");
			System.out.println(price);
		}
	}

	private void testMongoDBRepository() {
		com.haiming.springbuckscoffeeshop.documents.Coffee espresso =
				new com.haiming.springbuckscoffeeshop.documents.Coffee();
		espresso.setUpdateTime(new Date());
		espresso.setCreateTime(new Date());
		espresso.setName("espresso");
		espresso.setPrice(Money.of(CurrencyUnit.of("CNY"), 30));
		com.haiming.springbuckscoffeeshop.documents.Coffee latte =
				new com.haiming.springbuckscoffeeshop.documents.Coffee();
		latte.setUpdateTime(new Date());
		latte.setCreateTime(new Date());
		latte.setName("latte");
		latte.setPrice(Money.of(CurrencyUnit.of("CNY"), 40));
		coffeeMongoRepository.insert(Arrays.asList(espresso, latte));
		coffeeMongoRepository.findByName("espresso").forEach(c -> System.out.println(c));
		coffeeMongoRepository.deleteAll();
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

	public void saveMongoCoffee(){
		com.haiming.springbuckscoffeeshop.documents.Coffee coffee = new com.haiming.springbuckscoffeeshop.documents.Coffee();
		coffee.setName("foobar");
		coffee.setPrice(Money.of(CurrencyUnit.of("CNY"), 100D));
		coffee.setCreateTime(new Date());
		coffee.setUpdateTime(new Date());
		com.haiming.springbuckscoffeeshop.documents.Coffee saved = mongoTemplate.save(coffee);
		System.out.println(saved);
		List<com.haiming.springbuckscoffeeshop.documents.Coffee> list = mongoTemplate.find(Query.query(Criteria.where("name").is("foobar")), com.haiming.springbuckscoffeeshop.documents.Coffee.class);
		System.out.println(list.size());
		list.forEach(c -> System.out.println(c));

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

	@Bean
	public MongoCustomConversions mongoCustomConversions(){
		return new MongoCustomConversions(Arrays.asList(new MoneyReadConverter()));
	}
}
