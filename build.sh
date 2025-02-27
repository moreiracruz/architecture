#!/bin/bash
docker-compose up -d mysql-master mysql-slave rabbitmq eureka-server
sleep 10
mvn clean install
#docker-compose down
