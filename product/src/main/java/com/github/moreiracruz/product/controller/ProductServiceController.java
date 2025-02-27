package com.github.moreiracruz.product.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductServiceController {

	@Autowired
	private DiscoveryClient discoveryClient;

//	@GetMapping("/services")
//	public List<String> getServices() {
//		return discoveryClient.getServices();
//	}

//	@GetMapping("/service-instances")
//	public List<ServiceInstance> getServiceInstances(@RequestParam String serviceId) {
//		return discoveryClient.getInstances(serviceId);
//	}

	@GetMapping("/products")
	public String getHello() {
		return "Hello World!";
	}
	
}
