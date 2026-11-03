# 🛒 RUCART — Quick-Commerce 10-Minute Grocery Delivery Platform

[![Android](https://img.shields.io/badge/Platform-Android%20Java-3DDC84.svg?style=flat&logo=android)](https://developer.android.com/)
[![Spring Boot](https://img.shields.io/badge/Backend-Spring%20Boot%203.3-6DB33F.svg?style=flat&logo=springboot)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/Database-MySQL%208-4479A1.svg?style=flat&logo=mysql)](https://www.mysql.com/)

A commercial-grade quick-commerce grocery delivery application inspired by **Blinkit**, **Zepto**, and **BigBasket**. Built using a native **Android (Java + Material 3)** frontend and a robust **Spring Boot 3 + MySQL 8** backend.

---

## 📱 Screenshots & Visual Design

- **Customer App**: Emerald green (`#0B6E3F`) visual identity, live location selector (`📍 Shivajinagar, Pune`), 3-dot promotional carousel, sponsored freshness banner, and 15 Blinkit category departments with local assets.
- **Store Owner App**: Live store status toggle (`ONLINE` / `OFFLINE`), 4-card overview grid, inbound batch tracker, warehouse procurement modal, and top 10 SKU restock.
- **Delivery Partner App**: Duty status toggle, earnings dashboard, active delivery transit workflow, and 4-digit customer OTP verification.
- **Admin Console**: Platform statistics, store approvals, catalog management, and audit logging.

---

## 🏛️ Architecture Overview

```
rucart_project/
├── backend/                              # Java 21 / Spring Boot 3 + MySQL 8 REST API
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/rucart/backend/
│       │   ├── config/                   # Web Security, CORS, Exception Handling
│       │   ├── controller/               # Auth, Products, Stores, Orders, Payments, Delivery, Admin
│       │   ├── dto/                      # Requests, Responses & Data Transfer Objects
│       │   ├── model/                    # JPA Entities (Order, Payment, Product, Store, User...)
│       │   ├── repository/               # Spring Data JPA Repositories
│       │   ├── security/                 # JWT Authentication Filter & Token Utilities
│       │   ├── service/                  # Business Logic (OrderService, PaymentService, etc.)
│       │   └── util/                     # DataInitializer (Auto-seeds initial catalog & test accounts)
│       └── resources/
│           └── application.properties    # DataSource, Payment Gateway, JWT config
│
└── android/                              # Native Android Studio Project (Java & XML)
    ├── build.gradle / settings.gradle
    ├── gradlew / gradlew.bat
    └── app/
        ├── build.gradle                  # compileSdk 34, ViewBinding, Retrofit 2
        └── src/main/
            ├── AndroidManifest.xml
            ├── java/com/rucart/app/      # Splash, Customer, Store, Delivery, Admin, Auth Activities
            └── res/                      # 31+ high-res grocery drawables, layouts, styles, colors
```

---

## 👥 Default Demo Credentials

The backend automatically seeds these test accounts into MySQL:

| Role | Name | Phone Number | Password | Default Functionality |
| :--- | :--- | :--- | :--- | :--- |
| **CUSTOMER** | Rahul Sharma | `9876543210` | `123456` | Direct grocery shopping, addresses, orders |
| **STORE_OWNER** | Amit Patil | `9876543211` | `123456` | RUCART Express Store inventory & orders |
| **DELIVERY_PARTNER** | Priya Desai | `9876543212` | `123456` | Motorbike delivery dispatch with OTP |
| **ADMIN** | Admin Rucart | `9876543213` | `admin123` | Platform analytics & store oversight |

> **Note**: To access Store, Delivery, or Admin portals, open the Customer App, tap the **Account** tab, and select **"Merchant & Partner Portal"**.

---

## 💳 Payment Gateway

Integrated payment processing:
- **Payment Options in Checkout**:
  1. `💳 Pay Online (UPI, Cards, Net Banking)` — **Default**.
  2. `💵 Cash on Delivery`.
- **Direct Auto-Acceptance**:
  - Orders are **auto-accepted (`ACCEPTED`) upon creation & payment confirmation**.
  - No "Pending" status and no waiting for store owner approval before releasing.
- **Delivery Verification OTP**: Generates a 4-digit code required by the rider to mark an order delivered.

---

## 🚀 Setup & Execution Guide

### Prerequisites
- **JDK**: Version 21 (e.g. `C:\Users\KJ Library 07\.jdks\jbr-21.0.11`)
- **Maven**: Version 3.9+
- **MySQL**: 8.0+ running on port `3306`
- **Android SDK**: compileSdk 34, Android Platform Tools (`adb`)

---

### 1. Database Setup
Ensure MySQL is running on port `3306`:
```sql
CREATE DATABASE IF NOT EXISTS rucart_db;
```

### 2. Start Spring Boot Backend
```powershell
cd backend
$env:JAVA_HOME = "C:\Users\KJ Library 07\.jdks\jbr-21.0.11"
& "C:\Program Files\apache-maven-3.10.0\bin\mvn.cmd" spring-boot:run
```
*Backend runs on `http://localhost:8080` and automatically populates initial catalog data.*

### 3. Build & Install Android App
```powershell
cd android
$env:JAVA_HOME = "C:\Users\KJ Library 07\.jdks\jbr-21.0.11"
.\gradlew.bat assembleDebug

# Install on emulator or device:
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb install -r app\build\outputs\apk\debug\app-debug.apk

# Launch the app:
& $adb shell am start -n com.rucart.app/.ui.splash.SplashActivity
```

---

## 📡 Core API Reference

| Method | Endpoint | Description | Role |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/login` | Phone & password authentication | Public |
| `POST` | `/api/v1/auth/register` | Customer registration | Public |
| `GET` | `/api/v1/categories` | Active grocery categories | Public |
| `GET` | `/api/v1/products` | Search catalog with filters | Public |
| `POST` | `/api/v1/orders` | Create order with stock reservation | Customer |
| `GET` | `/api/v1/orders/my-orders` | Customer order history | Customer |
| `POST` | `/api/v1/payments/initiate` | Create payment gateway session | Customer |
| `POST` | `/api/v1/payments/verify` | Verify cryptographic HMAC signature | Customer |
| `GET` | `/api/v1/store-owner/orders` | Store fulfillment queue | Store Owner |
| `POST` | `/api/v1/delivery/orders/{id}/deliver`| Verify OTP and finish delivery | Delivery Partner |
| `GET` | `/api/v1/admin/dashboard` | Platform metrics & revenue | Admin |
