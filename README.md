# XML Product Importer

A Spring Boot application built with Kotlin that allows importing product data from XML files into a PostgreSQL database.

## Features

- 📤 Drag & drop XML file upload
- 🔍 Search products by name, brand, or description
- 🗄️ Automatic data storage in PostgreSQL
- 🐳 Docker containerized

## Quick Start

1. Make sure you have Docker installed
2. Create a `docker-compose.yml` file with the content below
3. Run: `docker-compose up -d`
4. Open: http://localhost:8080

## docker-compose.yml

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: productdb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres123
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/posttern/postgresql/data

  app:
    image: chadmany20/xml-importer-app:latest
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/productdb
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres123

volumes:
  postgres_data:
