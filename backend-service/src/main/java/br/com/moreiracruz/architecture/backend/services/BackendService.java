package br.com.moreiracruz.architecture.backend.services;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BackendService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @CircuitBreaker(name = "backendService", fallbackMethod = "fallback")
    public String data() {
        String cachedData = redisTemplate.opsForValue().get("cachedData");
        if (cachedData != null) {
            return cachedData;
        }
        String data = "Data from backend";
        redisTemplate.opsForValue().set("cachedData", data);
        return data;
    }

    public String fallback(Exception ex) {
        return "Serviço indisponível, retornando resposta de fallback";
    }

}
