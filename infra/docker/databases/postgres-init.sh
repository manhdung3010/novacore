#!/bin/bash
set -e

echo "=========================================="
echo "PostgreSQL Initialization Script"
echo "=========================================="

# Create database if it doesn't exist
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Enable extensions if needed
    -- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
    -- CREATE EXTENSION IF NOT EXISTS "pgcrypto";
    
    -- Grant privileges
    GRANT ALL PRIVILEGES ON DATABASE $POSTGRES_DB TO $POSTGRES_USER;
    
    -- Create schemas if needed
    -- CREATE SCHEMA IF NOT EXISTS app;
    -- GRANT ALL ON SCHEMA app TO $POSTGRES_USER;
    
    echo "Database initialization completed successfully!"
EOSQL

echo "=========================================="
echo "PostgreSQL is ready!"
echo "=========================================="




