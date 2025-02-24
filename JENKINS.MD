### 1. **Adicionar Jenkins ao `docker-compose.yml`**

Primeiro, adicione o Jenkins ao seu arquivo `docker-compose.yml`. Aqui está um exemplo de configuração:

```yaml
version: '3'
services:
  jenkins:
    image: jenkins/jenkins:lts
    container_name: jenkins
    ports:
      - "8080:8080"  # Porta para acessar a interface do Jenkins
      - "50000:50000"  # Porta para agentes do Jenkins
    volumes:
      - jenkins_home:/var/jenkins_home  # Volume para persistir dados do Jenkins
    networks:
      - app-network
    environment:
      - JAVA_OPTS=-Djenkins.install.runSetupWizard=false  # Desativa o setup wizard inicial
    depends_on:
      - eureka-server
      - backend-service
      - client-service

volumes:
  jenkins_home:

networks:
  app-network:
    driver: bridge
```

#### Explicação:
- **`image: jenkins/jenkins:lts`**: Usamos a imagem oficial do Jenkins LTS (Long Term Support).
- **`ports`**: Mapeamos a porta `8080` para acessar a interface do Jenkins e a porta `50000` para comunicação com agentes.
- **`volumes`**: Criamos um volume chamado `jenkins_home` para persistir os dados do Jenkins (configurações, jobs, etc.).
- **`environment`**: Desativamos o setup wizard inicial para simplificar a configuração.

---

### 2. **Subir o Jenkins**

Execute o seguinte comando para subir o Jenkins junto com os outros serviços:

```bash
docker-compose up -d
```

Após subir o contêiner, acesse o Jenkins no navegador:

```
http://localhost:8080
```

---

### 3. **Configurar o Jenkins**

#### a) **Desbloquear o Jenkins**
Na primeira execução, o Jenkins solicitará uma senha de desbloqueio. Para obtê-la, execute:

```bash
docker logs jenkins
```

Procure no log uma mensagem como:

```
*************************************************************
*************************************************************
*************************************************************

Jenkins initial setup is required. An admin user has been created and a password generated.
Please use the following password to proceed to installation:

<PASSWORD>

This may also be found at: /var/jenkins_home/secrets/initialAdminPassword

*************************************************************
*************************************************************
*************************************************************
```

Insira a senha na interface do Jenkins.

#### b) **Instalar Plugins**
- Escolha a opção **"Install suggested plugins"** para instalar os plugins recomendados.
- Crie um usuário admin ou use a conta padrão.

#### c) **Configurar Credenciais**
- No Jenkins, vá para **"Manage Jenkins"** > **"Manage Credentials"**.
- Adicione credenciais para acessar seu repositório Git (se aplicável) e o Docker Hub (se for implantar imagens).

---

### 4. **Criar um Pipeline no Jenkins**

Um pipeline no Jenkins define as etapas de construção e implantação da sua aplicação. Aqui está um exemplo de pipeline para o seu projeto:

#### a) **Criar um Arquivo `Jenkinsfile`**
No repositório do seu projeto, crie um arquivo chamado `Jenkinsfile` com o seguinte conteúdo:

```groovy
pipeline {
    agent any

    environment {
        DOCKER_COMPOSE_FILE = 'docker-compose.yml'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/seu-usuario/seu-repositorio.git'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build Docker Images') {
            steps {
                sh 'docker-compose -f ${DOCKER_COMPOSE_FILE} build --no-cache'
            }
        }

        stage('Deploy') {
            steps {
                sh 'docker-compose -f ${DOCKER_COMPOSE_FILE} up -d'
            }
        }
    }

    post {
        success {
            echo 'Build and deployment completed successfully!'
        }
        failure {
            echo 'Build or deployment failed!'
        }
    }
}
```

#### Explicação:
- **`Checkout`**: Clona o repositório Git.
- **`Build`**: Compila o código com Maven.
- **`Build Docker Images`**: Constrói as imagens Docker.
- **`Deploy`**: Sobe os contêineres com Docker Compose.

#### b) **Configurar o Pipeline no Jenkins**
- No Jenkins, clique em **"New Item"**.
- Escolha **"Pipeline"** e dê um nome ao job.
- Na seção **"Pipeline"**, selecione **"Pipeline script from SCM"**.
- Escolha **"Git"** e insira a URL do seu repositório.
- Especifique o caminho para o `Jenkinsfile` (geralmente na raiz do repositório).
- Salve e execute o pipeline.

---

### 5. **Automatizar com Webhooks (Opcional)**

Para automatizar a execução do pipeline sempre que houver um push no repositório Git, configure um webhook:

1. No seu repositório Git (GitHub, GitLab, etc.), vá para **"Settings"** > **"Webhooks"**.
2. Adicione um webhook com a URL:
   ```
   http://<jenkins-ip>:8080/github-webhook/
   ```
3. No Jenkins, configure o pipeline para ser acionado pelo webhook.

---

### 6. **Monitoramento e Logs**

- Use o **Kibana** e o **Elasticsearch** para monitorar os logs do Jenkins e da aplicação.
- Configure o Jenkins para enviar logs para o Elasticsearch (usando plugins ou scripts).

---

### 7. **Resumo**

1. Adicione o Jenkins ao `docker-compose.yml`.
2. Suba o Jenkins com `docker-compose up -d`.
3. Configure o Jenkins e crie um pipeline usando um `Jenkinsfile`.
4. Automatize o pipeline com webhooks (opcional).
5. Use o Kibana e o Elasticsearch para monitoramento.
