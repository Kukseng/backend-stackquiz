# 🌐 API Documentation

## 📖 Overview

This documentation provides details about all the available endpoints and messaging topics in the API.
It includes endpoints for user management, quiz session management, quiz management, option management, participant answers, session reports, question management, participant management, leaderboard management, category management, authentication, and WebSocket messaging.

---

## 🚀 Getting Started

### ⚡ Starting the Network

Run the following command to start the network:

```bash
just network create
```

When prompted, use the organization name `Org1` for development and testing.

---

## 📍 Endpoints

---

### 👤 User Management

* **\[GET]** `/api/v1/users/me` — Get current user profile 
* **\[PUT]** `/api/v1/users/me` — Update current user 
* **\[GET]** `/api/v1/users` — Get all users 
* **\[DELETE]** `/api/v1/users/{userId}` — Delete user by ID 
* **\[PATCH]** `/api/v1/users/{userId` — Disable user by ID (soft-delete)

---

### 🧠 Quiz Session Management

* **\[PUT]** `/api/v1/sessions/{sessionId}/start` — Start a quiz session
* **\[PUT]** `/api/v1/sessions/{sessionId}/next-question` — Go to next question
* **\[PUT]** `/api/v1/sessions/{sessionId}/end` — End a session
* **\[POST]** `/api/v1/sessions` — Create a session
* **\[GET]** `/api/v1/sessions/{sessionCode}/join` — Join a session
* **\[GET]** `/api/v1/sessions/{quizCode}` — Get session by quiz code
* **\[GET]** `/api/v1/sessions/me` — Get sessions of authenticated user

---

### 📝 Quiz Management

* **\[GET]** `/api/v1/quizzes/{quizId}` — Get a quiz by ID 
* **\[PUT]** `/api/v1/quizzes/{quizId}` — Update an existing quiz
* **\[DELETE]** `/api/v1/quizzes/{quizId}` — Delete a quiz by ID
* **\[GET]** `/api/v1/quizzes` — Get all quizzes (public)
* **\[POST]** `/api/v1/quizzes` — Create a new quiz
* **\[GET]** `/api/v1/quizzes/users/me` — Get quizzes created by the authenticated user

---

### ⚙️ Option Management

* **\[PUT]** `/api/v1/options/{optionId}` — Update an option 
* **\[DELETE]** `/api/v1/options/{optionId}` — Delete an option (secured)
* **\[POST]** `/api/v1/options/questions/{questionId}` — Add options to a question 
* **\[GET]** `/api/v1/options` — Get all options (secured)
* **\[GET]** `/api/v1/options/questions/{questionId}/public` — Get options by questionId 

---

### 📝 Participant Answer Management

* **\[PUT]** `/api/v1/answers/{answerId}` — Update an answer
* **\[POST]** `/api/v1/answers/submit` — Submit an answer
* **\[POST]** `/api/v1/answers/submit/bulk` — Submit multiple answers
* **\[GET]** `/api/v1/answers/participant/{participantId}` — Get answers by participant
* **\[GET]** `/api/v1/answers/participant/{participantId}/question/{questionId}` — Get specific answer

---

### 📊 Session Report Management

* **\[POST]** `/api/v1/sessions/{sessionId}/generate-report` — Generate a session report
* **\[GET]** `/api/v1/sessions/{sessionId}/report` — Get session report
* **\[GET]** `/api/v1/sessions/reports/{hostId}` — Get reports by host

---

### ❓ Question Management

* **\[GET]** `/api/v1/questions` — Get all questions 
* **\[POST]** `/api/v1/questions` — Create new question 
* **\[DELETE]** `/api/v1/questions` — Delete question by question IDs 
* **\[GET]** `/api/v1/questions/{id}` — Get question by ID 
* **\[DELETE]** `/api/v1/questions/{id}` — Delete single question by ID 
* **\[PATCH]** `/api/v1/questions/{id}` — Partially update question by ID
* **\[GET]** `/api/v1/questions/me` — Get all questions created by me

---

### 🧍 Participant Management

* **\[POST]** `/api/v1/participants/submit-answer` — Submit answer as participant
* **\[POST]** `/api/v1/participants/join` — Join quiz session
* **\[POST]** `/api/v1/participants/join/auth-user` — Join quiz session as authenticated user
* **\[GET]** `/api/v1/participants/session/{quizCode}` — Host: get all participants
* **\[GET]** `/api/v1/participants/session/{quizCode}/nickname-available` — Check nickname availability
* **\[GET]** `/api/v1/participants/session/{quizCode}/can-join` — Check if can join
* **\[DELETE]** `/api/v1/participants/{participantId}` — Host: delete participant

---

### 🏆 Leaderboard Management

* **\[POST]** `/api/v1/leaderboard/session/{sessionId}/finalize` — Finalize leaderboard when session ends
* **\[POST]** `/api/v1/leaderboard/session/{sessionCode}/initialize` — Initialize leaderboard for session
* **\[POST]** `/api/v1/leaderboard/live` — Get live leaderboard
* **\[POST]** `/api/v1/leaderboard/history` — Get historical leaderboards
* **\[GET]** `/api/v1/leaderboard/session/{sessionId}/top/{limit}` — Get top participants
* **\[GET]** `/api/v1/leaderboard/session/{sessionId}/stats` — Get session stats
* **\[GET]** `/api/v1/leaderboard/session/{sessionId}/report` — Get session leaderboard report
* **\[GET]** `/api/v1/leaderboard/session/{sessionCode}` — Get leaderboard by session code
* **\[GET]** `/api/v1/leaderboard/session/{sessionCode}/podium` — Get podium
* **\[GET]** `/api/v1/leaderboard/session/{sessionCode}/participant/{participantId}/rank` — Get participant rank

---

### 🗂️ Category Management

* **\[GET]** `/api/v1/categories` — Get all categories
* **\[POST]** `/api/v1/categories` — Create category quizzes
* **\[POST]** `/api/v1/categories/batch` — Create categories quizzes

---

### 🔐 Authentication

* **\[POST]** `/api/v1/auth/register` — Register
* **\[POST]** `/api/v1/auth/oauth/register` — OAuth register
* **\[POST]** `/api/v1/auth/login` — Login

---

### 📡 WebSocket Messaging

* **\[SEND]** `/app/events` — Broadcast events to all clients (subscribed to `/topic/events`)
* **\[SEND]** `/topic/session/{sessionId}` — Send events to all users in a session
* **\[SEND]** `/user/{participantId}/queue/private` — Send private events to a specific participant

---

## 🧩 Example Workflows

### 👤 User Registration and Login

1. Register a user

   ```http
   POST /api/v1/auth/register
   ```
2. Login to get access tokens

   ```http
   POST /api/v1/auth/login
   ```

---

### 🧠 Quiz Session Flow

1. Start a quiz session

   ```http
   PUT /api/v1/sessions/{sessionId}/start
   ```
2. Move to next question

   ```http
   PUT /api/v1/sessions/{sessionId}/next-question
   ```
3. End the session

   ```http
   PUT /api/v1/sessions/{sessionId}/end
   ```

---

© 2025 StackQuiz — API Documentation

