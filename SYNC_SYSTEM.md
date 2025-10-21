#  Automatic Sync System Documentation

## Overview

The sync system is a **fully automatic offline-first synchronization** that keeps local tasks in sync with the remote server. It's designed to work seamlessly without user intervention.

---

## 🎯 Key Features

### 1. **Immediate Network-Based Sync** 
-  Syncs **immediately** when network becomes available
-  Syncs on app startup if network is connected
-  Monitors network changes in real-time
-  Handles network transitions gracefully

### 2. **Automatic Background Sync** 
-  WorkManager runs sync every 1 minute
-  Works even when app is closed
-  Only runs when network is available
-  Exponential backoff on failures

### 3. **State Tracking for UI** 
-  `syncState`: Current sync status (Idle, Syncing, Success, Error)
-  `lastSyncTime`: Timestamp of last successful sync
-  `pendingTaskCount`: Number of tasks waiting to sync
-  All exposed as StateFlows (reactive)

### 4. **Smart Conflict Resolution** 
-  Uses `ConflictResolver` (Last Updated Wins)
-  Detects conflicts by comparing content + timestamps
-  Handles edge cases (PENDING, SYNCING states)
-  Preserves user data

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Application Start                     │
│                                                           │
│  TaskSyncApplication.onCreate()                          │
│      ↓                                                    │
│  syncInitializer.initialize(applicationScope)            │
│                                                           │
│  ┌────────────────────────────────────────────────┐     │
│  │  1. SyncManager.startAutoSync()                │     │
│  │     • Monitors NetworkMonitor.isOnline         │     │
│  │     • Syncs immediately if network available   │     │
│  │     • Reacts to network state changes          │     │
│  │     • Collects and logs sync states            │     │
│  │     • Updates pending task count               │     │
│  └────────────────────────────────────────────────┘     │
│                                                           │
│  ┌────────────────────────────────────────────────┐     │
│  │  2. SyncWorkScheduler.schedulePeriodicSync()   │     │
│  │     • WorkManager periodic sync (1 min)       │     │
│  │     • Only runs when network available         │     │
│  │     • Survives app restarts                    │     │
│  └────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────┘
                           ↓
                           ↓
┌─────────────────────────────────────────────────────────┐
│                    Network Connected                     │
│                                                           │
│  NetworkMonitor emits true                               │
│      ↓                                                    │
│  SyncManager.syncTasks() triggered                       │
│      ↓                                                    │
│  TaskRepository.syncTasks()                              │
│      ↓                                                    │
│  ┌────────────────────────────────────────────────┐     │
│  │  STEP 1: Push Pending Tasks                    │     │
│  │  • Get all PENDING tasks from Room             │     │
│  │  • Mark as SYNCING                             │     │
│  │  • Try UPDATE on server (PUT)                  │     │
│  │  • If 404, CREATE on server (POST)             │     │
│  │  • Mark as SYNCED if successful                │     │
│  └────────────────────────────────────────────────┘     │
│      ↓                                                    │
│  ┌────────────────────────────────────────────────┐     │
│  │  STEP 2: Pull Remote Changes                   │     │
│  │  • GET /tasks?updatedAt_gte=lastSyncTime       │     │
│  │  • Only fetch changed tasks (efficient)        │     │
│  └────────────────────────────────────────────────┘     │
│      ↓                                                    │
│  ┌────────────────────────────────────────────────┐     │
│  │  STEP 3: Resolve Conflicts                     │     │
│  │  • For each remote task:                       │     │
│  │    - New? Insert into Room                     │     │
│  │    - Exists?                                    │     │
│  │      • Skip if local has PENDING changes       │     │
│  │      • ConflictResolver.hasConflict()          │     │
│  │      • ConflictResolver.resolveConflict()      │     │
│  │      • Update Room with winner                 │     │
│  └────────────────────────────────────────────────┘     │
│      ↓                                                    │
│  ┌────────────────────────────────────────────────┐     │
│  │  STEP 4: Update State                          │     │
│  │  • UserPreferences.updateLastSyncTime()        │     │
│  │  • syncState.value = Success                   │     │
│  │  • lastSyncTime.value = now                    │     │
│  │  • UI updates automatically (Flow)             │     │
│  └────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────┘
```

---

## KeyNotes

### 1. **lastSyncTime: StateFlow<Long>**

Timestamp (milliseconds) of the last successful sync.

**When it updates:**
- After every successful sync
- Initial value: `0` (never synced)

**Use cases:**
- Show "Last synced at 10:30 AM" in UI
- Calculate time since last sync
- Determine if sync is needed

---

### 2. **pendingTaskCount: StateFlow<Int>**

Number of tasks waiting to be synced (marked as PENDING or SYNCING).

**When it updates:**
- After successful sync
- Every 30 seconds (automatically)
- When tasks are created/updated locally

**Use cases:**
- Show "3 tasks pending sync" badge
- Indicate unsaved changes
- Trigger manual sync if count is high

---

## 🔧 How Automatic Sync Works

### Scenario 1: User Creates Task Offline

```
1. User taps "Create Task"
   ↓
2. ViewModel.createTask()
   ↓
3. TaskRepository.createTask()
   • Saves to Room with syncStatus = PENDING
   • UI updates immediately (Flow) 
   ↓
4. Network comes back
   ↓
5. NetworkMonitor emits true
   ↓
6. SyncManager.syncTasks() triggered automatically
   ↓
7. TaskRepository.syncTasks()
   • Finds PENDING task
   • POSTs to server
   • Updates syncStatus = SYNCED
   ↓
8. UI updates automatically (Flow) 
   • Sync badge disappears
   • Success message shows
```

**No manual sync needed!** 

---

### Scenario 2: App Starts with Network

```
1. App starts
   ↓
2. TaskSyncApplication.onCreate()
   ↓
3. syncInitializer.initialize()
   ↓
4. SyncManager.startAutoSync()
   • Collects networkMonitor.isOnline
   • Network is already true
   ↓
5. 500ms delay (ensure network stable)
   ↓
6. SyncManager.syncTasks() triggered automatically
   ↓
7. Tasks sync immediately 
```

**User sees fresh data instantly!** 

---

### Scenario 3: Conflict Resolution

```
User A (offline):
  Edits "Buy milk" → "Buy milk and eggs"
  updatedAt = 10:00 AM
  syncStatus = PENDING

User B (online):
  Edits same task → "Buy milk and bread"
  updatedAt = 10:05 AM
  Synced to server 

User A comes online:
  ↓
1. SyncManager detects network
   ↓
2. Push phase:
   • Tries to push "Buy milk and eggs"
   • PUT /tasks/123 → Response with server version
   ↓
3. Pull phase:
   • GET /tasks?since=lastSync
   • Receives "Buy milk and bread" (10:05 AM)
   ↓
4. Conflict detection:
   • ConflictResolver.hasConflict()
     → title differs 
     → timestamps differ 
     → CONFLICT!
   ↓
5. Conflict resolution:
   • ConflictResolver.resolveConflict()
   • Compare: 10:05 AM > 10:00 AM
   • Resolution: KeepRemote
   ↓
6. Update local:
   • Room updates to "Buy milk and bread"
   • syncStatus = SYNCED
   ↓
7. User A sees:
   • "Buy milk and bread" (remote version won)
   • No sync errors
   • Clean state
```





