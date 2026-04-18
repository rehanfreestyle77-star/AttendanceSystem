# 📋 Smart Attendance System

An Android application for managing student/staff attendance digitally — built with Java and Firebase Realtime Database. Designed for schools and institutions with a dual-role system for Principals and Teachers.

---

## 📱 Screenshots

| Login Screen | Principal Dashboard | Teacher Interface |
|---|---|---|
| ![Login](screenshots/login.png) | ![Principal](screenshots/principal_dashboard.png) | ![Teacher](screenshots/teacher_interface.png) |

> Replace the above image paths with your actual screenshot files.

---

## ✨ Features

### 👨‍💼 Principal
- Secure login with hardcoded admin credentials
- Create and delete teacher accounts
- View all teachers registered in the system
- Full control over user management

### 👩‍🏫 Teacher
- Login with credentials created by Principal
- Mark attendance for assigned classes in real-time
- View attendance history
- Change account password from profile screen

### 🔒 General
- Firebase Authentication for secure login
- Role-based access control (Principal vs Teacher)
- 100% real-time data sync via Firebase Realtime Database
- Clean, intuitive UI for both roles

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java |
| IDE | Android Studio |
| Backend / DB | Firebase Realtime Database |
| Authentication | Firebase Authentication |
| UI | XML Layouts, Material Design |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio (latest version recommended)
- A Firebase project set up at [console.firebase.google.com](https://console.firebase.google.com)
- Android device or emulator (API level 21+)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/rehanfreestyle77-star/AttendanceSystem.git
   cd AttendanceSystem
   ```

2. **Connect Firebase**
   - Go to [Firebase Console](https://console.firebase.google.com) → Create a project
   - Add an Android app with your package name
   - Download `google-services.json` and place it in the `app/` folder

3. **Enable Firebase Services**
   - Firebase Console → Authentication → Enable **Email/Password**
   - Firebase Console → Realtime Database → Create database → Start in **test mode**

4. **Set Principal Email**
   - In the code, the Principal account is hardcoded to a specific email
   - Update it in `LoginActivity.java` if needed:
     ```java
     private static final String PRINCIPAL_EMAIL = "rehanfreestyle77@email.com";
     ```

5. **Build & Run**
   - Open project in Android Studio
   - Click **Run ▶** or use `Shift + F10`

---

## 🗂️ Project Structure

```
AttendanceSystem/
├── app/
│   ├── src/main/
│   │   ├── java/
│   │   │   ├── LoginActivity.java
│   │   │   ├── PrincipalDashboardActivity.java
│   │   │   ├── TeacherDashboardActivity.java
│   │   │   └── ...
│   │   └── res/
│   │       ├── layout/
│   │       └── drawable/
│   └── google-services.json   ← (you add this)
└── README.md
```

---

## 🔐 Default Login (for testing)

| Role | Email | Password |
|---|---|---|
| Principal | `rehanfreestyle77@gmail.com` | *(set in Firebase)* |
| Teacher | Created by Principal | *(set by Principal)* |

---

## 📌 Future Improvements

- [ ] Teacher can change their own password from profile
- [ ] Export attendance reports as PDF
- [ ] Push notifications for attendance reminders
- [ ] Student-facing view to check their own attendance

---

## 👨‍💻 Author

**Rehan Shaikh**
- GitHub: [@rehanfreestyle77-star](https://github.com/rehanfreestyle77-star)
- Email: rihanff801@gmail.com

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
