# StockFlow – Inventory and Sales Transaction Management System

StockFlow adalah REST API backend yang dirancang untuk mengelola inventory dan transaksi penjualan dalam sebuah sistem warehouse. Sistem ini menyediakan fitur untuk manajemen produk, stok, sales order, pembayaran, serta pengiriman barang secara terintegrasi.

Project ini dibuat sebagai **Final Project Backend Development Bootcamp**.

---

## Features

### Authentication & Authorization
- Registrasi dan login user
- JWT-based authentication
- Role-based access control (ADMIN / USER)
- Endpoint protection menggunakan Spring Security

### Master Data Management
- Product management
- Category management
- Warehouse management
- Customer management

### Inventory Management
- Menyimpan stok produk per warehouse
- Mekanisme **stock reservation**
- Tracking histori perubahan stok melalui **stock movements**

### Sales Order Management
- Membuat sales order
- Validasi ketersediaan stok
- Reserve stok sebelum pembayaran

### Payment Processing
- Pencatatan transaksi pembayaran
- Update status order setelah pembayaran berhasil
- Release reserved stock jika order dibatalkan

### Shipment Processing
- Membuat shipment setelah pembayaran berhasil
- Tracking status pengiriman

### Reporting
- Top selling products
- Low stock products

---

## Business Flow

```text
User Login
   ↓
Create Sales Order
   ↓
Check Product Stock
   ↓
Stock Available?
   ↓
Reserve Stock
   ↓
Create Payment Transaction
   ↓
Payment Success?
   ↓
Deduct Stock
   ↓
Record Stock Movement
   ↓
Create Shipment
   ↓
Order Completed
   ↓
Generate Reports
```

---

## Tech Stack

- **Java 17**
- **Spring Boot**
- **Spring Security**
- **JWT**
- **Spring Data JPA / Hibernate**
- **MySQL**
- **Flyway Migration**
- **Swagger / OpenAPI**
- **Maven**
- **Docker**
- **VPS Deployment**

---

## Project Structure

```text
src/main/java/com/dibimbing/inventory_sales_api
├── config
├── constant
├── controller
├── dto
├── entity
├── exception
├── repository
├── security
├── service
└── util
```

Project ini menggunakan **layered architecture** agar struktur kode lebih modular, rapi, dan mudah dikembangkan.

---

## Main Modules

- **Auth Module** → register, login, JWT authentication
- **User & Role Module** → user management dan authorization
- **Product Module** → CRUD produk dan kategori
- **Warehouse Module** → pengelolaan gudang
- **Customer Module** → data customer
- **Stock Module** → stok per warehouse
- **Stock Movement Module** → histori perubahan stok
- **Sales Order Module** → transaksi order
- **Payment Module** → transaksi pembayaran
- **Shipment Module** → pengiriman barang
- **Report Module** → laporan top products dan low stock

---

## Security

Sistem security menggunakan **Spring Security** dengan pendekatan **stateless authentication** berbasis JWT.

- User login untuk mendapatkan token
- Token dikirim melalui header `Authorization`
- Token divalidasi sebelum mengakses endpoint yang dilindungi
- Akses endpoint tertentu dibatasi berdasarkan role

---

## Database Migration

Project ini menggunakan **Flyway Migration** untuk mengelola schema database.

Contoh migration:
- `V1__init_schema.sql`
- `V2__seed_master.sql`

Dengan Flyway, struktur database menjadi lebih terkontrol, konsisten, dan mudah direplikasi di berbagai environment.

---

## API Documentation

Dokumentasi API disediakan menggunakan **Swagger / OpenAPI** agar endpoint dapat dilihat dan diuji dengan lebih mudah.

---

## Development Process

```text
Plan → Design → Code → Test → Deploy → Documentation
```

- **Plan** → Menentukan tema project, masalah, dan scope fitur
- **Design** → Mendesain database, arsitektur, dan endpoint API
- **Code** → Implementasi backend menggunakan Spring Boot
- **Test** → Pengujian endpoint dan business flow menggunakan Postman
- **Deploy** → Menyiapkan aplikasi untuk Docker / VPS
- **Documentation** → Menyusun Swagger, README, dan materi presentasi

---

## How to Run

### 1. Clone repository
```bash
git clone <repository-url>
cd inventory-final-project
```

### 2. Configure environment
Sesuaikan konfigurasi database dan environment pada file `application.properties` atau `.env` jika digunakan.

### 3. Run database migration
Pastikan MySQL aktif dan konfigurasi koneksi database sudah benar. Flyway akan menjalankan migration saat aplikasi dijalankan.

### 4. Run application
```bash
./mvnw spring-boot:run
```

Atau di Windows:
```bash
mvnw.cmd spring-boot:run
```

### 5. Access application
- API Base URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

---

## Author

**elBezali**  
Final Project – Backend Development Bootcamp
