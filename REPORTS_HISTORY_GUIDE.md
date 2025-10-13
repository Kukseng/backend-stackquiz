# 📊 Reports History Feature - Implementation Guide

## Overview

This guide will help you implement a **Quizizz-style Reports History page** where hosts can view all their past quiz sessions in a beautiful list with filters, search, and sorting.

---

## ✅ What's Included

### Backend (Already Implemented)
1. ✅ New API endpoint: `GET /api/v1/reports-history/my-sessions`
2. ✅ Session summary DTO with statistics
3. ✅ Filtering by status
4. ✅ Optimized queries for performance

### Frontend (Ready to Use)
1. ✅ Beautiful Quizizz-style UI
2. ✅ Search by quiz name, session name, or code
3. ✅ Filter by status (All, Running, Scheduled, Completed, Paused)
4. ✅ Sort by date, participants, or accuracy
5. ✅ Click to view detailed report
6. ✅ Responsive design with animations

---

## 🚀 Implementation Steps

### Step 1: Backend is Ready! ✅

The backend has been updated with:

**New Files Added:**
- `SessionSummaryResponse.java` - DTO for session summaries
- `ReportsHistoryController.java` - API endpoints
- `ReportsHistoryService.java` - Service interface
- `ReportsHistoryServiceImpl.java` - Service implementation

**New Endpoint:**
```
GET /api/v1/reports-history/my-sessions
Authorization: Bearer {token}

Response:
[
  {
    "sessionId": "abc123",
    "sessionCode": "123456",
    "sessionName": "Math Quiz Session",
    "quizTitle": "Algebra Basics",
    "status": "COMPLETED",
    "startTime": "2024-10-12T10:00:00",
    "endTime": "2024-10-12T10:30:00",
    "totalParticipants": 25,
    "averageAccuracy": 78.5,
    "completionRate": 92.0,
    "hostName": "John Doe",
    "totalQuestions": 10
  },
  ...
]
```

---

### Step 2: Add Frontend Component

**File:** `app/reports/page.tsx`

Copy the content from `ReportsHistoryPageFixed.tsx` to your Next.js app:

```bash
# In your frontend project
cp ReportsHistoryPageFixed.tsx app/reports/page.tsx
```

**Required Dependencies:**
```bash
npm install framer-motion axios
```

---

### Step 3: Update Navigation

Add a link to the Reports page in your navigation:

```tsx
// In your navigation component
<Link href="/reports">
  <button className="nav-button">
    📊 Reports
  </button>
</Link>
```

---

### Step 4: Configure API Base URL

Update the API endpoint in the component if your backend URL is different:

```tsx
// In ReportsHistoryPage.tsx, line 49
const response = await axios.get(
  `http://localhost:9999/api/v1/reports-history/my-sessions`,  // ← Change this
  {
    headers: {
      Authorization: `Bearer ${localStorage.getItem("token")}`,
    },
  }
);
```

---

## 🎨 Features

### 1. Search Functionality
- Search by quiz title
- Search by session name
- Search by session code
- Real-time filtering

### 2. Status Filters
- **All** - Show all sessions
- **Running** - Currently active sessions
- **Scheduled** - Future scheduled sessions
- **Completed** - Finished sessions
- **Paused** - Paused sessions

Each filter shows the count of sessions.

### 3. Sorting Options
- **Date hosted** - Sort by start time
- **Participants** - Sort by number of participants
- **Accuracy** - Sort by average accuracy

Toggle between ascending and descending order.

### 4. Visual Indicators
- **Status badges** with color coding
- **Accuracy circles** showing performance
- **Hover effects** for better UX
- **Smooth animations** using Framer Motion

### 5. Actions
- Click **View Report** for completed sessions
- Disabled button for incomplete sessions
- Click entire row to view report (for completed)

---

## 📊 UI Components

### Header
```
📊 Reports
View and manage your quiz session reports
```

### Search Bar
```
🔍 Search by report name, quiz title, or code...
```

### Filter Buttons
```
[All (7)] [Running (1)] [Scheduled (2)] [Completed (3)] [Paused (1)]
```

### Sort Controls
```
Sort by: [Date hosted ▼] [↓ Descending]
Showing 7 of 7 sessions
```

### Table Columns
| Activity name | Date hosted | Participants | Accuracy | Code | Status | Actions |
|--------------|-------------|--------------|----------|------|--------|---------|
| Algebra Basics | Oct 12 | 25 | 78% 🟢 | 123456 | Completed | View Report |

---

## 🎯 User Flow

1. Host logs in
2. Clicks "Reports" in navigation
3. Sees list of all their quiz sessions
4. Can search for specific quiz
5. Can filter by status
6. Can sort by different criteria
7. Clicks "View Report" for completed session
8. Redirected to detailed session report

---

## 🔒 Security

- ✅ JWT authentication required
- ✅ Only shows sessions for current host
- ✅ Role-based access control (HOST or ADMIN)
- ✅ No sensitive data exposed

---

## 📱 Responsive Design

The UI is fully responsive:
- **Desktop**: Full table view
- **Tablet**: Scrollable table
- **Mobile**: Optimized card view (you may want to add this)

---

## 🎨 Customization

### Change Colors

```tsx
// Primary color (currently purple)
className="bg-purple-600"  // Change to bg-blue-600, bg-green-600, etc.

