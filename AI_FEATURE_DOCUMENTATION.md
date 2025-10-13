# AI Quiz Generation Feature - Documentation

## Overview

The AI Quiz Generation feature allows users to automatically generate quiz questions and options using AI (powered by OpenAI), similar to Quizizz and Kahoot's AI features. This feature significantly reduces the time needed to create engaging quiz content.

---

## Features

### 1. **Bulk Question Generation**
Generate multiple quiz questions at once based on a topic and parameters.

### 2. **Single Question Generation**
Generate one question at a time for quick additions.

### 3. **Direct Quiz Integration**
AI-generated questions can be automatically saved to your quiz database.

### 4. **Question Improvement**
Use AI to refine and improve existing questions.

### 5. **Customizable Parameters**
- Topic/subject
- Difficulty level (EASY, MEDIUM, HARD)
- Question type (MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER)
- Number of options
- Time limit
- Points
- Language
- Include explanations

---

## API Endpoints

### 1. Generate Questions (Preview Only)

**Endpoint**: `POST /api/v1/ai/quiz/generate`

**Description**: Generate quiz questions using AI without saving to database (preview mode).

**Request Body**:
```json
{
  "topic": "World War II",
  "numberOfQuestions": 5,
  "difficulty": "MEDIUM",
  "questionType": "MULTIPLE_CHOICE",
  "numberOfOptions": 4,
  "timeLimit": 30,
  "points": 100,
  "language": "English",
  "additionalContext": "Focus on major battles and key dates",
  "includeExplanations": true
}
```

**Response**:
```json
{
  "success": true,
  "message": "Questions generated successfully",
  "data": {
    "topic": "World War II",
    "difficulty": "MEDIUM",
    "totalQuestions": 5,
    "generatedAt": "2025-10-11T12:00:00",
    "questions": [
      {
        "questionText": "Which battle is considered the turning point of WWII in Europe?",
        "questionType": "MULTIPLE_CHOICE",
        "timeLimit": 30,
        "points": 100,
        "options": [
          {
            "optionText": "Battle of Stalingrad",
            "isCorrect": true,
            "optionOrder": 1
          },
          {
            "optionText": "Battle of Britain",
            "isCorrect": false,
            "optionOrder": 2
          },
          {
            "optionText": "Battle of Midway",
            "isCorrect": false,
            "optionOrder": 3
          },
          {
            "optionText": "D-Day Invasion",
            "isCorrect": false,
            "optionOrder": 4
          }
        ],
        "explanation": "The Battle of Stalingrad (1942-1943) marked the first major defeat of Nazi Germany and shifted momentum to the Allies."
      }
    ]
  }
}
```

---

### 2. Generate Single Question

**Endpoint**: `POST /api/v1/ai/quiz/generate/single`

**Query Parameters**:
- `topic` (required): The topic for the question
- `difficulty` (optional, default: MEDIUM): EASY, MEDIUM, or HARD
- `questionType` (optional, default: MULTIPLE_CHOICE): Question type

**Example**:
```
POST /api/v1/ai/quiz/generate/single?topic=Python Programming&difficulty=HARD&questionType=MULTIPLE_CHOICE
```

---

### 3. Generate and Add to Quiz

**Endpoint**: `POST /api/v1/quiz/{quizId}/ai/generate-questions`

**Description**: Generate questions and automatically add them to the specified quiz.

**Path Parameter**:
- `quizId`: The ID of the quiz to add questions to

**Request Body**: Same as endpoint #1

**Response**:
```json
{
  "success": true,
  "message": "5 questions generated and added successfully",
  "data": {
    "quizId": "abc123",
    "questionsAdded": 5,
    "questions": [...]
  }
}
```

---

### 4. Generate and Add Single Question to Quiz

**Endpoint**: `POST /api/v1/quiz/{quizId}/ai/generate-single`

**Query Parameters**:
- `topic` (required)
- `difficulty` (optional, default: MEDIUM)
- `questionType` (optional, default: MULTIPLE_CHOICE)

**Example**:
```
POST /api/v1/quiz/abc123/ai/generate-single?topic=JavaScript&difficulty=EASY
```

---

### 5. Improve Existing Question

**Endpoint**: `POST /api/v1/ai/quiz/improve`

**Query Parameters**:
- `questionText` (required): The original question text
- `context` (optional): Additional requirements or context

**Example**:
```
POST /api/v1/ai/quiz/improve?questionText=What is Python?&context=Make it more specific and challenging
```

