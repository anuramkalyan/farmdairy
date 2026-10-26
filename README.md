# FarmDairy API

Backend REST API for **FarmDairy**, a dairy ordering and milk-subscription platform. Customers can browse the product catalogue and create accounts; administrators manage products. Built with **Java 21, Spring Boot, Spring Security and PostgreSQL**.

> **Status:** in active development. Product catalogue and authentication are complete. Cart, orders, milk subscriptions (with pause/resume) and payment-gateway sandbox integration are planned next.

---

## Contents

- [Tech stack](#tech-stack)
- [Features](#features)
- [Project structure](#project-structure)
- [Getting started](#getting-started)
- [API documentation](#api-documentation)
  - [Authentication](#authentication)
  - [Products](#products)
  - [Error responses](#error-responses)
- [Roadmap](#roadmap)

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4 (Spring Web MVC) |
| Security | Spring Security, OAuth2 Resource Server (JWT, HS256), BCrypt |
| Persistence | Spring Data JPA / Hibernate |
| Database | PostgreSQL |
| Validation | Jakarta Bean Validation |
| Build | Maven (wrapper included) |

## Features

- **Product catalogue** with create, read, update and delete operations
- **Pagination and filtering** by category and availability
- **JWT authentication**: stateless login that returns a signed access token (valid for 1 hour)
- **Role-based access control** with two roles, `CUSTOMER` and `ADMIN`
  - Anyone can browse products
  - Only admins can create, update or delete products
- **Customer self-registration** with BCrypt-hashed passwords
- **Admin account bootstrap** from environment variables on startup
- **Request validation** with clear, field-level error messages
- **Consistent error responses** in the standard [RFC 7807 Problem Details](https://www.rfc-editor.org/rfc/rfc7807) format
- **No secrets in code**: database password and JWT secret are read from environment variables

## Project structure

```
src/main/java/com/anuramkalyan/farmdairy
├── config/        SecurityConfig (JWT, roles, endpoint rules), AdminAccountInitializer
├── controller/    AuthController, ProductController (REST endpoints)
├── service/       AuthService, ProductService (business logic)
├── repository/    Spring Data JPA repositories
├── entity/        AppUser, Product, Role (database tables)
├── dto/           Request and response records
└── exception/     GlobalExceptionHandler (Problem Details error responses)
```

The code follows a **controller → service → repository** layering. Controllers handle HTTP and validation, services hold business logic, and repositories talk to the database. Entities are never returned directly; responses always go through DTOs.

---

## Getting started

### Prerequisites

- Java 21 or newer
- PostgreSQL 14 or newer

### 1. Create the database

```sql
CREATE USER farmdairy_user WITH PASSWORD 'your_db_password';
CREATE DATABASE farmdairy_db OWNER farmdairy_user;
```

Tables are created automatically on first run (`spring.jpa.hibernate.ddl-auto=update`).

### 2. Set environment variables

| Variable | Required | Description |
|---|---|---|
| `FARMDAIRY_DB_PASSWORD` | Yes | Password for `farmdairy_user` |
| `FARMDAIRY_JWT_SECRET` | Yes | Base64-encoded secret of at least 32 bytes, used to sign tokens |
| `ADMIN_EMAIL` | Optional | Creates an admin account with this email on startup if it doesn't exist |
| `ADMIN_PASSWORD` | Optional | Password for that admin account (set both or neither) |

Generate a JWT secret:

```bash
openssl rand -base64 32
```

### 3. Run the application

```bash
export FARMDAIRY_DB_PASSWORD=your_db_password
export FARMDAIRY_JWT_SECRET=$(openssl rand -base64 32)
export ADMIN_EMAIL=admin@example.com
export ADMIN_PASSWORD=change_me_please

./mvnw spring-boot:run
```

The API starts on **http://localhost:8080**. In IntelliJ IDEA, add the same variables under *Run → Edit Configurations → Environment variables* on a single run configuration.

---

## API documentation

**Base URL:** `http://localhost:8080/api/v1`

All request and response bodies are JSON. Protected endpoints need the access token from [Login](#login) in an `Authorization` header:

```
Authorization: Bearer <accessToken>
```

### Endpoint summary

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/auth/register` | Public | Register a customer account |
| `POST` | `/auth/login` | Public | Log in and receive a JWT |
| `GET` | `/products` | Public | List products (paginated, filterable) |
| `GET` | `/products/{id}` | Public | Get one product |
| `POST` | `/products` | Admin | Create a product |
| `PUT` | `/products/{id}` | Admin | Update a product |
| `DELETE` | `/products/{id}` | Admin | Delete a product |

---

### Authentication

#### Register

`POST /api/v1/auth/register`

Creates a new account with the `CUSTOMER` role. The email is trimmed and stored in lowercase.

**Request body**

| Field | Type | Rules |
|---|---|---|
| `name` | string | Required, max 100 characters |
| `email` | string | Required, valid email, max 254 characters |
| `password` | string | Required, 8–72 characters |

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"user@test.com","password":"password123"}'
```

**Response — `201 Created`**

Returns the registered email as plain text:

```
user@test.com
```

| Status | When |
|---|---|
| `400 Bad Request` | A field fails validation |
| `409 Conflict` | An account with this email already exists |

#### Login

`POST /api/v1/auth/login`

**Request body**

| Field | Type | Rules |
|---|---|---|
| `email` | string | Required, valid email |
| `password` | string | Required |

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@test.com","password":"password123"}'
```

**Response — `200 OK`**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

`expiresIn` is in seconds. The token's `roles` claim holds the user's role (`ROLE_CUSTOMER` or `ROLE_ADMIN`), and `sub` holds their email.

| Status | When |
|---|---|
| `400 Bad Request` | A field fails validation |
| `401 Unauthorized` | Wrong email or password |

Tip: to save the token straight into a shell variable:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"change_me_please"}' \
  | sed 's/.*"accessToken":"\([^"]*\)".*/\1/')
```

---

### Products

#### Product object

| Field | Type | Description |
|---|---|---|
| `id` | number | Unique product ID |
| `name` | string | Product name |
| `description` | string \| null | Optional description |
| `category` | string | Product category, e.g. `MILK` |
| `price` | number | Price, two decimal places |
| `unit` | string | Selling unit, e.g. `1 litre` |
| `available` | boolean | Whether the product is in stock |
| `createdAt` | string | Creation time (ISO 8601) |

#### List products

`GET /api/v1/products`

**Query parameters** (all optional)

| Parameter | Type | Default | Description |
|---|---|---|---|
| `category` | string | — | Filter by category. Matched in uppercase, so `milk` finds `MILK` |
| `available` | boolean | — | `true` for in-stock only, `false` for out-of-stock only |
| `page` | integer | `0` | Page number, starting at 0 |
| `size` | integer | `10` | Items per page, 1–100 |

Results are sorted by `id`, ascending.

```bash
curl "http://localhost:8080/api/v1/products?category=MILK&available=true&page=0&size=10"
```

**Response — `200 OK`**

```json
{
  "content": [
    {
      "id": 2,
      "name": "Farm Fresh Milk",
      "description": "Fresh dairy milk",
      "category": "MILK",
      "price": 70.00,
      "unit": "1 litre",
      "available": true,
      "createdAt": "2026-10-25T16:53:26.583782"
    }
  ],
  "number": 0,
  "size": 10,
  "numberOfElements": 1,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true,
  "empty": false
}
```

The response also includes Spring's `pageable` and `sort` metadata (omitted above for brevity).

| Status | When |
|---|---|
| `400 Bad Request` | `page` is negative, or `size` is outside 1–100 |

#### Get a product

`GET /api/v1/products/{id}`

```bash
curl http://localhost:8080/api/v1/products/2
```

**Response — `200 OK`**: a single [product object](#product-object).

| Status | When |
|---|---|
| `404 Not Found` | No product with this ID |

#### Create a product

`POST /api/v1/products` · **Admin only**

**Request body**

| Field | Type | Rules |
|---|---|---|
| `name` | string | Required, max 120 characters |
| `description` | string | Optional, max 1000 characters |
| `category` | string | Required, max 50 characters |
| `price` | number | Required, at least `0.01` |
| `unit` | string | Required, max 30 characters |
| `available` | boolean | Optional, defaults to `true` |

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Fresh Milk","description":"Full cream","category":"MILK","price":60.00,"unit":"1 litre"}'
```

**Response — `201 Created`**: the created [product object](#product-object).

| Status | When |
|---|---|
| `400 Bad Request` | A field fails validation |
| `401 Unauthorized` | Missing, invalid or expired token |
| `403 Forbidden` | Logged in, but not an admin |

> **Note:** store categories in uppercase (e.g. `MILK`, `OILS`). The list filter converts the search term to uppercase, so a product saved as `Milk` won't match `?category=milk`.

#### Update a product

`PUT /api/v1/products/{id}` · **Admin only**

Replaces the product's details. Takes the same body as [Create a product](#create-a-product). If `available` is left out, the current availability is kept.

```bash
curl -X PUT http://localhost:8080/api/v1/products/2 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Farm Fresh Milk","description":"Fresh dairy milk","category":"MILK","price":72.00,"unit":"1 litre","available":true}'
```

**Response — `200 OK`**: the updated [product object](#product-object).

| Status | When |
|---|---|
| `400 Bad Request` | A field fails validation |
| `401 Unauthorized` | Missing, invalid or expired token |
| `403 Forbidden` | Logged in, but not an admin |
| `404 Not Found` | No product with this ID |

#### Delete a product

`DELETE /api/v1/products/{id}` · **Admin only**

```bash
curl -X DELETE http://localhost:8080/api/v1/products/2 \
  -H "Authorization: Bearer $TOKEN"
```

**Response — `204 No Content`** (empty body)

| Status | When |
|---|---|
| `401 Unauthorized` | Missing, invalid or expired token |
| `403 Forbidden` | Logged in, but not an admin |
| `404 Not Found` | No product with this ID |

---

### Error responses

Application errors use the **Problem Details** format (`Content-Type: application/problem+json`).

**Validation error — `400`**

```json
{
  "title": "Validation failed",
  "status": 400,
  "detail": "price: must be greater than or equal to 0.01; name: must not be blank",
  "instance": "/api/v1/products"
}
```

**Not found — `404`**

```json
{
  "title": "Request failed",
  "status": 404,
  "detail": "Product not found with id: 99",
  "instance": "/api/v1/products/99"
}
```

**Duplicate email — `409`**

```json
{
  "title": "Request failed",
  "status": 409,
  "detail": "An account with this email already exists",
  "instance": "/api/v1/auth/register"
}
```

**Authentication and authorisation (`401` / `403`)** come from Spring Security. They have an empty body; the reason is in the `WWW-Authenticate` header, for example:

```
HTTP/1.1 401
WWW-Authenticate: Bearer error="invalid_token", error_description="An error occurred while attempting to decode the Jwt: Malformed token"
```

### Status code reference

| Code | Meaning |
|---|---|
| `200 OK` | Request succeeded |
| `201 Created` | Resource created |
| `204 No Content` | Resource deleted |
| `400 Bad Request` | Invalid body or query parameters |
| `401 Unauthorized` | Not logged in, or the token is invalid or expired |
| `403 Forbidden` | Logged in, but the role isn't allowed |
| `404 Not Found` | Resource doesn't exist |
| `409 Conflict` | Duplicate resource (e.g. email already registered) |

---

## Roadmap

- [x] Product catalogue with pagination and filtering
- [x] JWT authentication and role-based access control
- [ ] Customer profiles and delivery addresses
- [ ] Shopping cart
- [ ] Order placement and order history
- [ ] Milk subscriptions with scheduling and pause/resume
- [ ] Payment-gateway sandbox integration
- [ ] Unit and integration tests (JUnit, MockMvc)
- [ ] Docker setup for local development

## Author

**Anuram Kalyan** — [GitHub](https://github.com/anuramkalyan)
