# AI Question Generation - Postman Guide

## 🚀 Quick Start

### Prerequisites

1. ✅ Backend server running on `http://localhost:9999`
2. ✅ OpenAI API key set as environment variable
3. ✅ Postman installed

---

## 📋 Step-by-Step Guide

### Step 1: Start the Server

```bash
cd backend-stackquiz
export OPENAI_API_KEY=your-openai-api-key-here
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
./gradlew bootRun
```

Wait for:
```
Started StackquizApiApplication in X.XXX seconds
```

---

## 🎯 API Endpoints

### 1. Generate Multiple Questions (Preview Mode)

**Endpoint:** `POST /api/v1/ai/quiz/generate`

**Purpose:** Generate questions without saving to database (for preview)

#### Postman Setup

**Method:** `POST`  
**URL:** `http://localhost:9999/api/v1/ai/quiz/generate`  
**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "topic": "Solar System",
  "numberOfQuestions": 3,
  "difficulty": "MEDIUM",
  "questionType": "MULTIPLE_CHOICE",
  "numberOfOptions": 4,
  "timeLimit": 30,
  "points": 100,
  "language": "English",
  "includeExplanations": true,
  "additionalContext": "Focus on planets and their characteristics"
}
```

#### Expected Response

```json
{
  "success": true,
  "message": "Generated 3 questions successfully",
  "questions": [
    {
      "questionText": "Which planet is known as the Red Planet?",
      "questionType": "MULTIPLE_CHOICE",
      "difficulty": "MEDIUM",
      "timeLimit": 30,
      "points": 100,
      "options": [
        {
          "optionText": "Mars",
          "isCorrect": true
        },
        {
          "optionText": "Venus",
          "isCorrect": false
        },
        {
          "optionText": "Jupiter",
          "isCorrect": false
        },
        {
          "optionText": "Saturn",
          "isCorrect": false
        }
      ],
      "explanation": "Mars is called the Red Planet because of iron oxide (rust) on its surface, giving it a reddish appearance."
    },
    {
      "questionText": "Which planet is the largest in our solar system?",
      "questionType": "MULTIPLE_CHOICE",
      "difficulty": "MEDIUM",
      "timeLimit": 30,
      "points": 100,
      "options": [
        {
          "optionText": "Jupiter",
          "isCorrect": true
        },
        {
          "optionText": "Saturn",
          "isCorrect": false
        },
        {
          "optionText": "Neptune",
          "isCorrect": false
        },
        {
          "optionText": "Uranus",
          "isCorrect": false
        }
      ],
      "explanation": "Jupiter is the largest planet in our solar system, with a mass more than twice that of all other planets combined."
    },
    {
      "questionText": "How many planets are in our solar system?",
      "questionType": "MULTIPLE_CHOICE",
      "difficulty": "MEDIUM",
      "timeLimit": 30,
      "points": 100,
      "options": [
        {
          "optionText": "8",
          "isCorrect": true
        },
        {
          "optionText": "7",
          "isCorrect": false
        },
        {
          "optionText": "9",
          "isCorrect": false
        },
        {
          "optionText": "10",
          "isCorrect": false
        }
      ],
      "explanation": "There are 8 planets in our solar system since Pluto was reclassified as a dwarf planet in 2006."
    }
  ],
  "generatedAt": "2025-10-11T11:50:00.000Z"
}
```

---

### 2. Generate Single Question (Quick Preview)

**Endpoint:** `POST /api/v1/ai/quiz/generate/single`

**Purpose:** Generate one question quickly

#### Postman Setup

**Method:** `POST`  
**URL:** `http://localhost:9999/api/v1/ai/quiz/generate/single?topic=Python Programming&difficulty=EASY`  
**Headers:**
```
Content-Type: application/json
```

**Query Parameters:**
- `topic`: Python Programming
- `difficulty`: EASY (optional)
- `questionType`: MULTIPLE_CHOICE (optional)
- `numberOfOptions`: 4 (optional)

**Body:** None (uses query parameters)

#### Expected Response

```json
{
  "success": true,
  "message": "Generated 1 question successfully",
  "questions": [
    {
      "questionText": "What keyword is used to define a function in Python?",
      "questionType": "MULTIPLE_CHOICE",
      "difficulty": "EASY",
      "timeLimit": 30,
      "points": 100,
      "options": [
        {
          "optionText": "def",
          "isCorrect": true
        },
        {
          "optionText": "function",
          "isCorrect": false
        },
        {
          "optionText": "define",
          "isCorrect": false
        },
        {
          "optionText": "func",
          "isCorrect": false
        }
      ],
      "explanation": "In Python, the 'def' keyword is used to define a function."
    }
  ],
  "generatedAt": "2025-10-11T11:50:00.000Z"
}
```

