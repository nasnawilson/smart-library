# ==========================================
# 智慧图书馆前端 Dockerfile（多阶段构建）
# ==========================================

# ---- 第一阶段：构建 Vue 静态文件 ----
FROM node:20-alpine AS builder

WORKDIR /build

# 先复制 package.json，利用 Docker 层缓存
COPY package*.json ./
RUN npm install --registry=https://registry.npmmirror.com

# 复制源码并构建
COPY . .
RUN npm run build

# ---- 第二阶段：使用 Nginx 部署 ----
FROM nginx:alpine

# 设置时区
ENV TZ=Asia/Shanghai
RUN apk add --no-cache tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

# 复制自定义 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 拷贝构建产物
COPY --from=builder /build/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
