# Jeevan Raksha Pharmacy — REST API

A production-grade Spring Boot application exposing a complete REST API for the Jeevan Raksha Pharmacy Management System.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.5 |
| ORM | Spring Data JPA / Hibernate |
| Database | MySQL 8.x |
| Build | Maven |
| API Docs | Springdoc OpenAPI 2.x (Swagger UI) |
| Validation | Jakarta Bean Validation |
| Logging | SLF4J + Logback |

---

## Quick Start

### Prerequisites

- Java 17
- Maven 3.8+
- MySQL 8.x running locally

### 1. Create the Database

Run the SQL script provided in `Usecase_02_Questions.pdf` against your MySQL instance:

```sql
CREATE DATABASE IF NOT EXISTS jeevan_raksha_pharmacy;
USE jeevan_raksha_pharmacy;
-- (paste and run the full schema + insert statements from the PDF)
```

### 2. Configure Database Credentials

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/jeevan_raksha_pharmacy?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password_here
```

### 3. Build and Run

```bash
# Build
mvn clean package -DskipTests

# Run
mvn spring-boot:run
# or
java -jar target/jeevan-raksha-pharmacy-1.0.0.jar
```

### 4. Access the API

| URL | Description |
|---|---|
| `http://localhost:8080/swagger-ui.html` | Interactive Swagger UI |
| `http://localhost:8080/v3/api-docs` | OpenAPI JSON spec |
| `http://localhost:8080/api/customers` | Sample endpoint |

---

## API Summary (38 Endpoints)

### Customers — `/api/customers`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/customers` | List all customers (filter by city, paginated) |
| GET | `/api/customers/{id}` | Customer profile with order summary |
| POST | `/api/customers` | Register a new customer |
| PUT | `/api/customers/{id}` | Update customer profile |
| DELETE | `/api/customers/{id}` | Delete customer (blocked if orders exist) |
| GET | `/api/customers/{id}/orders` | Full order history |
| GET | `/api/customers/top-spenders` | Customers ranked by total spend |

### Suppliers — `/api/suppliers`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/suppliers` | List all suppliers with medicine count |
| GET | `/api/suppliers/{id}` | Supplier profile with medicines and avg price |
| POST | `/api/suppliers` | Register a new supplier |
| PUT | `/api/suppliers/{id}` | Update supplier details |
| DELETE | `/api/suppliers/{id}` | Delete supplier (blocked if medicines linked) |
| GET | `/api/suppliers/{id}/medicines` | All medicines by this supplier |

### Medicines — `/api/medicines`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/medicines` | List all medicines (filter by category, paginated) |
| GET | `/api/medicines/{id}` | Full details of one medicine |
| POST | `/api/medicines` | Add a new medicine |
| PUT | `/api/medicines/{id}` | Update medicine details |
| DELETE | `/api/medicines/{id}` | Remove medicine (blocked if in orders) |
| GET | `/api/medicines/by-category/{cat}` | All medicines in a category |
| GET | `/api/medicines/by-supplier/{id}` | All medicines from a supplier |
| GET | `/api/medicines/price-range?min=&max=` | Medicines within a price range |

### Orders — `/api/orders`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/orders` | Place a new order (validates stock, deducts qty) |
| GET | `/api/orders/{id}` | Full order with line items |
| GET | `/api/orders` | All orders (date filter, paginated) |
| GET | `/api/orders/by-date-range?from=&to=` | Orders in a date window |
| DELETE | `/api/orders/{id}` | Cancel order (restores stock) |
| GET | `/api/orders/by-payment-mode/{mode}` | Orders by payment mode |
| GET | `/api/orders/{id}/invoice` | Invoice-ready JSON |

### Inventory — `/api/inventory`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/inventory` | Full stock overview |
| GET | `/api/inventory/low-stock?threshold=50` | Medicines below threshold |
| PATCH | `/api/inventory/{id}/restock` | Add stock (delta quantity) |