---

### 3. Improve Existing Question

**Endpoint:** `POST /api/v1/ai/quiz/improve`

**Purpose:** Use AI to refine and improve a question

#### Postman Setup

**Method:** `POST`  
**URL:** `http://localhost:9999/api/v1/ai/quiz/improve?questionText=What is Python?`  
**Headers:**
```
Content-Type: application/json
```

**Query Parameters:**
- `questionText`: What is Python?

**Body:** None

#### Expected Response

```json
{
  "success": true,
  "message": "Question improved successfully",
  "questions": [
    {
      "questionText": "Which of the following best describes Python's primary design philosophy and its key advantage in rapid application development?",
      "questionType": "MULTIPLE_CHOICE",
      "difficulty": "MEDIUM",
      "timeLimit": 30,
      "points": 100,
      "options": [
        {
          "optionText": "A high-level, interpreted programming language emphasizing code readability and simplicity",
          "isCorrect": true
        },
        {
          "optionText": "A low-level compiled language focused on system programming",
          "isCorrect": false
        },
        {
          "optionText": "A markup language for web development",
          "isCorrect": false
        },
        {
          "optionText": "A database management system",
          "isCorrect": false
        }
      ],
      "explanation": "Python is a high-level, interpreted language known for its clear syntax and readability, making it ideal for rapid development."
    }
  ],
  "generatedAt": "2025-10-11T11:50:00.000Z"
}
```

---

### 4. Health Check

**Endpoint:** `GET /api/v1/ai/quiz/health`

**Purpose:** Check if AI service is working

#### Postman Setup

**Method:** `GET`  
**URL:** `http://localhost:9999/api/v1/ai/quiz/health`  
**Headers:** None

#### Expected Response

```json
{
  "status": "healthy",
  "service": "AI Quiz Generation",
  "timestamp": "2025-10-11T11:50:00.000Z",
  "apiConfigured": true
}
```

---

### 5. Generate and Save to Quiz (Database Integration)

**Endpoint:** `POST /api/v1/quiz/{quizId}/ai/generate-questions`

**Purpose:** Generate questions and automatically add them to a quiz

#### Postman Setup

**Method:** `POST`  
**URL:** `http://localhost:9999/api/v1/quiz/quiz-123/ai/generate-questions`  
**Headers:**
```
Content-Type: application/json
Authorization: Bearer YOUR_JWT_TOKEN
```

**Body (raw JSON):**
```json
{
  "topic": "JavaScript ES6 Features",
  "numberOfQuestions": 5,
  "difficulty": "MEDIUM",
  "questionType": "MULTIPLE_CHOICE",
  "numberOfOptions": 4,
  "timeLimit": 30,
  "points": 100,
  "language": "English",
  "includeExplanations": true,
  "additionalContext": "Focus on arrow functions, promises, and async/await"
}
```

#### Expected Response

```json
{
  "success": true,
  "message": "Successfully generated and added 5 questions to quiz",
  "quizId": "quiz-123",
  "questionsAdded": 5,
  "questions": [
    {
      "id": "q-001",
      "questionText": "What is the syntax for an arrow function in JavaScript?",
      "questionType": "MULTIPLE_CHOICE",
      "difficulty": "MEDIUM",
      "timeLimit": 30,
      "points": 100,
      "options": [
        {
          "id": "opt-001",
          "optionText": "() => {}",
          "isCorrect": true
        },
        {
          "id": "opt-002",
          "optionText": "function() {}",
          "isCorrect": false
        },
        {
          "id": "opt-003",
          "optionText": "=> () {}",
          "isCorrect": false
        },
        {
          "id": "opt-004",
          "optionText": "-> () {}",
          "isCorrect": false
        }
      ],
      "explanation": "Arrow functions use the => syntax: (parameters) => { statements }"
    }
    // ... 4 more questions
  ]
}
```

---

### 6. Generate Single Question and Add to Quiz

**Endpoint:** `POST /api/v1/quiz/{quizId}/ai/generate-single`

**Purpose:** Generate one question and add to quiz

#### Postman Setup

