package com.haiming.springbuckscoffeeshop;

import com.haiming.springbuckscoffeeshop.beans.Coffee;
import com.haiming.springbuckscoffeeshop.beans.CoffeeOrder;
import com.haiming.springbuckscoffeeshop.beans.OrderState;
import com.haiming.springbuckscoffeeshop.converter.BytesToMoneyConverter;
import com.haiming.springbuckscoffeeshop.converter.MoneyReadConverter;
import com.haiming.springbuckscoffeeshop.converter.MoneyToBytesConverter;
import com.haiming.springbuckscoffeeshop.converter.MoneyWriteConverter;
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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.convert.RedisCustomConversions;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@SpringBootApplication
@EnableJpaRepositories
@EnableCaching(proxyTargetClass = true)
@EnableRedisRepositories

public class SpringBucksCoffeeShopApplication implements CommandLineRunner {

	@Autowired
	private CoffeeRepository coffeeRepository;

	@Autowired
	private CoffeeMongoRepository coffeeMongoRepository;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private ReactiveMongoTemplate reactiveMongoTemplate;

	@Autowired
	private CoffeeOrderRepository coffeeOrderRepository;

	@Autowired
	private JedisPool jedisPool;

	@Autowired
	private CoffeeService coffeeService;
	private static final String KEY = "COFFEE_MENU";

	@Autowired
	private ReactiveStringRedisTemplate redisTemplate;
	private CountDownLatch cdl = new CountDownLatch(2);

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
		testReactorMongoDB();
	}

	private void testReactorMongoDB() throws InterruptedException {
		startFromInsertion(() -> {
			System.out.println("Runnable");
			decreaseHighPrice();
		});
		System.out.println("after starting");
		cdl.await();
	}

	private void decreaseHighPrice() {
		reactiveMongoTemplate.updateMulti(Query.query(where("price").gte(3000L)),
				new Update().inc("price", -500L).currentDate("updateTime"), Coffee.class)
				.doFinally(s ->
				{
					cdl.countDown();
					System.out.println("Finally 2, " + s);
				}).subscribe(r -> System.out.println("result is " + r));
	}

	public void startFromInsertion(Runnable runnable){
		reactiveMongoTemplate.insertAll(coffeeService.findAllCoffee())
				.publishOn(Schedulers.elastic())
				.doOnNext(c -> System.out.println("Next: " + c))
				.doOnComplete(runnable)
				.doFinally(s -> {
					cdl.countDown();
					System.out.println("Finally 1, " + s);
				})
				.count()
				.subscribe(c -> System.out.println("Insert " + c + " records"));

	}

	private void testRedisOnReactor() throws InterruptedException {
		List<Coffee> coffeeList = coffeeService.findAllCoffee();
		CountDownLatch latch = new CountDownLatch(1);
		ReactiveHashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
		Flux.fromIterable(coffeeList)
				.publishOn(Schedulers.single())
				.doOnComplete(() -> System.out.println("list ok"))
				.flatMap(c -> {
					System.out.println("try put " + c);
					return hashOps.put(KEY, c.getName(), c.getPrice().toString());
				})
				.doOnComplete(() -> System.out.println("set ok"))
				.concatWith(redisTemplate.expire(KEY, Duration.ofMinutes(1)))
				.doOnComplete(() -> System.out.println("expire ok"))
				.onErrorResume(e -> {
					System.err.println("exception " + e.getMessage());
					return Mono.just(false);
				})
				.subscribe(b -> System.out.println("Boolean: " + b),
						e -> System.err.println("Exception " + e.getMessage()),
						() -> latch.countDown());
		System.out.println("waiting");
		latch.await();

	}

	private void testRedisRepository() {
		Optional<Coffee> coffee = coffeeService.findSimpleCoffeeFromCache("espresso");
		System.out.println("Coffee " + coffee);
		for(int i = 0; i < 5; i++){
			coffee = coffeeService.findSimpleCoffeeFromCache("espresso");
		}
		System.out.println("Value from redis: " + coffee);
	}

	private void testRedisTemplate() {
		Optional<Coffee> coffee = coffeeService.findOneCoffee("espresso");
		System.out.println("Coffee " + coffee);
		for(int i = 0; i < 5; i++){
			coffee = coffeeService.findOneCoffee("espresso");
		}
		System.out.println("Value from redis: " + coffee);
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
		List<com.haiming.springbuckscoffeeshop.documents.Coffee> list = mongoTemplate.find(Query.query(where("name").is("foobar")), com.haiming.springbuckscoffeeshop.documents.Coffee.class);
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
		return new MongoCustomConversions(Arrays.asList(new MoneyReadConverter(), new MoneyWriteConverter()));
	}

	@Bean
	public RedisTemplate<String, Coffee> redisTemplate(RedisConnectionFactory redisConnectionFactory){
		RedisTemplate<String, Coffee> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory);
		return template;
	}

	@Bean
	public RedisCustomConversions redisCustomConversions(){
		return new RedisCustomConversions(Arrays.asList(new MoneyToBytesConverter(), new BytesToMoneyConverter()));
	}
}
