# 🐳 Docker Setup - Infrastructure Services

Docker Compose setup cho các infrastructure services: **PostgreSQL**, **Redis**, **Kafka**, và **Zookeeper**.

## 📋 Yêu cầu

- Docker Desktop (Windows/Mac) hoặc Docker Engine + Docker Compose (Linux)
- Tối thiểu 4GB RAM available
- Ports cần mở: 5432, 6379, 9092, 2181

## 🚀 Quick Start

### 1. Copy environment file

```bash
# Copy template và chỉnh sửa nếu cần
cp novacore/env.example novacore/.env
```

### 2. Start tất cả services

```bash
# Từ thư mục gốc của project
cd novacore
docker-compose --profile infra up -d

# Hoặc từ thư mục gốc
docker-compose -f novacore/docker-compose.yml --profile infra up -d
```

### 3. Kiểm tra status

```bash
docker-compose -f novacore/docker-compose.yml ps
```

### 4. Xem logs

```bash
# Tất cả services
docker-compose -f novacore/docker-compose.yml logs -f

# Service cụ thể
docker-compose -f novacore/docker-compose.yml logs -f postgres
docker-compose -f novacore/docker-compose.yml logs -f redis
docker-compose -f novacore/docker-compose.yml logs -f kafka
```

## 🛑 Stop Services

```bash
# Stop services (giữ data)
docker-compose -f novacore/docker-compose.yml down

# Stop và xóa volumes (⚠️ mất data)
docker-compose -f novacore/docker-compose.yml down -v
```

## 📊 Services Overview

### PostgreSQL

- **Port**: 5432
- **Database**: novacore
- **Username**: novacore
- **Password**: changeme (đổi trong `.env`)
- **Data Volume**: `novacore-postgres-data`
- **Connection String**: `jdbc:postgresql://localhost:5432/novacore`

**Test connection:**
```bash
docker-compose -f novacore/docker-compose.yml exec postgres psql -U novacore -d novacore
```

### Redis

- **Port**: 6379
- **Password**: changeme (đổi trong `.env`)
- **Data Volume**: `novacore-redis-data`
- **Config**: `infra/docker/databases/redis.conf`

**Test connection:**
```bash
docker-compose -f novacore/docker-compose.yml exec redis redis-cli -a changeme ping
```

### Kafka

- **Port**: 9092
- **Internal Port**: 29092 (cho inter-container communication)
- **Data Volume**: `novacore-kafka-data`
- **Bootstrap Servers**: `localhost:9092`

**Test connection:**
```bash
# List topics
docker-compose -f novacore/docker-compose.yml exec kafka kafka-topics --list --bootstrap-server localhost:9092

# Create topic
docker-compose -f novacore/docker-compose.yml exec kafka kafka-topics --create --topic test-topic --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

### Zookeeper

- **Port**: 2181
- **Data Volumes**: `novacore-zookeeper-data`, `novacore-zookeeper-logs`
- **Required by**: Kafka

## 🔧 Configuration

### Environment Variables

Tạo file `novacore/.env` từ template `novacore/env.example`:

```bash
POSTGRES_DB=novacore
POSTGRES_USER=novacore
POSTGRES_PASSWORD=changeme
POSTGRES_PORT=5432

REDIS_PASSWORD=changeme
REDIS_PORT=6379

KAFKA_PORT=9092
ZOOKEEPER_PORT=2181
```

### Resource Limits

**Development (default):**
- PostgreSQL: 2GB RAM, 2 CPUs
- Redis: 512MB RAM, 1 CPU
- Zookeeper: 512MB RAM, 1 CPU
- Kafka: 2GB RAM, 2 CPUs

**Production (override):**
- PostgreSQL: 4GB RAM, 4 CPUs
- Redis: 1GB RAM, 2 CPUs
- Zookeeper: 512MB RAM, 1 CPU
- Kafka: 4GB RAM, 4 CPUs

## 🏭 Production Deployment

### Sử dụng Production Overrides

```bash
docker-compose -f novacore/docker-compose.yml -f novacore/docker-compose.prod.yml --profile infra up -d
```

**Production settings:**
- ❌ Không expose ports ra ngoài (chỉ internal network)
- ⬆️ Tăng resource limits
- 🔒 Disable auto-create topics trong Kafka
- 📈 Tăng số partitions và retention time

### Security Checklist

- [ ] Đổi tất cả passwords trong `.env`
- [ ] Review resource limits
- [ ] Setup backup strategy cho volumes
- [ ] Configure network security (firewall)
- [ ] Enable SSL/TLS nếu expose services
- [ ] Setup monitoring và alerting

## 🐛 Troubleshooting

### Services không start

```bash
# Check logs
docker-compose -f novacore/docker-compose.yml logs [service-name]

# Check resource usage
docker stats

# Restart service
docker-compose -f novacore/docker-compose.yml restart [service-name]
```

### Port conflicts

Nếu ports đã được sử dụng, đổi trong `novacore/.env`:

```bash
POSTGRES_PORT=5433
REDIS_PORT=6380
KAFKA_PORT=9093
ZOOKEEPER_PORT=2182
```

### PostgreSQL init script không chạy

Init script chỉ chạy lần đầu khi volume trống:

```bash
# Xóa volume và restart (⚠️ mất data)
docker-compose -f novacore/docker-compose.yml down -v
docker-compose -f novacore/docker-compose.yml --profile infra up -d postgres
```

### Kafka không connect được

```bash
# Check Kafka health
docker-compose -f novacore/docker-compose.yml exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092

# Check Zookeeper connection
docker-compose -f novacore/docker-compose.yml exec kafka nc -z zookeeper 2181
```

## 📦 Volumes

Data được lưu trong Docker volumes:

- `novacore-postgres-data`: PostgreSQL data
- `novacore-redis-data`: Redis data
- `novacore-zookeeper-data`: Zookeeper data
- `novacore-zookeeper-logs`: Zookeeper logs
- `novacore-kafka-data`: Kafka data

**Backup volumes:**
```bash
# Backup PostgreSQL
docker run --rm -v novacore-postgres-data:/data -v $(pwd)/backups:/backup alpine tar czf /backup/postgres-$(date +%Y%m%d).tar.gz /data

# Backup Redis
docker run --rm -v novacore-redis-data:/data -v $(pwd)/backups:/backup alpine tar czf /backup/redis-$(date +%Y%m%d).tar.gz /data
```

## 🔄 Common Commands

```bash
# Start services
docker-compose -f novacore/docker-compose.yml --profile infra up -d

# Stop services
docker-compose -f novacore/docker-compose.yml down

# View logs
docker-compose -f novacore/docker-compose.yml logs -f [service]

# Restart service
docker-compose -f novacore/docker-compose.yml restart [service]

# Execute command trong container
docker-compose -f novacore/docker-compose.yml exec [service] [command]

# Check resource usage
docker stats

# Remove all (including volumes)
docker-compose -f novacore/docker-compose.yml down -v
```

## 📚 Additional Resources

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [PostgreSQL Docker Image](https://hub.docker.com/_/postgres)
- [Redis Docker Image](https://hub.docker.com/_/redis)
- [Confluent Kafka Docker Image](https://hub.docker.com/r/confluentinc/cp-kafka)

---

**Last Updated**: 2025-01-03




