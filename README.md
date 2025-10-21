#  TaskSync - Offline-First Android Task Manager

A modern Android task management application built with **Clean Architecture**, **Jetpack Compose**, and **offline-first synchronization**. This project demonstrates advanced Android development practices including modular architecture, dependency injection, background sync, and conflict resolution.

---

##  Project Overview

**TaskSync** is an Android application that allows users to manage tasks with full offline capability. Tasks are created, edited, and deleted locally first, then automatically synchronized with a remote server when connectivity is available.

### **Key Features:**
-  **Offline-First Architecture** - All operations work without internet
-  **Automatic Background Sync** - Seamless synchronization when online
-  **Conflict Resolution** - "Last Updated Wins" strategy
-  **Modern UI** - Built with Jetpack Compose and Material 3
-  **Clean Architecture** - Modular, testable, and maintainable
-  **Comprehensive Testing** - 68 tests across all layers
-  **Production Ready** - Enterprise-grade quality

---

##  Architecture

### **Clean Architecture Layers**

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────┐ │
│  │   MainActivity  │  │   ViewModels    │  │  Screens │ │
│  │   Navigation    │  │   (Auth/Tasks)  │  │ (Compose)│ │
│  └─────────────────┘  └─────────────────┘  └──────────┘ │
└─────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────┐
│                      Domain Layer                       │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────┐ │
│  │   Use Cases     │  │  Conflict       │  │ Entities │ │
│  │   (Business)    │  │  Resolver       │  │  (Task)  │ │
│  └─────────────────┘  └─────────────────┘  └──────────┘ │
└─────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────┐
│                       Data Layer                        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────┐ │
│  │  Repositories   │  │   Local DB      │  │ Network  │ │
│  │  (TaskRepo)     │  │   (Room)        │  │ (Retrofit)│ │
│  └─────────────────┘  └─────────────────┘  └──────────┘ │
└─────────────────────────────────────────────────────────┘
```

### **Module Structure**

```
TaskSync/
├── app/                          # Main application module
├── build-logic/                  # Gradle convention plugins
│   └── convention/
├── core/
│   ├── common/                   # Shared utilities
│   ├── data/                     # Data layer (repositories, mappers)
│   ├── database/                 # Room database
│   ├── domain/                   # Business logic (use cases, entities)
│   ├── network/                  # API services and DTOs
│   └── ui/                       # Shared UI components and themes
├── feature/
│   ├── auth/                     # Authentication feature
│   ├── sync/                     # Background sync feature
│   └── tasks/                    # Task management feature
└── json-server/                  # Mock backend server
```

### **Technology Stack**

| Layer | Technology | Purpose |
|-------|------------|---------|
| **UI** | Jetpack Compose, Material 3 | Modern declarative UI |
| **Navigation** | Navigation Compose | Type-safe navigation |
| **Architecture** | MVVM + Clean Architecture | Separation of concerns |
| **DI** | Hilt | Dependency injection |
| **Database** | Room | Local data persistence |
| **Network** | Retrofit + OkHttp | API communication |
| **Background** | WorkManager | Background sync |
| **Storage** | DataStore | User preferences |
| **Testing** | JUnit, MockK, Compose Testing | Comprehensive testing |

---

##  Getting Started

### **Prerequisites**

- **Android Studio** Hedgehog (2023.1.1) or later
- **JDK 17** or later
- **Android SDK** API 24+ (Android 7.0+)
- **Node.js** 16+ (for mock server)
- **Physical Android device** or emulator (API 24+)

### **1. Clone the Repository**

```bash
git clone 
cd TaskSyncdlight
```

### **2. Build the Project**

```bash
# Build all modules
./gradlew build

# Or build specific modules
./gradlew :app:assembleDebug
```

### **3. Run the Application**

**Option A: Android Studio**
1. Open the project in Android Studio
2. Connect a physical device or start an emulator
3. Click "Run" or press `Shift + F10`

**Option B: Command Line**
```bash
# Install on connected device
./gradlew :app:installDebug

# Or run directly
./gradlew :app:connectedDebugAndroidTest
```

---

##  Mock Server Setup

The application requires a mock backend server for API communication.

### **1. Install Dependencies**

```bash
cd json-server
npm install
```

### **2. Start the Server**

```bash
# Using npm (recommended)
npm start

# Or using json-server directly
json-server --watch db.json --port 3000

# Or using custom server with extra features
node server.js
```

### **3. Verify Server is Running**

The server will be available at:
- **Local:** `http://localhost:3000`
- **Android Emulator:** `http://10.0.2.2:3000` 
- **Physical Device:** `http://[YOUR_COMPUTER_IP]:3000`, Go to `NetworkModule.kt` and set BASE_URL accordingly.

### **4. Test the API**

```bash
# Test login endpoint
curl -X POST http://localhost:3000/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@dlight.com"}'

# Test tasks endpoint
curl http://localhost:3000/tasks
```

---

##  Conflict Resolution Strategy

The application implements a **"Last Updated Wins"** conflict resolution strategy:

### **How It Works**

1. **Conflict Detection**: Compares `updatedAt` timestamps between local and remote versions
2. **Resolution Logic**: The version with the newer `updatedAt` timestamp wins
3. **Tie-Breaker**: If timestamps are equal, local version is preferred
4. **Content Validation**: Also compares actual content to detect meaningful conflicts

### **Conflict Resolution Flow**

