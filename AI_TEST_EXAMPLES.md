# AI Quiz Generation - Test Examples

This file contains ready-to-use test examples for the AI quiz generation feature.

---

## Test 1: Generate Science Questions

### Request
```bash
curl -X POST http://localhost:9999/api/v1/ai/quiz/generate \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "Photosynthesis",
    "numberOfQuestions": 3,
    "difficulty": "MEDIUM",
    "questionType": "MULTIPLE_CHOICE",
    "numberOfOptions": 4,
    "timeLimit": 30,
    "points": 100,
    "language": "English",
    "additionalContext": "Focus on the light-dependent reactions",
    "includeExplanations": true
  }'
```

### Expected Output
- 3 multiple-choice questions about photosynthesis
- Each with 4 options
- Focused on light-dependent reactions
- Includes explanations for correct answers

---

## Test 2: Generate Programming Questions

### Request
```bash
curl -X POST http://localhost:9999/api/v1/ai/quiz/generate \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "Python Data Structures",
    "numberOfQuestions": 5,
    "difficulty": "HARD",
    "questionType": "MULTIPLE_CHOICE",
    "numberOfOptions": 4,
    "timeLimit": 45,
    "points": 150,
    "language": "English",
    "additionalContext": "Focus on lists, dictionaries, and sets. Include time complexity questions.",
    "includeExplanations": true
  }'
```

---

## Test 3: Generate True/False Questions

### Request
```bash
curl -X POST http://localhost:9999/api/v1/ai/quiz/generate \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "World History - Ancient Rome",
    "numberOfQuestions": 10,
    "difficulty": "EASY",
    "questionType": "TRUE_FALSE",
    "numberOfOptions": 2,
    "timeLimit": 15,
    "points": 50,
    "language": "English",
    "includeExplanations": false
  }'
```

---

## Test 4: Generate Single Question

### Request
```bash
curl -X POST "http://localhost:9999/api/v1/ai/quiz/generate/single?topic=Machine%20Learning&difficulty=HARD&questionType=MULTIPLE_CHOICE"
```

---

## Test 5: Add AI Questions to Existing Quiz

### Prerequisites
- You need a valid quiz ID
- You need authentication token (if security is enabled)

### Request
```bash
curl -X POST http://localhost:9999/api/v1/quiz/YOUR_QUIZ_ID/ai/generate-questions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "topic": "JavaScript ES6 Features",
    "numberOfQuestions": 5,
    "difficulty": "MEDIUM",
    "questionType": "MULTIPLE_CHOICE",
    "numberOfOptions": 4,
    "timeLimit": 30,
    "points": 100,
    "language": "English",
    "additionalContext": "Focus on arrow functions, promises, and async/await",
    "includeExplanations": true
  }'
```

---

## Test 6: Improve Existing Question

### Request
```bash
curl -X POST "http://localhost:9999/api/v1/ai/quiz/improve?questionText=What%20is%20Java?&context=Make%20it%20more%20specific%20and%20technical"
```

### Expected Output
```json
{
  "success": true,
  "message": "Question improved successfully",
  "data": {
    "original": "What is Java?",
    "improved": "Which of the following best describes Java's memory management model and its primary advantage over manual memory management in C++?"
  }
}
```

---

## Test 7: Generate Math Questions

### Request
```bash
curl -X POST http://localhost:9999/api/v1/ai/quiz/generate \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "Calculus - Derivatives",
    "numberOfQuestions": 4,
    "difficulty": "HARD",
    "questionType": "MULTIPLE_CHOICE",
    "numberOfOptions": 4,
    "timeLimit": 60,
    "points": 200,
    "language": "English",
    "additionalContext": "Include chain rule and product rule problems",
    "includeExplanations": true
  }'
```

---

## Test 8: Generate Business Questions

### Request
```bash
curl -X POST http://localhost:9999/api/v1/ai/quiz/generate \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "Marketing Strategy - Digital Marketing",
    "numberOfQuestions": 6,
    "difficulty": "MEDIUM",
    "questionType": "MULTIPLE_CHOICE",
    "numberOfOptions": 4,
    "timeLimit": 30,
    "points": 100,
    "language": "English",
    "additionalContext": "Focus on SEO, SEM, and social media marketing",
    "includeExplanations": true
  }'
```

---

