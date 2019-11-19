package com.example.demo.configuration;

import com.example.demo.domain.Order;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway
public interface Shop {

	@Gateway(requestChannel="ordersChannel")
	void placeOrder(Order order);

}