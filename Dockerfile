FROM gcr.io/distroless/java21-debian12
LABEL maintainer=westelh
WORKDIR /app
COPY build/libs/vault-oauth-client-all.jar /app/vault-oauth-client-all.jar
ENTRYPOINT ["java", "jar", "/app/vault-oauth-client-all.jar"]
EXPOSE 8080
