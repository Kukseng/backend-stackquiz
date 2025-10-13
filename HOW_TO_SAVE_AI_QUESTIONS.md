# How to Save AI-Generated Questions to Database

## 🔍 The Problem

You're using the **preview endpoint** which generates questions but **doesn't save them** to the database.

---

## ✅ Solution: Use the Quiz Integration Endpoint

There are **two types of endpoints**:

### 1. Preview Endpoints (NO Database Save)
These generate questions for preview only:
- ❌ `POST /api/v1/ai/quiz/generate`
- ❌ `POST /api/v1/ai/quiz/generate/single`
- ❌ `POST /api/v1/ai/quiz/improve`

### 2. Quiz Integration Endpoints (SAVES to Database)
These generate AND save to database:
- ✅ `POST /api/v1/quiz/{quizId}/ai/generate-questions`
- ✅ `POST /api/v1/quiz/{quizId}/ai/generate-single`

---

## 🚀 Step-by-Step: Save to Database

### Step 1: Get a Quiz ID

First, you need an existing quiz. Create one or get the ID of an existing quiz.

**Create Quiz (if needed):**
```
POST http://localhost:9999/api/v1/quizzes
Headers:
  Content-Type: application/json
  Authorization: Bearer YOUR_JWT_TOKEN

Body:
{
  "title": "My AI Quiz",
  "description": "Quiz with AI-generated questions",
  "mode": "ASYNC",
  "isPublic": true
}
```

**Response:**
```json
{
  "id": "abc123xyz",  ← This is your quizId
  "title": "My AI Quiz",
  ...
}
```

---

### Step 2: Generate and Save Questions

**Use the correct endpoint:**
```
POST http://localhost:9999/api/v1/quiz/abc123xyz/ai/generate-questions
                                              ↑
                                         Your Quiz ID
```

**Full Postman Setup:**

**Method:** `POST`  
**URL:** `http://localhost:9999/api/v1/quiz/abc123xyz/ai/generate-questions`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer YOUR_JWT_TOKEN
```

**Body (raw JSON):**
```json
{
  "topic": "Solar System",
  "numberOfQuestions": 5,
  "difficulty": "MEDIUM",
  "questionType": "MULTIPLE_CHOICE",
  "numberOfOptions": 4,
  "timeLimit": 30,
  "points": 100,
  "language": "English",
  "includeExplanations": true,
  "additionalContext": "Focus on planets"
}
```

**Click Send** → Questions will be generated AND saved to database!

---

### Step 3: Verify Questions Were Saved

**Check the quiz:**
```
GET http://localhost:9999/api/v1/quizzes/abc123xyz
Headers:
  Authorization: Bearer YOUR_JWT_TOKEN
```

**Response will include the questions:**
```json
{
  "id": "abc123xyz",
  "title": "My AI Quiz",
  "questions": [
    {
      "id": "q-001",
      "questionText": "Which planet is known as the Red Planet?",
      "options": [...]
    },
    {
      "id": "q-002",
      "questionText": "Which planet is the largest?",
      "options": [...]
    }
    // ... 3 more questions
  ]
}
```

---

## 🔑 Key Differences

### Preview Endpoint (No Save)
```
POST /api/v1/ai/quiz/generate
         ↑
    No quiz ID in URL
    
❌ Doesn't save to database
✅ Good for testing/previewing
✅ No authentication required
```

### Integration Endpoint (Saves to DB)
```
POST /api/v1/quiz/{quizId}/ai/generate-questions
                   ↑
              Quiz ID required
              
✅ Saves to database
✅ Questions added to quiz
⚠️ Requires authentication
⚠️ Requires existing quiz
```

---

## 📋 Complete Example

### Example 1: Create Quiz and Add AI Questions

**Step 1: Create Quiz**
```bash
curl -X POST http://localhost:9999/api/v1/quizzes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "title": "Python Quiz",
    "description": "AI-generated Python questions",
    "mode": "ASYNC"
  }'
```

**Response:**
```json
{
  "id": "quiz-python-001",
  ...
}
```

**Step 2: Generate and Save Questions**
```bash
curl -X POST http://localhost:9999/api/v1/quiz/quiz-python-001/ai/generate-questions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "topic": "Python Basics",
    "numberOfQuestions": 10,
    "difficulty": "EASY"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "10 questions generated and added successfully",
  "data": {
    "quizId": "quiz-python-001",
    "questionsAdded": 10,
    "questions": [...]
  }
}
```

**Step 3: Verify**
```bash
curl http://localhost:9999/api/v1/quizzes/quiz-python-001 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 🎯 In Postman