**Method:** `POST`  
**URL:** `http://localhost:9999/api/v1/quiz/quiz-123/ai/generate-single?topic=React Hooks&difficulty=HARD`  
**Headers:**
```
Content-Type: application/json
Authorization: Bearer YOUR_JWT_TOKEN
```

**Query Parameters:**
- `topic`: React Hooks
- `difficulty`: HARD

#### Expected Response

```json
{
  "success": true,
  "message": "Successfully generated and added 1 question to quiz",
  "quizId": "quiz-123",
  "questionsAdded": 1,
  "questions": [
    {
      "id": "q-002",
      "questionText": "What is the purpose of useEffect's dependency array?",
      "questionType": "MULTIPLE_CHOICE",
      "difficulty": "HARD",
      "timeLimit": 30,
      "points": 100,
      "options": [
        {
          "id": "opt-005",
          "optionText": "Controls when the effect runs based on value changes",
          "isCorrect": true
        },
        {
          "id": "opt-006",
          "optionText": "Stores component state",
          "isCorrect": false
        },
        {
          "id": "opt-007",
          "optionText": "Defines component props",
          "isCorrect": false
        },
        {
          "id": "opt-008",
          "optionText": "Manages component lifecycle",
          "isCorrect": false
        }
      ],
      "explanation": "The dependency array tells React when to re-run the effect based on which values have changed."
    }
  ]
}
```

---

## 📝 Request Parameters Explained

### Required Parameters

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `topic` | String | Subject matter for questions | "Solar System" |
| `numberOfQuestions` | Integer | How many questions to generate | 5 |

### Optional Parameters

| Parameter | Type | Default | Description | Example |
|-----------|------|---------|-------------|---------|
| `difficulty` | String | MEDIUM | Question difficulty | "EASY", "MEDIUM", "HARD" |
| `questionType` | String | MULTIPLE_CHOICE | Type of question | "MULTIPLE_CHOICE", "TRUE_FALSE", "FILL_IN_BLANK" |
| `numberOfOptions` | Integer | 4 | Options per question | 4 |
| `timeLimit` | Integer | 30 | Time limit in seconds | 30 |
| `points` | Integer | 100 | Points per question | 100 |
| `language` | String | English | Language for questions | "English", "Spanish", "French" |
| `includeExplanations` | Boolean | true | Include answer explanations | true |
| `additionalContext` | String | null | Extra context for AI | "Focus on basics" |

---

## 🎨 Example Requests

### Example 1: Science Quiz

```json
{
  "topic": "Photosynthesis",
  "numberOfQuestions": 5,
  "difficulty": "MEDIUM",
  "questionType": "MULTIPLE_CHOICE",
  "numberOfOptions": 4,
  "timeLimit": 45,
  "points": 150,
  "language": "English",
  "includeExplanations": true,
  "additionalContext": "Focus on the process, inputs, and outputs of photosynthesis"
}
```

### Example 2: Programming Quiz

```json
{
  "topic": "Python Data Structures",
  "numberOfQuestions": 10,
  "difficulty": "HARD",
  "questionType": "MULTIPLE_CHOICE",
  "numberOfOptions": 4,
  "timeLimit": 60,
  "points": 200,
  "language": "English",
  "includeExplanations": true,
  "additionalContext": "Cover lists, dictionaries, sets, and tuples with practical examples"
}
```

### Example 3: History Quiz

```json
{
  "topic": "World War II",
  "numberOfQuestions": 8,
  "difficulty": "MEDIUM",
  "questionType": "MULTIPLE_CHOICE",
  "numberOfOptions": 4,
  "timeLimit": 40,
  "points": 120,
  "language": "English",
  "includeExplanations": true,
  "additionalContext": "Focus on major events, key figures, and turning points"
}
```

### Example 4: True/False Questions

```json
{
  "topic": "Basic Geography",
  "numberOfQuestions": 10,
  "difficulty": "EASY",
  "questionType": "TRUE_FALSE",
  "numberOfOptions": 2,
  "timeLimit": 20,
  "points": 50,
  "language": "English",
  "includeExplanations": true
}
```

### Example 5: Multi-Language Quiz

```json
{
  "topic": "Historia de España",
  "numberOfQuestions": 5,
  "difficulty": "MEDIUM",
  "questionType": "MULTIPLE_CHOICE",
  "numberOfOptions": 4,
  "timeLimit": 30,
  "points": 100,
  "language": "Spanish",
  "includeExplanations": true,
  "additionalContext": "Enfócate en la Guerra Civil Española"
}
```

---

## 🔧 Troubleshooting

