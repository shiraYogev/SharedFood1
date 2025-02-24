**A food-sharing app to reduce food waste and connect people in need.**  

---

## 📖 Project Description
**ShareFood** is a mobile application designed to help users **share and receive food** that would otherwise go to waste. The platform allows individuals, restaurants, and businesses to **post food donations**, making it easy for those in need to find and collect food items before they expire.

---

## ✨ Features  
✔️ **User Authentication** – Sign in with email/password and third-party authentication (Google, Facebook).  
✔️ **Food Posts** – Users can create, edit, and delete food donation posts with images, descriptions, and location.  
✔️ **Admin Controls** – Admins can manage users and delete inappropriate posts.  
✔️ **Chat System** – Built-in messaging feature for users to coordinate food pickup.  
✔️ **Filters & Search** – Users can filter food posts based on preferences (vegetarian, gluten-free, etc.).  
✔️ **Geolocation Integration** – Users can view nearby food donations on a map.  
✔️ **Push Notifications** – Alerts for new food posts and chat messages.  
✔️ **Admin Dashboard** – Tools for managing users, blocking/unblocking, and promoting users to admin.  

---

## 🛠️ Tech Stack  
| **Category**         | **Technology**  |
|----------------------|----------------|
| **Frontend**        | Jetpack Compose, XML UI |
| **Backend**         | Firebase Firestore, Firebase Authentication, Firebase Storage |
| **Programming Language** | Kotlin |
| **Database**        | Firebase Firestore |
| **Authentication**  | Firebase Auth (Google, Facebook, Email/Password) |
| **Real-time Messaging** | Firestore & Firebase Cloud Messaging |
| **Architecture**    | MVVM (Model-View-ViewModel) |

---

## 📂 Project Structure  

```
📦 sharedfood
 ┣ 📂 activitiesAdmin        # Admin-related activities (User list, Post management)
 ┣ 📂 admin                  # Admin-related classes (Adapters, Models)
 ┣ 📂 chat                   # Chat feature (Adapters, Models, Managers)
 ┣ 📂 post                   # Post-related components (Adapters, Models)
 ┣ 📜 MainActivity.kt        # Entry point of the app
 ┣ 📜 build.gradle.kts       # Project dependencies
 ┣ 📜 google-services.json   # Firebase configuration (NOT included in the repo)
```

---

## 🚀 Getting Started  

### 1️⃣ Prerequisites  
- **Android Studio (latest version)**  
- **Firebase Project** (with Firestore, Auth, and Storage enabled)  

### 2️⃣ Installation  
1️⃣ **Clone the Repository:**  
```bash
git clone https://github.com/your-repo/ShareFood.git
cd ShareFood
```
2️⃣ **Add Firebase Configuration:**  
- Download `google-services.json` from Firebase and place it in `app/` directory.  

3️⃣ **Run the Application:**  
- Open the project in **Android Studio**.  
- Click **Run** ▶ on an **emulator** or **physical device**.  

---

## ⚡ Future Enhancements  
- ✅ **Live Food Tracking** – Allow users to see real-time updates on available food.  
- ✅ **Donation Analytics** – Track and display stats on shared food items.  
- ✅ **Expanded User Roles** – Introduce "verified donors" and "trusted recipients".  



---

