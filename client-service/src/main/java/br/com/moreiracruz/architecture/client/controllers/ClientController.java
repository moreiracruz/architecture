package br.com.moreiracruz.architecture.client.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
public class ClientController {

	@Autowired
	private RestTemplate restTemplate;

	@GetMapping("/data")
	public String getData() {
		return restTemplate.getForObject("http://backend-service/api/data", String.class);
	}
	
}