**Response**:
```json
{
  "success": true,
  "message": "Question improved successfully",
  "data": {
    "original": "What is Python?",
    "improved": "Which of the following best describes Python's primary design philosophy?"
  }
}
```

---

### 6. Health Check

**Endpoint**: `GET /api/v1/ai/quiz/health`

**Description**: Check if the AI service is operational.

---

## Configuration

### Environment Variables

Set the OpenAI API key as an environment variable:

```bash
export OPENAI_API_KEY=your-actual-api-key-here
```

### Application Configuration

The feature is configured in `application.yml`:

```yaml
openai:
  api:
    key: ${OPENAI_API_KEY:your-api-key-here}
    model: gpt-4.1-mini
    temperature: 0.7
    max-tokens: 2000
```

---

## Question Types Mapping

The system supports the following question types:

| User Input | Database Enum | Description |
|------------|---------------|-------------|
| MULTIPLE_CHOICE, MCQ | MCQ | Multiple choice questions with 2-6 options |
| TRUE_FALSE, TF | TF | True/False questions |
| FILL_THE_BLANK, SHORT_ANSWER | FILL_THE_BLANK | Fill in the blank questions |

---

## Usage Examples

### Example 1: Generate Math Questions

```bash
curl -X POST http://localhost:9999/api/v1/ai/quiz/generate \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "Algebra - Quadratic Equations",
    "numberOfQuestions": 3,
    "difficulty": "HARD",
    "questionType": "MULTIPLE_CHOICE",
    "numberOfOptions": 4,
    "timeLimit": 45,
    "points": 150,
    "language": "English",
    "includeExplanations": true
  }'
```

### Example 2: Add AI Questions to Existing Quiz

```bash
curl -X POST http://localhost:9999/api/v1/quiz/quiz-id-123/ai/generate-questions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "topic": "Biology - Cell Structure",
    "numberOfQuestions": 10,
    "difficulty": "MEDIUM",
    "questionType": "MULTIPLE_CHOICE",
    "numberOfOptions": 4,
    "timeLimit": 30,
    "points": 100,
    "language": "English",
    "includeExplanations": true
  }'
```

### Example 3: Generate True/False Questions

```bash
curl -X POST http://localhost:9999/api/v1/ai/quiz/generate \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "World Geography",
    "numberOfQuestions": 5,
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

## Best Practices

### 1. Topic Specificity
Be specific with your topics for better question quality:
- ✅ Good: "Photosynthesis in C3 plants"
- ❌ Poor: "Science"

### 2. Batch Size
Generate 5-10 questions at a time for optimal quality and speed.

### 3. Additional Context
Use the `additionalContext` field to guide question generation:
```json
{
  "topic": "JavaScript",
  "additionalContext": "Focus on ES6 features, avoid basic syntax questions"
}
```

### 4. Review Generated Questions
Always review AI-generated questions before using them in live quizzes.

### 5. Difficulty Calibration
- **EASY**: Basic recall and understanding
- **MEDIUM**: Application and analysis
- **HARD**: Synthesis and evaluation

---

## Error Handling

### Common Errors

**1. API Key Not Set**
```json
{
  "success": false,
  "message": "Failed to generate questions: API key not configured"
}
```
**Solution**: Set the `OPENAI_API_KEY` environment variable.

**2. Invalid Question Type**
```json
{
  "success": false,
  "message": "Question type is required"
}
```
**Solution**: Use valid question types: MULTIPLE_CHOICE, TRUE_FALSE, or FILL_THE_BLANK.

**3. Quiz Not Found**
```json
{
  "success": false,
  "message": "Failed to generate and add questions: Quiz not found: xyz"
}
```
**Solution**: Verify the quiz ID exists in the database.

---

## Limitations

1. **Rate Limits**: Subject to OpenAI API rate limits
2. **Cost**: Each generation consumes API credits
3. **Quality**: AI-generated questions should be reviewed before use
4. **Language**: Best results in English; other languages may vary
5. **Maximum Questions**: Limited to 20 questions per request for performance

---

## Future Enhancements

Potential improvements for future versions:

- [ ] Image generation for questions
- [ ] Bulk import from documents/PDFs
- [ ] Question difficulty auto-calibration
- [ ] Multi-language support optimization
- [ ] Question bank management
- [ ] Duplicate detection
- [ ] Custom prompt templates
- [ ] Analytics on question performance

---

## Support

For issues or questions about the AI feature:
1. Check the logs for detailed error messages
2. Verify API key configuration
3. Review the API documentation
4. Contact support at https://help.manus.im

---

## License

This feature is part of the StackQuiz backend application.

