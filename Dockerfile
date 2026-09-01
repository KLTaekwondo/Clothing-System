# 根目录 Dockerfile：一个文件构建两个服务（backend / frontend）
# 用法见 docker-compose.yml（build.target 指定构建哪个）
# 前提：本地已构建好产物（backend jar + frontend dist），不在容器内重新构建

# ================= 后端 =================
FROM eclipse-temurin:17-jre AS backend
WORKDIR /app

# 拷贝本地构建好的 jar（明确文件名，避免 target 下多个 jar 导致 COPY 失败）
COPY backend/target/backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# 使用 prod profile 启动（数据库/Redis 连接通过环境变量注入，见 docker-compose.yml）
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]

# ================= 前端 =================
FROM nginx:1.27-alpine AS frontend

# 拷贝本地构建产物到 nginx 静态目录
COPY frontend/dist/ /usr/share/nginx/html/

# 自定义 nginx 配置：前端页面 + /api 反向代理
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
