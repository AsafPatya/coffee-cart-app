FROM eclipse-temurin:21-jdk AS build
RUN apt-get update && apt-get install -y libatomic1 && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY . .
RUN ./gradlew :composeApp:wasmJsBrowserProductionWebpack --no-daemon

FROM nginx:alpine
COPY --from=build /app/composeApp/build/processedResources/wasmJs/main/index.html /usr/share/nginx/html/
COPY --from=build /app/composeApp/build/kotlin-webpack/wasmJs/productionExecutable/ /usr/share/nginx/html/
COPY web-nginx.conf /etc/nginx/conf.d/default.conf
