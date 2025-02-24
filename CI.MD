### 1. **Configurar o GitHub Actions**

O GitHub Actions é uma ferramenta de integração contínua (CI) e entrega contínua (CD) integrada ao GitHub. Ele permite automatizar tarefas como construção, testes e implantação.

#### a) **Criar um Workflow no GitHub Actions**
No seu repositório GitHub, crie um diretório chamado `.github/workflows` e adicione um arquivo YAML para definir o workflow. Exemplo: `ci-cd.yml`.

```yaml
name: CI/CD Pipeline

on:
  push:
    branches:
      - main  # Executa o pipeline quando há um push na branch main
  pull_request:
    branches:
      - main  # Executa o pipeline quando há um pull request para a branch main

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Build with Maven
        run: mvn clean package -DskipTests

      - name: Login to Docker Hub
        uses: docker/login-action@v2
        with:
          username: ${{ secrets.DOCKER_HUB_USERNAME }}
          password: ${{ secrets.DOCKER_HUB_TOKEN }}

      - name: Build and push Docker images
        run: |
          docker-compose -f docker-compose.yml build
          docker-compose -f docker-compose.yml push
```

#### Explicação:
- **`on`**: Define quando o workflow será executado (push na branch `main` ou pull request).
- **`jobs`**: Contém as etapas do pipeline.
    - **Checkout code**: Baixa o código do repositório.
    - **Set up JDK 17**: Configura o Java 17.
    - **Build with Maven**: Compila o código com Maven.
    - **Login to Docker Hub**: Faz login no Docker Hub usando credenciais secretas.
    - **Build and push Docker images**: Constrói as imagens Docker e as envia para o Docker Hub.

#### b) **Adicionar Credenciais ao GitHub**
1. No seu repositório GitHub, vá para **"Settings"** > **"Secrets and variables"** > **"Actions"**.
2. Adicione as seguintes credenciais:
    - **`DOCKER_HUB_USERNAME`**: Seu nome de usuário no Docker Hub.
    - **`DOCKER_HUB_TOKEN`**: Um token de acesso do Docker Hub (gerado em **Account Settings** > **Security** > **New Access Token**).

---

### 2. **Configurar o Docker Hub**

O Docker Hub é um repositório de imagens Docker. Após construir as imagens no GitHub Actions, você pode enviá-las para o Docker Hub.

#### a) **Criar Repositórios no Docker Hub**
Crie repositórios no Docker Hub para cada serviço da sua aplicação (por exemplo, `eureka-server`, `backend-service`, `client-service`).

#### b) **Tag e Push das Imagens**
No workflow do GitHub Actions, as imagens são construídas e enviadas para o Docker Hub usando o comando `docker-compose push`. Certifique-se de que o `docker-compose.yml` esteja configurado para usar os nomes corretos dos repositórios.

Exemplo de `docker-compose.yml`:

```yaml
version: '3'
services:
  eureka-server:
    image: seu-usuario-dockerhub/eureka-server:latest
    build:
      context: ./eureka-server
    ports:
      - "8761:8761"

  backend-service:
    image: seu-usuario-dockerhub/backend-service:latest
    build:
      context: ./backend-service
    ports:
      - "8081:8081"

  client-service:
    image: seu-usuario-dockerhub/client-service:latest
    build:
      context: ./client-service
    ports:
      - "8082:8082"
```

Substitua `seu-usuario-dockerhub` pelo seu nome de usuário no Docker Hub.

---

### 3. **Automatizar a Implantação**

Após enviar as imagens para o Docker Hub, você pode automatizar a implantação em um servidor ou ambiente de produção. Aqui estão duas abordagens:

#### a) **Usando SSH no GitHub Actions**
Adicione uma etapa no workflow para fazer login no servidor e executar o `docker-compose up`.

Exemplo de etapa adicional no workflow:

```yaml
      - name: Deploy to server
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.SSH_HOST }}
          username: ${{ secrets.SSH_USERNAME }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          script: |
            docker-compose -f docker-compose.yml pull
            docker-compose -f docker-compose.yml up -d
```

Adicione as credenciais SSH ao GitHub Secrets:
- **`SSH_HOST`**: Endereço IP ou domínio do servidor.
- **`SSH_USERNAME`**: Nome de usuário para login SSH.
- **`SSH_PRIVATE_KEY`**: Chave privada SSH.

#### b) **Usando Webhooks e Scripts no Servidor**
1. Configure um webhook no GitHub para notificar o servidor sempre que houver um push na branch `main`.
2. No servidor, crie um script que faz o pull das imagens e executa o `docker-compose up`.

Exemplo de script no servidor (`deploy.sh`):

```bash
#!/bin/bash
docker-compose -f /caminho/para/docker-compose.yml pull
docker-compose -f /caminho/para/docker-compose.yml up -d
```

---

### 4. **Resumo**

1. **GitHub Actions**:
    - Crie um workflow para construir e testar o código.
    - Use o Docker Compose para construir e enviar as imagens para o Docker Hub.

2. **Docker Hub**:
    - Crie repositórios para cada serviço.
    - Configure o `docker-compose.yml` para usar os repositórios do Docker Hub.

3. **Automatizar a Implantação**:
    - Use SSH no GitHub Actions para implantar no servidor.
    - Ou configure webhooks e scripts no servidor para implantação automática.
