# 🐳 Docker Compose - Infrastructure Services

## 📋 Tổng quan

Docker Compose setup cho infrastructure services: PostgreSQL, Redis, Kafka, Zookeeper.

**Backend application chạy riêng** (local hoặc separate container).

---

## 🚀 Quick Start

### Chạy Infrastructure Services

```bash
# Start tất cả services
docker-compose --profile infra up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f [service-name]
```

### Stop Services

```bash
# Stop services (giữ data)
docker-compose down

# Stop và xóa volumes (⚠️ mất data)
docker-compose down -v
```

---

## 📁 Files Structure

| File | Mục đích |
|------|----------|
| `novacore/docker-compose.yml` | Infrastructure services (PostgreSQL, Redis, Kafka, Zookeeper) |
| `novacore/docker-compose.prod.yml` | Production overrides (bỏ expose ports, tăng limits) |
| `novacore/env.example` | Environment variables template |
| `infra/docker/databases/redis.conf` | Redis configuration file |
| `infra/docker/databases/postgres-init.sh` | PostgreSQL init script |

---

## ⚙️ Configuration

### Environment Variables

Tạo file `.env` từ template:

```bash
# Copy template (nếu có)
# Hoặc tạo .env với các biến sau:

POSTGRES_DB=novacore
POSTGRES_USER=novacore
POSTGRES_PASSWORD=changeme
POSTGRES_PORT=5432

REDIS_PASSWORD=changeme
REDIS_PORT=6379

KAFKA_PORT=9092
ZOOKEEPER_PORT=2181
```

### Profiles

Services sử dụng profile `infra`:

```bash
# Chỉ start services với profile infra
docker-compose --profile infra up -d

# Start tất cả (nếu có services khác không dùng profile)
docker-compose up -d
```

---

## 🔧 Service Details

### PostgreSQL

- **Port**: 5432 (exposed cho local dev)
- **Data**: Persistent volume `novacore-postgres-data`
- **Init Script**: Chạy lần đầu khi volume trống
- **Resource Limits**: 2GB RAM, 2 CPUs

**Connection từ Backend Local:**
```
jdbc:postgresql://localhost:5432/novacore
```

### Redis

- **Port**: 6379 (exposed cho local dev)
- **Data**: Persistent volume `novacore-redis-data`
- **Config**: Sử dụng `redis.conf` file
- **Resource Limits**: 512MB RAM, 1 CPU

**Connection từ Backend Local:**
```
host: localhost
port: 6379
password: (từ REDIS_PASSWORD env)
```

### Kafka

- **Port**: 9092 (exposed cho local dev)
- **Data**: Persistent volume `novacore-kafka-data`
- **Internal Port**: 29092 (cho inter-container communication)
- **Partitions**: 3 (dev-friendly)
- **Resource Limits**: 2GB RAM, 2 CPUs

**Connection từ Backend Local:**
```
bootstrap-servers: localhost:9092
```

### Zookeeper

- **Port**: 2181 (exposed cho local dev)
- **Data**: Persistent volumes cho data và logs
- **Resource Limits**: 512MB RAM, 1 CPU

---

## 🔒 Production Deployment

### Sử dụng Production Overrides

```bash
# Start với production settings
docker-compose -f docker-compose.yml -f docker-compose.prod.yml --profile infra up -d
```

**Production settings:**
- ❌ Không expose ports ra ngoài (chỉ internal network)
- ⬆️ Tăng resource limits
- 🔒 Stricter security (disable auto-create topics)

### Security Checklist

- [ ] Change tất cả passwords trong `.env`
- [ ] Review resource limits
- [ ] Setup backup strategy cho volumes
- [ ] Configure network security (firewall)
- [ ] Enable SSL/TLS nếu expose services
- [ ] Setup monitoring và alerting

---

## 🐛 Troubleshooting

### Services không start

```bash
# Check logs
docker-compose logs [service-name]

# Check resource usage
docker stats

# Restart service
docker-compose restart [service-name]
```

### Redis không connect được

```bash
# Check Redis password
docker-compose exec redis redis-cli -a changeme ping

# Check config
docker-compose exec redis cat /usr/local/etc/redis/redis.conf
```

### Kafka không connect được

```bash
# Check Kafka health
docker-compose exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092

# List topics
docker-compose exec kafka kafka-topics --list --bootstrap-server localhost:9092
```

### PostgreSQL init script không chạy

**Lưu ý**: Init script chỉ chạy lần đầu khi volume trống. Nếu volume đã có data, script sẽ KHÔNG chạy lại.

```bash
# Xóa volume và restart (⚠️ mất data)
docker-compose down -v
docker-compose up -d postgres
```

---

## 📊 Resource Limits

### Development (default)

| Service | Memory | CPUs |
|---------|--------|------|
| PostgreSQL | 2GB | 2.0 |
| Redis | 512MB | 1.0 |
| Zookeeper | 512MB | 1.0 |
| Kafka | 2GB | 2.0 |

### Production (override)

| Service | Memory | CPUs |
|---------|--------|------|
| PostgreSQL | 4GB | 4.0 |
| Redis | 1GB | 2.0 |
| Zookeeper | 512MB | 1.0 |
| Kafka | 4GB | 4.0 |

---

## 🔄 Common Commands

```bash
# Start services
docker-compose --profile infra up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f [service]

# Restart service
docker-compose restart [service]

# Execute command trong container
docker-compose exec [service] [command]

# Check resource usage
docker stats

# Backup volumes
docker run --rm -v novacore-postgres-data:/data -v $(pwd)/backups:/backup alpine tar czf /backup/postgres-$(date +%Y%m%d).tar.gz /data
```

---

## 📚 Additional Resources

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [PostgreSQL Docker Image](https://hub.docker.com/_/postgres)
- [Redis Docker Image](https://hub.docker.com/_/redis)
- [Confluent Kafka Docker Image](https://hub.docker.com/r/confluentinc/cp-kafka)

---

**Last Updated**: 2024-12-31