### Reports — `/api/reports`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/reports/revenue?from=&to=` | Total revenue in date range |
| GET | `/api/reports/revenue-by-payment-mode` | Revenue by UPI / Cash / Card |
| GET | `/api/reports/bestsellers` | Top medicines by quantity sold |
| GET | `/api/reports/customer-with-most-orders` | VIP customer |
| GET | `/api/reports/expired-medicines` | Expired stock (compliance) |
| GET | `/api/reports/inventory-audit` | Low-stock + supplier contact |

### Search — `/api/search`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/search?q=dolo&type=all` | Cross-entity search (medicines + suppliers) |

---

## Standard Response Envelope

Every endpoint wraps its payload in a consistent JSON envelope:

**Success:**
```json
{
  "status": "success",
  "message": "Customer retrieved successfully",
  "data": { ... }
}
```

**Error:**
```json
{
  "status": "error",
  "message": "Customer with id '99' not found",
  "data": null
}
```

**Validation Error (400):**
```json
{
  "status": "error",
  "message": "Validation failed. Please check the request fields.",
  "data": {
    "name": "Customer name is required",
    "phone": "Phone must be 10–15 digits"
  }
}
```

---

## HTTP Status Codes

| Status | Meaning |
|---|---|
| 200 OK | Success (GET, PUT, DELETE, PATCH) |
| 201 Created | Resource created (POST) |
| 400 Bad Request | Validation or request format error |
| 404 Not Found | Entity does not exist |
| 409 Conflict | Delete blocked by referential integrity |
| 422 Unprocessable Entity | Insufficient stock for an order |
| 500 Internal Server Error | Unexpected server-side error |

---

## Project Structure

```
src/main/java/com/codegnan/jeevanraksha/
├── JeevanRakshaApplication.java   ← Entry point
├── config/
│   └── SwaggerConfig.java         ← OpenAPI / Swagger config
├── controller/                    ← REST layer (7 controllers)
│   ├── CustomerController.java
│   ├── SupplierController.java
│   ├── MedicineController.java
│   ├── OrderController.java
│   ├── InventoryController.java
│   ├── ReportController.java
│   └── SearchController.java
├── service/                       ← Business logic (7 services)
│   ├── CustomerService.java
│   ├── SupplierService.java
│   ├── MedicineService.java
│   ├── OrderService.java
│   ├── InventoryService.java
│   ├── ReportService.java
│   └── SearchService.java
├── repository/                    ← Data access (5 repositories)
│   ├── CustomerRepository.java
│   ├── SupplierRepository.java
│   ├── MedicineRepository.java
│   ├── OrderRepository.java
│   └── OrderItemRepository.java
├── entity/                        ← JPA entities (5 entities)
│   ├── Customer.java
│   ├── Supplier.java
│   ├── Medicine.java
│   ├── Order.java
│   └── OrderItem.java
├── dto/
│   ├── request/                   ← Inbound payloads (6 classes)
│   └── response/                  ← Outbound payloads (13 classes)
├── enums/
│   └── PaymentMode.java
└── exception/                     ← Custom exceptions + handler
    ├── ResourceNotFoundException.java
    ├── ResourceConstraintException.java
    ├── InsufficientStockException.java
    ├── InvalidRequestException.java
    └── GlobalExceptionHandler.java
```

---

## Key Design Decisions

- **Controller → Service → Repository → DB** — strict layered architecture; no cross-layer bypasses.
- **No Mappers** — entity-to-DTO conversion is done inline in service methods using builder patterns.
- **No Seed Data** — the application assumes the database has been set up separately using the provided SQL script.
- **@Transactional** — write operations (place order, cancel order, restock) are fully transactional; if any step fails, the entire operation rolls back.
- **Stock deduction is atomic** — when placing an order, all stock is validated first before any deduction occurs.
- **Referential integrity** — delete operations on customers, suppliers, and medicines are blocked with a 409 response if dependent records exist.
- **Global Exception Handler** — `@RestControllerAdvice` intercepts all exceptions and returns uniform error envelopes.

---

## Logging

Logs are written to both the console and `logs/jeevan-raksha-pharmacy.log`.

Log levels by package:
- `com.codegnan.jeevanraksha` → DEBUG (detailed service/controller logs)
- `org.hibernate.SQL` → DEBUG (SQL query logging)
- Root → INFO
