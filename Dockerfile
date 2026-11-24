# ===== Etapa 1: compila o projeto com Maven =====
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Baixa as dependências primeiro (aproveita o cache do Docker)
COPY pom.xml .
RUN mvn -q dependency:go-offline

# Compila e gera o .jar (os testes precisam do banco, por isso são pulados aqui)
COPY src ./src
RUN mvn -q -DskipTests package

# ===== Etapa 2: imagem final, só com o Java para rodar =====
FROM eclipse-temurin:21-jre
WORKDIR /app

# Horário de Brasília nas datas dos chamados
ENV TZ=America/Sao_Paulo

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# MaxRAMPercentage evita estourar a memória no plano gratuito do Render
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-Duser.timezone=America/Sao_Paulo", "-jar", "app.jar"]
