package com.example.demo;

import com.example.demo.configuration.Shop;
import com.example.demo.domain.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class DemoApplication {


	public static void main(String[] args) {
		ConfigurableApplicationContext applicationContext = SpringApplication.run(DemoApplication.class, args);
		String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
		for (String name : beanDefinitionNames) {
			System.out.println(name);
		}
		Shop shop = applicationContext.getBean("shop", Shop.class);
		shop.placeOrder(createOrder());
	}

	private static Order createOrder() {
		Book book = new Book("Of Mice & Men", "Bluebird", new BigDecimal("100"), 1988, "John Steinbeck");
		MusicCD cd = new MusicCD("Off the Wall", "publisher", new BigDecimal("100"), 1975, "Michael Jackson");
		Software macos = new Software("Mavericks", "publisher", new BigDecimal("100"), 2014, "10.9.3");
		OrderItem bookItems = new OrderItem(book);
		OrderItem cdItems = new OrderItem(cd);
		OrderItem swItems = new OrderItem(macos);
		final List<OrderItem> orderItems = new ArrayList<OrderItem>();
		orderItems.add(bookItems);
		orderItems.add(cdItems);
		orderItems.add(swItems);
		Order order = new Order();
		order.setOrderItems(orderItems);
		return order;
	}

}
