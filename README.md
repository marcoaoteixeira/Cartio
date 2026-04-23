# Cartio — Shopping List Android App

Cartio is a personal-use shopping list Android application designed both as a learning project and a production-worthy utility for daily grocery shopping.

It focuses on simplicity, offline-first reliability, and gradual evolution into a lightweight personal grocery intelligence tool (with features like price history and reporting).

---

## ✨ Purpose

Cartio was built with two main goals:

- 📚 Learning modern Android development practices
- 🛒 Creating a practical, fast, and reliable grocery shopping tool

It intentionally avoids enterprise-level overengineering while still keeping a scalable architecture.

---

## 🧱 Tech Stack

- **Language:** Kotlin  
- **UI:** Jetpack Compose  
- **Architecture:** MVVM + Repository pattern  
- **Local Storage:** Room  
- **Async:** Coroutines + Flow  
- **Dependency Injection:** Hilt  
- **Navigation:** Navigation Compose  

---

## 🏗 Architecture Overview

Cartio follows a layered MVVM architecture:

### Presentation Layer
- Jetpack Compose screens
- ViewModels
- StateFlow-based UI state management

### Domain Layer (lightweight)
- Business logic use cases (only where meaningful)
- Future-ready for expansion (reports, sync, scanning)

### Data Layer
- Room (local database)
- Repository abstraction
- Designed to support future sync sources (e.g., Google Drive)

---

## 📦 Core Domain Model

The app is built around a normalized product-centric model:

### Entities

- **Product**
  - Canonical representation of an item (e.g., "Milk", "Bread")
  - Can include barcode data

- **ShoppingList**
  - Represents a user’s grocery list

- **ShoppingListItem**
  - Links products to a specific list
  - Stores quantity, notes, and checked state

- **PriceHistory**
  - Tracks historical prices for products
  - Enables reporting and price trend analysis

---

## 📱 Key Features

### v1 Core Features
- Multiple shopping lists
- Add/remove items
- Check-off items
- Optional quantity and unit support
- Estimated total cost per list
- Product-based structure (not just raw strings)

### Planned Features
- 📷 Barcode scanning (ML Kit)
- ☁️ Google Drive sync (optional, user-controlled)
- 📊 Price history reporting
- 📈 Spending trends and analytics
- 🏪 Store-based price comparison (future extension)

---

## 📡 Offline-First Strategy

Cartio is designed as an **offline-first application**:

- Room database is the source of truth
- Sync is optional and user-controlled
- No backend server required

### Sync Approach (Future)
- Google Drive JSON-based synchronization
- Simple conflict resolution (timestamp-based)
- User-owned data storage

---

## 🧭 UX Philosophy

- Single-screen-first design for speed
- Minimal navigation complexity
- Fast item entry during grocery shopping
- Low-friction interactions (tap-to-check, quick add)

Navigation will expand only where needed (e.g., Reports, Settings).

---

## 📊 Reporting Vision

Cartio is evolving beyond a simple checklist into a lightweight analytics tool:

Planned insights:
- Price trends over time
- Estimated monthly spending
- Cheapest observed price per product
- Purchase history summaries

---