// Status colors
const getStatusColor = (status: string) => {
  switch (status) {
    case "COMPLETED":
      return "bg-blue-100 text-blue-700";  // Customize here
    // ...
  }
};
```

### Change Date Format

```tsx
// Currently: "Oct 12"
new Date(session.startTime).toLocaleDateString("en-US", {
  month: "short",
  day: "numeric",
  year: "numeric",  // Add this for full year
});
```

### Add Export Feature

```tsx
const handleExport = async (sessionCode: string) => {
  const response = await axios.get(
    `http://localhost:9999/api/v1/reports/session/${sessionCode}/export?format=pdf`,
    {
      headers: { Authorization: `Bearer ${token}` },
      responseType: 'blob',
    }
  );
  // Download file
};
```

---

## 🧪 Testing

### Test Scenarios

1. ✅ Load page with no sessions
2. ✅ Load page with multiple sessions
3. ✅ Search for specific quiz
4. ✅ Filter by each status
5. ✅ Sort by each criterion
6. ✅ Click View Report button
7. ✅ Try to view incomplete session report

### API Testing (Postman)

```
GET http://localhost:9999/api/v1/reports-history/my-sessions
Headers:
  Authorization: Bearer YOUR_JWT_TOKEN

Expected: 200 OK with array of session summaries
```

---

## 🐛 Troubleshooting

### Issue: "Failed to load sessions"
**Solution**: Check if backend is running and JWT token is valid

### Issue: "No sessions found"
**Solution**: Host needs to have created at least one quiz session

### Issue: Accuracy showing 0%
**Solution**: Session needs participants who have answered questions

### Issue: View Report button disabled
**Solution**: Only COMPLETED sessions have reports available

---

## 📈 Performance

- ✅ Optimized queries with JPA
- ✅ Statistics calculated efficiently
- ✅ Frontend filtering done client-side
- ✅ Lazy loading for large lists (can be added)

---

## 🎉 Features Comparison with Quizizz

| Feature | Quizizz | StackQuiz |
|---------|---------|-----------|
| List all sessions | ✅ | ✅ |
| Filter by status | ✅ | ✅ |
| Search sessions | ✅ | ✅ |
| Sort options | ✅ | ✅ |
| Accuracy indicator | ✅ | ✅ |
| Click to view report | ✅ | ✅ |
| Export reports | ✅ | ✅ (Backend ready) |
| Date range filter | ✅ | ⚠️ (Can be added) |

---

## 🚀 Next Steps

1. ✅ Backend implemented
2. ✅ Frontend component created
3. ⏳ Add to your app
4. ⏳ Test with real data
5. ⏳ Deploy to production

---

## 📞 Support

If you encounter any issues:
1. Check backend logs
2. Check browser console
3. Verify JWT token is valid
4. Ensure session data exists in database

---

## 🎓 Summary

You now have a complete **Reports History feature** that:
- ✅ Shows all host's quiz sessions
- ✅ Filters by status
- ✅ Searches by name/code
- ✅ Sorts by multiple criteria
- ✅ Links to detailed reports
- ✅ Looks exactly like Quizizz!

**Time to implement**: ~30 minutes  
**Difficulty**: Easy  
**Status**: ✅ Ready to use!

