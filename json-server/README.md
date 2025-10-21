#  TaskSync Mock Server - d.light

Mock backend server for the TaskSync Android application using json-server.

##  Quick Start

### 1. Install Dependencies

```bash
cd json-server
npm install
```

Or install json-server globally:
```bash
npm install -g json-server
```

### 2. Start the Server

**Using npm (recommended):**
```bash
npm start
```

**Or using json-server directly:**
```bash
json-server --watch db.json --port 3000
```

**Or using custom server (with extra features):**
```bash
node server.js
```

The server will start at:
- **Local:** `http://localhost:3000`
- **Android Emulator:** `http://10.0.2.2:3000`

---