### Error: "API key not configured"

**Solution:**
```bash
export OPENAI_API_KEY=your-actual-api-key-here
./gradlew bootRun
```

### Error: "Connection refused"

**Solution:** Make sure server is running
```bash
curl http://localhost:9999/api/v1/ai/quiz/health
```

### Error: "Failed to generate questions"

**Possible causes:**
1. Invalid OpenAI API key
2. No internet connection
3. OpenAI API rate limit exceeded
4. Invalid request parameters

**Check logs:**
```bash
# Look for error messages in server logs
tail -f logs/application.log
```

### Error: 401 Unauthorized (for quiz endpoints)

**Solution:** Add authentication token
```
Authorization: Bearer YOUR_JWT_TOKEN
```

---

## 📊 Response Times

| Operation | Expected Time |
|-----------|---------------|
| 1 question | 2-4 seconds |
| 5 questions | 5-10 seconds |
| 10 questions | 10-20 seconds |
| 20 questions | 20-40 seconds |

---

## 💰 Cost Estimation

Using OpenAI's pricing (approximate):

| Questions | Tokens | Cost |
|-----------|--------|------|
| 1 question | ~200 | $0.0004 |
| 5 questions | ~1000 | $0.002 |
| 10 questions | ~2000 | $0.004 |
| 100 questions | ~20000 | $0.04 |

---

## 🎯 Best Practices

### 1. Be Specific with Topics

✅ **Good:** "Quadratic equations and the quadratic formula"  
❌ **Poor:** "Math"

### 2. Use Additional Context

```json
{
  "topic": "JavaScript",
  "additionalContext": "Focus on ES6 features like arrow functions, promises, and async/await. Avoid basic syntax."
}
```

### 3. Start Small

Generate 1-3 questions first to preview quality, then generate more.

### 4. Review Before Using

Always review AI-generated questions before adding to live quizzes.

### 5. Optimal Batch Size

Generate 5-10 questions at a time for best quality and speed.

---

## 📚 Postman Collection

### Import This Collection

Create a new collection in Postman and add these requests:

**Collection Name:** StackQuiz AI Generation

**Requests:**
1. Generate Questions (Preview)
2. Generate Single Question
3. Improve Question
4. Health Check
5. Generate and Save to Quiz
6. Generate Single and Save

**Variables:**
- `baseUrl`: http://localhost:9999
- `quizId`: quiz-123 (replace with actual quiz ID)

---

## 🔐 Authentication

### For Preview Endpoints (No Auth Required)
- `/api/v1/ai/quiz/generate`
- `/api/v1/ai/quiz/generate/single`
- `/api/v1/ai/quiz/improve`
- `/api/v1/ai/quiz/health`

### For Quiz Integration Endpoints (Auth Required)
- `/api/v1/quiz/{quizId}/ai/generate-questions`
- `/api/v1/quiz/{quizId}/ai/generate-single`

**Add to Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN
```

---

## 🎓 Complete Workflow Example

### Step 1: Check Health
```
GET http://localhost:9999/api/v1/ai/quiz/health
```

### Step 2: Preview Questions
```
POST http://localhost:9999/api/v1/ai/quiz/generate
Body: { "topic": "Solar System", "numberOfQuestions": 3 }
```

### Step 3: Review Generated Questions
Check the response, verify quality.

### Step 4: Create Quiz (if needed)
```
POST http://localhost:9999/api/v1/quizzes
Body: { "title": "Solar System Quiz", ... }
```

### Step 5: Add AI Questions to Quiz
```
POST http://localhost:9999/api/v1/quiz/quiz-123/ai/generate-questions
Body: { "topic": "Solar System", "numberOfQuestions": 10 }
```

### Step 6: Verify Questions Added
```
GET http://localhost:9999/api/v1/quizzes/quiz-123
```

---

## ✅ Quick Test Checklist

- [ ] Server running on port 9999
- [ ] OpenAI API key configured
- [ ] Health check returns "healthy"
- [ ] Can generate 1 question
- [ ] Can generate multiple questions
- [ ] Can improve a question
- [ ] Can add questions to quiz (with auth)
- [ ] Response times acceptable
- [ ] Questions are high quality

---

## 📞 Support

If you encounter issues:

1. Check server logs
2. Verify OpenAI API key
3. Test health endpoint
4. Review request format
5. Check authentication (for quiz endpoints)

---

**Status**: ✅ **READY TO USE**

**Base URL**: `http://localhost:9999`

**API Version**: `v1`