## Test 9: Health Check

### Request
```bash
curl -X GET http://localhost:9999/api/v1/ai/quiz/health
```

### Expected Output
```json
{
  "service": "AI Quiz Generation",
  "status": "operational",
  "timestamp": 1697040000000
}
```

---

## Test 10: Generate Questions in Different Language

### Request
```bash
curl -X POST http://localhost:9999/api/v1/ai/quiz/generate \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "French Revolution",
    "numberOfQuestions": 3,
    "difficulty": "MEDIUM",
    "questionType": "MULTIPLE_CHOICE",
    "numberOfOptions": 4,
    "timeLimit": 30,
    "points": 100,
    "language": "French",
    "includeExplanations": true
  }'
```

---

## Postman Collection

You can import these tests into Postman using this collection:

```json
{
  "info": {
    "name": "StackQuiz AI Generation",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Generate Questions",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"topic\": \"Photosynthesis\",\n  \"numberOfQuestions\": 3,\n  \"difficulty\": \"MEDIUM\",\n  \"questionType\": \"MULTIPLE_CHOICE\",\n  \"numberOfOptions\": 4,\n  \"timeLimit\": 30,\n  \"points\": 100,\n  \"language\": \"English\",\n  \"includeExplanations\": true\n}"
        },
        "url": {
          "raw": "http://localhost:9999/api/v1/ai/quiz/generate",
          "protocol": "http",
          "host": ["localhost"],
          "port": "9999",
          "path": ["api", "v1", "ai", "quiz", "generate"]
        }
      }
    },
    {
      "name": "Generate Single Question",
      "request": {
        "method": "POST",
        "url": {
          "raw": "http://localhost:9999/api/v1/ai/quiz/generate/single?topic=Python&difficulty=MEDIUM&questionType=MULTIPLE_CHOICE",
          "protocol": "http",
          "host": ["localhost"],
          "port": "9999",
          "path": ["api", "v1", "ai", "quiz", "generate", "single"],
          "query": [
            {"key": "topic", "value": "Python"},
            {"key": "difficulty", "value": "MEDIUM"},
            {"key": "questionType", "value": "MULTIPLE_CHOICE"}
          ]
        }
      }
    },
    {
      "name": "Health Check",
      "request": {
        "method": "GET",
        "url": {
          "raw": "http://localhost:9999/api/v1/ai/quiz/health",
          "protocol": "http",
          "host": ["localhost"],
          "port": "9999",
          "path": ["api", "v1", "ai", "quiz", "health"]
        }
      }
    }
  ]
}
```

---

## Integration Testing Script

Create a bash script to test all endpoints:

```bash
#!/bin/bash

BASE_URL="http://localhost:9999"

echo "Testing AI Quiz Generation Endpoints..."
echo "========================================"

# Test 1: Health Check
echo -e "\n1. Health Check"
curl -s -X GET "$BASE_URL/api/v1/ai/quiz/health" | jq '.'

# Test 2: Generate Questions
echo -e "\n2. Generate Questions"
curl -s -X POST "$BASE_URL/api/v1/ai/quiz/generate" \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "Solar System",
    "numberOfQuestions": 2,
    "difficulty": "EASY",
    "questionType": "MULTIPLE_CHOICE",
    "numberOfOptions": 4,
    "timeLimit": 20,
    "points": 50,
    "language": "English",
    "includeExplanations": true
  }' | jq '.'

# Test 3: Generate Single Question
echo -e "\n3. Generate Single Question"
curl -s -X POST "$BASE_URL/api/v1/ai/quiz/generate/single?topic=Chemistry&difficulty=MEDIUM&questionType=MULTIPLE_CHOICE" | jq '.'

echo -e "\n========================================"
echo "Tests completed!"
```

Save as `test-ai-endpoints.sh` and run with:
```bash
chmod +x test-ai-endpoints.sh
./test-ai-endpoints.sh
```

---

## Notes

1. **API Key Required**: Make sure `OPENAI_API_KEY` environment variable is set before running tests
2. **Server Running**: Ensure the backend server is running on port 9999
3. **Authentication**: Some endpoints may require authentication tokens depending on your security configuration
4. **Rate Limits**: Be mindful of OpenAI API rate limits when testing
5. **Cost**: Each API call incurs costs based on your OpenAI pricing plan

