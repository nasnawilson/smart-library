# ==========================================
# 智慧图书馆后端 Dockerfile（多阶段构建）
# ==========================================

# ---- 第一阶段：使用 Maven 构建 jar 包 ----
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /build

# 先复制 pom.xml，利用 Docker 层缓存加速依赖下载
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并构建
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---- 第二阶段：运行 jar 包 ----
FROM eclipse-temurin:17-jre

WORKDIR /app

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 从构建阶段拷贝 jar
COPY --from=builder /build/target/smart-library-backend-1.0.0.jar app.jar

EXPOSE 8080

# JVM 参数优化
ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