```
┌─────────────────────────────────────────────────────────┐
│                Conflict Detection                       │
│                                                         │
│  Local Task:  updatedAt = 2025-01-17T10:00:00Z        │
│  Remote Task: updatedAt = 2025-01-17T11:00:00Z        │
│                                                         │
│  Result: Remote wins (newer timestamp)                 │
└─────────────────────────────────────────────────────────┘
```

### **Implementation Details**

- **ConflictResolver**: Pure domain logic for conflict resolution
- **Automatic Detection**: Conflicts are detected during sync operations
- **User Transparency**: Users see the resolved version without manual intervention
- **Data Integrity**: No data loss - conflicts are resolved predictably

---

##  Testing

The project includes **68 comprehensive tests** across all layers with **100% pass rate** on physical devices.

### **Test Categories**

| Test Type | Count | Purpose | Speed |
|-----------|-------|---------|-------|
| **Unit Tests** | 26 | Business logic, use cases | Fast (~7s) |
| **DAO Tests** | 11 | Database operations | Medium (~32s) |
| **Integration Tests** | 3 | End-to-end sync flows | Medium (~40s) |
| **UI Tests** | 28 | User interface testing | Medium (~27s) |
| **Total** | **68** | **Complete coverage** | **~106s** |

### **Running Tests**

#### **All Tests**
```bash
# Run all unit tests
./gradlew test

# Run all instrumented tests (requires device)
./gradlew connectedDebugAndroidTest
```

#### **Specific Test Categories**
```bash
# Unit tests only
./gradlew :core:domain:test
./gradlew :core:data:test
./gradlew :feature:sync:test

# Database tests
./gradlew :core:database:connectedDebugAndroidTest

# UI tests
./gradlew :feature:auth:connectedDebugAndroidTest
./gradlew :feature:tasks:connectedDebugAndroidTest

# Integration tests
./gradlew :core:data:connectedDebugAndroidTest
```

#### **Test Reports**
```bash
# View HTML test reports
open */build/reports/tests/*/index.html
open */build/reports/androidTests/connected/*/index.html
```

### **Test Coverage Details**

#### **Unit Tests (26 tests)**
-  **ConflictResolver** (8 tests) - Conflict resolution logic
-  **CreateTaskUseCase** (4 tests) - Task creation business logic
-  **TaskRepositoryImpl** (10 tests) - Repository operations
-  **SyncManager** (4 tests) - Sync orchestration

#### **DAO Tests (11 tests)**
-  **CRUD Operations** - Create, Read, Update, Delete
-  **Upsert Functionality** - Insert or update tasks
-  **Flow Observation** - Reactive data streams
-  **Sync Status Filtering** - Pending/Synced tasks
-  **Batch Operations** - Multiple task operations

#### **Integration Tests (3 scenarios)**
-  **Offline Create → Sync** - Create task offline, sync when online
-  **Offline Edit → Sync** - Edit task offline, sync changes
-  **Conflict Resolution** - Handle concurrent modifications

#### **UI Tests (28 tests - Run on Actual Device)**
-  **Login Screen** (11 tests) - Authentication UI
-  **Tasks Screen** (6 tests) - Task list UI
-  **Add Task Screen** (11 tests) - Task creation UI

### **Test Quality Metrics**

-  **100% Pass Rate** on physical devices
-  **Comprehensive Coverage** across all layers
-  **Fast Execution** for unit tests
-  **Reliable Results** with stable test identifiers
-  **Maintainable Code** with clear patterns

---

## 📱 Features

### **Authentication**
- Simple email-based login (no password required)
- Token-based authentication
- Automatic session management
- Secure token storage with DataStore

### **Task Management**
- Create, edit, and delete tasks
- Mark tasks as complete/incomplete
- Rich task descriptions
- Real-time task list updates

### **Offline-First Design**
- All operations work without internet
- Tasks saved locally immediately
- Visual indicators for sync status
- Seamless online/offline transitions

### **Automatic Synchronization**
- Background sync every 15 minutes
- Immediate sync when network available
- Conflict resolution with "Last Updated Wins"
- Pending task count tracking

### **Modern UI**
- Material 3 design system
- Dark/light theme support
- Responsive layouts
- Smooth animations and transitions
- Pull-to-refresh functionality

---

##  Development

### **Build Logic Conventions**

The project uses custom Gradle convention plugins for consistent configuration:

```kotlin
// Android Library Convention
alias(libs.plugins.tasksync.android.library)

// Android Feature Convention (includes Compose)
alias(libs.plugins.tasksync.android.feature)

// Hilt Convention
alias(libs.plugins.tasksync.android.hilt)

// Room Convention
alias(libs.plugins.tasksync.android.room)
```

### **Dependency Management**

Dependencies are managed through `libs.versions.toml` with bundles for related libraries:

```toml
[versions]
compose = "1.5.4"
hilt = "2.48"
room = "2.6.1"

[bundles]
compose = ["androidx-compose-ui", "androidx-compose-material3", ...]
lifecycle = ["androidx-lifecycle-runtime-ktx", "androidx-lifecycle-viewmodel-ktx", ...]
testing = ["junit", "mockk", "kotlinx-coroutines-test", "turbine"]
```

### **Code Quality**

-  **Kotlin Coding Standards** - Consistent code style
-  **Clean Architecture** - Proper separation of concerns
-  **Dependency Injection** - Hilt for testable code
-  **Reactive Programming** - StateFlow for UI state
-  **Error Handling** - Comprehensive error management
-  **Logging** - Timber for debugging

---

##  Documentation

The project includes comprehensive documentation:
- **SYNC_SYSTEM.md** - Detailed sysnc system documentation
- **json-server/README.md** - Mock server documentation
