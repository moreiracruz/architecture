### Estrutura do Projeto

1. **Service Discovery**: Usaremos o **Eureka** do Spring Cloud para service discovery.
2. **Balanceamento de Carga**: Usaremos o **Ribbon** ou **Spring Cloud LoadBalancer** para balanceamento de carga.
3. **Cache**: Usaremos **Redis** para cache.
4. **Tolerância a Falhas**: Usaremos **Hystrix** ou **Resilience4j** para circuit breaker.
5. **Monitoramento de Logs**: Usaremos **ELK Stack** (Elasticsearch, Logstash, Kibana) ou **Prometheus** e **Grafana**.
6. **Docker**: Tudo será conteinerizado usando Docker.
7. **Clean Code**: Seguiremos boas práticas de clean code.

### Passos para Implementação

#### 1. Configuração do Service Discovery (Eureka)

Crie um projeto Spring Boot para o servidor Eureka.

**pom.xml**:
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
    </dependency>
</dependencies>
```

**application.yml**:
```yaml
server:
  port: 8761

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

**EurekaServerApplication.java**:
```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

#### 2. Configuração do Servidor (Backend)

Crie um projeto Spring Boot para o servidor backend.

**pom.xml**:
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
    </dependency>
</dependencies>
```

**application.yml**:
```yaml
server:
  port: 8081

spring:
  application:
    name: backend-service
  redis:
    host: redis
    port: 6379

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

**BackendApplication.java**:
```java
@SpringBootApplication
@EnableEurekaClient
@EnableHystrix
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
```

**BackendController.java**:
```java
@RestController
@RequestMapping("/api")
public class BackendController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/data")
    @HystrixCommand(fallbackMethod = "fallbackMethod")
    public String getData() {
        String cachedData = redisTemplate.opsForValue().get("cachedData");
        if (cachedData != null) {
            return cachedData;
        }
        String data = "Data from backend";
        redisTemplate.opsForValue().set("cachedData", data);
        return data;
    }

    public String fallbackMethod() {
        return "Fallback data";
    }
}
```

#### 3. Configuração dos Clientes

Crie dois projetos Spring Boot para os clientes.

**pom.xml**:
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-ribbon</artifactId>
    </dependency>
</dependencies>
```

**application.yml**:
```yaml
server:
  port: 8082

spring:
  application:
    name: client-service

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

**ClientApplication.java**:
```java
@SpringBootApplication
@EnableEurekaClient
public class ClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }
}
```

**ClientController.java**:
```java
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
```

#### 4. Configuração do Docker

Crie um `Dockerfile` para cada serviço e um `docker-compose.yml` para orquestrar os contêineres.

**Dockerfile (Eureka)**:
```dockerfile
FROM openjdk:11-jre-slim
COPY target/eureka-server.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

**Dockerfile (Backend)**:
```dockerfile
FROM openjdk:11-jre-slim
COPY target/backend-service.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

**Dockerfile (Client)**:
```dockerfile
FROM openjdk:11-jre-slim
COPY target/client-service.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

**docker-compose.yml**:
```yaml
version: '3'
services:
  eureka-server:
    image: eureka-server
    build:
      context: ./eureka-server
    ports:
      - "8761:8761"

  backend-service:
    image: backend-service
    build:
      context: ./backend-service
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    ports:
      - "8081:8081"
    depends_on:
      - eureka-server
      - redis

  client-service:
    image: client-service
    build:
      context: ./client-service
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    ports:
      - "8082:8082"
    depends_on:
      - eureka-server

  redis:
    image: redis:alpine
    ports:
      - "6379:6379"
```

#### 5. Configuração de Monitoramento de Logs

Para monitoramento de logs, você pode usar o ELK Stack ou Prometheus e Grafana. Aqui está um exemplo básico de como configurar o ELK Stack.

**docker-compose.yml** (adicionar):
```yaml
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:7.10.0
    environment:
      - discovery.type=single-node
    ports:
      - "9200:9200"

  logstash:
    image: docker.elastic.co/logstash/logstash:7.10.0
    volumes:
      - ./logstash.conf:/usr/share/logstash/pipeline/logstash.conf
    ports:
      - "5000:5000"
    depends_on:
      - elasticsearch

  kibana:
    image: docker.elastic.co/kibana/kibana:7.10.0
    ports:
      - "5601:5601"
    depends_on:
      - elasticsearch
```

**logstash.conf**:
```conf
input {
  tcp {
    port => 5000
    codec => json_lines
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
  }
}
```

#### 6. Clean Code

Siga boas práticas de clean code, como:

- Nomes descritivos para classes, métodos e variáveis.
- Funções pequenas e com uma única responsabilidade.
- Evitar código duplicado.
- Uso adequado de comentários.
- Testes unitários e de integração.

