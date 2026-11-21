FROM azul/zulu-openjdk:21 AS build
WORKDIR /src
# The Node binary Gradle downloads for the Kotlin/Wasm build needs libatomic.
RUN apt-get update && apt-get install -y --no-install-recommends libatomic1 && rm -rf /var/lib/apt/lists/*
COPY . .
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && \
    ./gradlew --no-daemon :app:webApp:wasmJsBrowserDistribution :server:installDist

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/server/build/install/server ./server
COPY --from=build /src/app/webApp/build/dist/wasmJs/productionExecutable ./web
ENV WEB_APP_DIR=/app/web
EXPOSE 8080
CMD ["./server/bin/server"]
