### Estrutura do Projeto

Sua estrutura de pastas deve estar organizada da seguinte forma:

```
architecture/
├── pom.xml (projeto pai)
├── docker-compose.yml (unificado)
├── eureka-server/
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── backend-service/
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
└── client-service/
    ├── src/
    ├── pom.xml
    └── Dockerfile
```

---

### Passos para Construção e Execução

#### 1. **Construir os Projetos com Maven**

No diretório raiz do projeto pai (`architecture`), execute o seguinte comando para construir todos os submódulos:

```bash
mvn clean install
```

Isso irá compilar e empacotar cada submódulo (`eureka-server`, `backend-service`, `client-service`) gerando os arquivos `.jar` na pasta `target` de cada um.

---

#### 2. **Configurar os Dockerfiles**

Certifique-se de que cada submódulo tenha um `Dockerfile` para construir a imagem Docker do respectivo serviço. Aqui estão exemplos de `Dockerfile` para cada submódulo:

**Dockerfile (eureka-server)**:
```dockerfile
FROM openjdk:17-jdk-alpine
COPY target/eureka-server-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Dockerfile (backend-service)**:
```dockerfile
FROM openjdk:17-jdk-alpine
COPY target/backend-service-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Dockerfile (client-service)**:
```dockerfile
FROM openjdk:17-jdk-alpine
COPY target/client-service-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

#### 3. **Configurar o `docker-compose.yml` Unificado**

No diretório raiz do projeto pai (`architecture`), crie ou edite o arquivo `docker-compose.yml` para incluir todos os serviços. Aqui está um exemplo completo:

```yaml
version: '3'
services:
  eureka-server:
    build:
      context: ./eureka-server
    ports:
      - "8761:8761"
    networks:
      - app-network

  backend-service:
    build:
      context: ./backend-service
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    ports:
      - "8081:8081"
    depends_on:
      - eureka-server
      - redis
    networks:
      - app-network

  client-service:
    build:
      context: ./client-service
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    ports:
      - "8082:8082"
    depends_on:
      - eureka-server
    networks:
      - app-network

  redis:
    image: redis:alpine
    ports:
      - "6379:6379"
    networks:
      - app-network

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:7.10.0
    environment:
      - discovery.type=single-node
    ports:
      - "9200:9200"
    networks:
      - app-network

  logstash:
    image: docker.elastic.co/logstash/logstash:7.10.0
    volumes:
      - ./logstash.conf:/usr/share/logstash/pipeline/logstash.conf
    ports:
      - "5000:5000"
    depends_on:
      - elasticsearch
    networks:
      - app-network

  kibana:
    image: docker.elastic.co/kibana/kibana:7.10.0
    ports:
      - "5601:5601"
    depends_on:
      - elasticsearch
    networks:
      - app-network

  prometheus:
    image: prom/prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
    networks:
      - app-network

  grafana:
    image: grafana/grafana
    ports:
      - "3000:3000"
    depends_on:
      - prometheus
    networks:
      - app-network

networks:
  app-network:
    driver: bridge
```

---

#### 4. **Subir o Sistema com Docker Compose**

No diretório raiz do projeto pai (`architecture`), execute o seguinte comando para construir as imagens Docker e iniciar todos os serviços:

```bash
docker-compose up --build
```

O parâmetro `--build` garante que as imagens Docker sejam reconstruídas antes de iniciar os contêineres.

---

#### 5. **Acessar os Serviços**

Após subir os contêineres, você pode acessar os serviços nos seguintes endereços:

- **Eureka Server**: `http://localhost:8761`
- **Backend Service**: `http://localhost:8081`
- **Client Service**: `http://localhost:8082`
- **Redis**: `http://localhost:6379`
- **Elasticsearch**: `http://localhost:9200`
- **Kibana**: `http://localhost:5601`
- **Prometheus**: `http://localhost:9090`
- **Grafana**: `http://localhost:3000`

---

#### 6. **Parar o Sistema**

Para parar e remover todos os contêineres, use o comando:

```bash
docker-compose down
```

---

### Considerações Finais

1. **Modularidade**: A estrutura com um projeto pai e submódulos permite que cada serviço seja desenvolvido e testado de forma independente.
2. **Docker Compose Unificado**: O uso de um único `docker-compose.yml` facilita a orquestração de todos os serviços.
3. **Escalabilidade**: Você pode adicionar mais submódulos (por exemplo, novos microsserviços) ou serviços de monitoramento conforme necessário.
4. **Clean Code**: Mantenha o código organizado e siga boas práticas de desenvolvimento em cada submódulo.