### Method 1: Use Postman Collection

1. Import the collection I provided
2. Find request **"5. Generate and Save to Quiz"**
3. Update the `quizId` variable with your actual quiz ID
4. Update the `Authorization` header with your JWT token
5. Click **Send**

### Method 2: Manual Setup

1. **Create new request**
2. **Method:** POST
3. **URL:** `http://localhost:9999/api/v1/quiz/YOUR_QUIZ_ID/ai/generate-questions`
4. **Headers:**
   - `Content-Type: application/json`
   - `Authorization: Bearer YOUR_JWT_TOKEN`
5. **Body (raw JSON):**
   ```json
   {
     "topic": "Your Topic",
     "numberOfQuestions": 5,
     "difficulty": "MEDIUM"
   }
   ```
6. **Send**

---

## 🔐 Authentication

### Getting JWT Token

You need to authenticate first to get a token:

```
POST http://localhost:9999/api/v1/auth/login
Content-Type: application/json

{
  "email": "your@email.com",
  "password": "yourpassword"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "..."
}
```

Use the `accessToken` in your requests:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

---

## ⚠️ Common Errors

### Error 1: "Quiz not found"
**Cause:** Invalid quiz ID  
**Solution:** Check the quiz ID is correct

### Error 2: "401 Unauthorized"
**Cause:** Missing or invalid JWT token  
**Solution:** Add valid `Authorization: Bearer TOKEN` header

### Error 3: "403 Forbidden"
**Cause:** You don't own this quiz  
**Solution:** Use a quiz you created, or have permission to edit

### Error 4: Questions generated but not saved
**Cause:** Using preview endpoint instead of integration endpoint  
**Solution:** Use `/api/v1/quiz/{quizId}/ai/generate-questions` instead of `/api/v1/ai/quiz/generate`

---

## 📊 Comparison Table

| Feature | Preview Endpoint | Integration Endpoint |
|---------|------------------|----------------------|
| **URL** | `/api/v1/ai/quiz/generate` | `/api/v1/quiz/{quizId}/ai/generate-questions` |
| **Saves to DB** | ❌ No | ✅ Yes |
| **Requires Quiz ID** | ❌ No | ✅ Yes |
| **Requires Auth** | ❌ No | ✅ Yes |
| **Use Case** | Preview/Testing | Production |
| **Response** | Just questions | Questions + saved IDs |

---

## ✅ Quick Checklist

Before using the integration endpoint:

- [ ] Have a valid quiz ID
- [ ] Have a valid JWT token
- [ ] Using correct URL: `/api/v1/quiz/{quizId}/ai/generate-questions`
- [ ] Authorization header included
- [ ] Quiz exists and you have permission

---

## 🎓 Summary

### Wrong Way (Preview Only)
```
POST /api/v1/ai/quiz/generate
❌ Questions NOT saved to database
```

### Right Way (Saves to Database)
```
POST /api/v1/quiz/{quizId}/ai/generate-questions
✅ Questions saved to database
✅ Added to your quiz
✅ Ready to use in quiz sessions
```

---

## 🔧 Testing Flow

### Complete Test Flow

1. **Login** → Get JWT token
2. **Create Quiz** → Get quiz ID
3. **Generate Questions** → Use integration endpoint with quiz ID
4. **Verify** → Check quiz has questions
5. **Start Quiz Session** → Use the quiz with AI questions
6. **Play** → Questions appear in game!

---

## 📞 Still Having Issues?

### Check Server Logs

```bash
# Look for errors
tail -f logs/application.log

# Look for "Generated and added" message
grep "Generated and added" logs/application.log
```

### Check Database

```sql
-- Check if questions were saved
SELECT * FROM questions WHERE quiz_id = 'your-quiz-id';

-- Check if options were saved
SELECT * FROM options WHERE question_id IN (
  SELECT id FROM questions WHERE quiz_id = 'your-quiz-id'
);
```

---

**Status**: ✅ **Ready to Save to Database**

**Correct Endpoint**: `POST /api/v1/quiz/{quizId}/ai/generate-questions`

**Requires**: Quiz ID + JWT Token

