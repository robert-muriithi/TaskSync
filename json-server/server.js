const jsonServer = require('json-server');
const server = jsonServer.create();
const router = jsonServer.router('db.json');
const middlewares = jsonServer.defaults();

// Validate and fix existing tasks on startup
const db = router.db;
const now = new Date().toISOString();
let tasksFixed = 0;

db.get('tasks').value().forEach((task, index) => {
  let needsUpdate = false;
  
  if (!task.createdAt) {
    task.createdAt = task.updatedAt || now;
    needsUpdate = true;
  }
  
  if (!task.updatedAt) {
    task.updatedAt = task.createdAt || now;
    needsUpdate = true;
  }
  
  if (needsUpdate) {
    db.get('tasks').nth(index).assign(task).write();
    tasksFixed++;
  }
});

if (tasksFixed > 0) {
  console.log(`🔧 Fixed ${tasksFixed} tasks with missing timestamps`);
}

// Add custom middleware
server.use(middlewares);
server.use(jsonServer.bodyParser);

// Middleware to ensure all tasks have timestamps
server.use((req, res, next) => {
  if (req.method === 'POST' && req.path === '/tasks') {
    // Auto-generate timestamps for new tasks
    const now = new Date().toISOString();
    req.body.createdAt = req.body.createdAt || now;
    req.body.updatedAt = req.body.updatedAt || now;
    console.log(`✅ Auto-generated timestamps for new task: ${req.body.id || 'new'}`);
  } else if ((req.method === 'PUT' || req.method === 'PATCH') && req.path.startsWith('/tasks/')) {
    // Auto-update updatedAt for existing tasks
    const now = new Date().toISOString();
    
    // Preserve createdAt if it exists in the request
    if (!req.body.createdAt) {
      const taskId = req.path.split('/').pop();
      const existingTask = db.get('tasks').find({ id: taskId }).value();
      if (existingTask && existingTask.createdAt) {
        req.body.createdAt = existingTask.createdAt;
      } else {
        req.body.createdAt = now;
      }
    }
    
    req.body.updatedAt = now; // Always update this on PUT/PATCH
    console.log(`✅ Auto-updated timestamp for task: ${req.path.split('/').pop()}`);
  }
  next();
});

// Custom auth endpoint - no password required, just email
server.post('/auth/login', (req, res) => {
  const { email } = req.body;
  
  if (!email) {
    return res.status(400).json({ 
      error: 'Email is required' 
    });
  }

  // Mock authentication - always succeeds with valid email
  const userId = `user-${Date.now()}`;
  const token = `mock-token-${Buffer.from(email).toString('base64')}-${Date.now()}`;
  
  console.log(`✅ Login successful: ${email}`);
  
  res.status(200).json({
    id: userId,
    email: email,
    token: token
  });
});

// GET /tasks with timestamp filtering (for incremental sync)
server.get('/tasks', (req, res) => {
  const db = router.db; // Access the lowdb instance
  let tasks = db.get('tasks').value();
  
  // Filter by 'since' or 'updatedAt_gte' parameter (both supported)
  const since = req.query.since || req.query.updatedAt_gte;
  if (since) {
    const sinceDate = new Date(since);
    tasks = tasks.filter(task => {
      const taskDate = new Date(task.updatedAt);
      return taskDate >= sinceDate;
    });
    console.log(`📥 Fetching tasks since ${since}: ${tasks.length} tasks`);
  } else {
    console.log(`📥 Fetching all tasks: ${tasks.length} tasks`);
  }
  
  res.status(200).json(tasks);
});

// POST /sync - batch sync endpoint (accepts array of changes)
server.post('/sync', (req, res) => {
  const { tasks: incomingTasks } = req.body;
  
  if (!incomingTasks || !Array.isArray(incomingTasks)) {
    return res.status(400).json({ 
      error: 'Invalid sync request. Expected { tasks: [...] }' 
    });
  }

  const db = router.db;
  const existingTasks = db.get('tasks').value();
  const results = {
    created: [],
    updated: [],
    conflicts: []
  };

  incomingTasks.forEach(incomingTask => {
    // Ensure timestamps exist
    const now = new Date().toISOString();
    if (!incomingTask.createdAt) {
      incomingTask.createdAt = now;
    }
    if (!incomingTask.updatedAt) {
      incomingTask.updatedAt = now;
    }
    
    const existingIndex = existingTasks.findIndex(t => t.id === incomingTask.id);
    
    if (existingIndex === -1) {
      // Create new task
      db.get('tasks').push(incomingTask).write();
      results.created.push(incomingTask.id);
      console.log(`✅ Created task: ${incomingTask.id}`);
    } else {
      const existingTask = existingTasks[existingIndex];
      const existingDate = new Date(existingTask.updatedAt);
      const incomingDate = new Date(incomingTask.updatedAt);
      
      if (incomingDate > existingDate) {
        // Update task (incoming is newer)
        db.get('tasks')
          .find({ id: incomingTask.id })
          .assign(incomingTask)
          .write();
        results.updated.push(incomingTask.id);
        console.log(`✅ Updated task: ${incomingTask.id}`);
      } else if (incomingDate < existingDate) {
        // Conflict - server has newer version
        results.conflicts.push({
          taskId: incomingTask.id,
          reason: 'Server has newer version',
          serverVersion: existingTask
        });
        console.log(`⚠️  Conflict detected for task: ${incomingTask.id}`);
      } else {
        // Same timestamp, no action needed
        console.log(`ℹ️  No change for task: ${incomingTask.id}`);
      }
    }
  });

  console.log(`🔄 Sync complete: ${results.created.length} created, ${results.updated.length} updated, ${results.conflicts.length} conflicts`);
  
  res.status(200).json({
    success: true,
    results: results
  });
});

// Logging middleware for debugging
server.use((req, res, next) => {
  console.log(`${req.method} ${req.url}`);
  next();
});

// Use default router for other endpoints
server.use(router);

// Start server
const PORT = 3000;
server.listen(PORT, () => {
  console.log('');
  console.log('╔════════════════════════════════════════════════════════════╗');
  console.log('║                                                            ║');
  console.log('║         📱 TaskSync Mock Server - d.light                 ║');
  console.log('║                                                            ║');
  console.log('╚════════════════════════════════════════════════════════════╝');
  console.log('');
  console.log(`🚀 Server running at: http://localhost:${PORT}`);
  console.log(`📱 Android Emulator: http://10.0.2.2:${PORT}`);
  console.log('');
  console.log('📡 Available Endpoints:');
  console.log(`   POST   /auth/login              - Login (no password)`);
  console.log(`   GET    /tasks                   - Get all tasks`);
  console.log(`   GET    /tasks?updatedAt_gte=... - Get tasks since timestamp`);
  console.log(`   POST   /tasks                   - Create task`);
  console.log(`   PUT    /tasks/:id               - Update task`);
  console.log(`   DELETE /tasks/:id               - Delete task`);
  console.log(`   POST   /sync                    - Batch sync`);
  console.log('');
  console.log('📝 Press Ctrl+C to stop');
  console.log('');
});
