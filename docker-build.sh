#!/usr/bin/env bash

# проверяем, установлен ли maven, если да, то собираем проект
if command -v mvn &> /dev/null
then
  mvn -DskipTests package
fi

docker build -t dreamworkerln/restreamer:latest -f infrastructure/docker/Dockerfile .

