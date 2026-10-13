# 🛒 My Cart

**My Cart** is a modern, feature-rich Android shopping application built with Jetpack Compose. It delivers a personalized shopping experience with real-time AI-powered recommendations, dynamic taste profiles, regional currency formatting (₹), a light-themed aesthetic, and an interactive live order-tracking system.

---

## ✨ Key Features

### 🤖 Personalized AI Recommendations Engine
- **Dynamic Taste Profiles:** Switch seamlessly between shopping personas (*Tech & Audiophile*, *Urban Minimalist*, *Smart Living*, *Active Wellness*) to instantly re-score and tailor the product catalog.
- **Affinity Match Scores:** View match percentage bars (e.g., 98% Match) complete with transparent rationales explaining why items were curated for you.
- **Preference Tuning:** Quick filter chips for *Top Rated (4.8★+)*, *Budget-Friendly*, and *Next-Day Delivery*.
- **Recommendations Carousel:** Highlights top curated products directly on the home screen.

### 🚚 Real-Time Live Order Tracking
- **Interactive Route Map (`OrderRouteCanvas`):** Custom animated canvas rendering city waypoints, glowing route paths, and a delivery vehicle navigating with a pulsing GPS radar beacon.
- **Live Dynamic ETA:** Real-time updates showing estimated arrival time and exact distance away (e.g., *11 mins • 2.1 km away*).
- **Courier Contact Profile:** Detailed courier info (*Liam Walker*, *Scoot EV Pro*, *4.95★*) with interactive Call and Message modals.
- **Milestone Timeline:** Clear progress indicators through *Order Confirmed*, *Packed*, *Out for Delivery*, *In Transit*, and *Delivered*.
- **Simulation Toolbar:** Test and control the delivery simulation with play/pause, manual checkpoint advancing, and route resets.

### 🛍️ E-Commerce Shopping & Checkout
- **Diverse Catalog:** Includes Indian clothing, groceries, laptops, mobile phones, lifestyle products, and home essentials.
- **Browse & Search:** Category filters, instant keyword search, and flash sale cards.
- **Product Detail Sheet:** Comprehensive technical specifications, finish/color swatches, delivery estimates, and instant checkout triggers.
- **Cart & Promo Vouchers:** Quantity controls, voucher support (e.g., `CARTIFY20` for 20% off), delivery address management, and one-tap checkout that kicks off live tracking.
- **Rupee Currency (₹):** Full localization using the Indian Rupee pricing structure and numbering format.

### 🎨 Clean Light Aesthetic
- **Airy Color Palette:** Built on a clean light background (`#F8FAFC`) with pure white card surfaces (`#FFFFFF`) and soft contrast borders (`#E2E8F0`).
- **Daytime Map UI:** Light-themed roads, subtle gridlines, and glowing gradient delivery paths.

---

## 🛠️ Tech Stack & Architecture
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** MVVM (Model-View-ViewModel)
- **Language:** Kotlin
- **Asynchronous:** Coroutines & Flow

---

## 🚀 Getting Started

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/NehaRaj316/My-Cart.git](https://github.com/NehaRaj316/My-Cart.git)
