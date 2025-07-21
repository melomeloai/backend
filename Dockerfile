# 使用多阶段构建来优化镜像大小
FROM maven:3.9.4-eclipse-temurin-17 AS build

# 设置工作目录
WORKDIR /app

# 复制pom.xml和源代码
COPY pom.xml .
COPY src ./src

# 构建应用程序
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jdk-jammy

# 设置工作目录
WORKDIR /app

# 从构建阶段复制jar文件
COPY --from=build /app/target/*.jar app.jar

# 创建非root用户以提高安全性
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# 暴露端口（Render会自动分配PORT环境变量）
EXPOSE 8080

# 启动应用程序
# 使用SERVER_PORT环境变量，如果没有设置则默认使用8080
CMD ["java", "-jar", "-Dserver.port=${PORT:-8080}", "app.jar"]
