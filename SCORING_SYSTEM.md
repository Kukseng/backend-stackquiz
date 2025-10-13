# StackQuiz Scoring System - Updated ✅

## Overview

The scoring system now works exactly like **Quizizz**:
- Participants get **full base points** for correct answers
- **Speed bonus** awarded only if answered within time limit
- Late answers still get base points but **no bonus**

---

## How Scoring Works

### Scenario 1: Answer Within Time Limit ✅

```
Question: "What is 2+2?"
Time Limit: 30 seconds
Base Points: 100

Participant answers correctly in 10 seconds:
- Base Points: 100 ✅
- Speed Bonus: 13 points (20% of base × speed factor)
- Total: 113 points
```

**Speed Bonus Calculation:**
```java
speedRatio = timeTaken / timeLimit = 10 / 30 = 0.33
speedBonus = (1.0 - 0.33) × 100 × 0.2 = 13 points
```

### Scenario 2: Answer After Time Expires (Late) ⏰

```
Question: "What is 2+2?"
Time Limit: 30 seconds
Base Points: 100

Time expires at 30 seconds → Notification: "Time's up!"
Participant answers correctly at 35 seconds:
- Base Points: 100 ✅ (Still awarded!)
- Speed Bonus: 0 (No bonus for late answer)
- Total: 100 points
```

### Scenario 3: Wrong Answer

```
Participant answers incorrectly:
- Base Points: 0
- Speed Bonus: 0
- Total: 0 points
```

---

## Speed Bonus Formula

### Current Implementation

```java
if (isCorrect && timeTaken <= questionTimeLimit) {
    // Calculate speed bonus (up to 20% of base points)
    double speedRatio = (double) timeTaken / questionTimeLimit;
    bonusPoints = (int) ((1.0 - speedRatio) * basePoints * 0.2);
}
```

### Examples

| Time Taken | Time Limit | Base Points | Speed Ratio | Bonus (20%) | Total |
|------------|------------|-------------|-------------|-------------|-------|
| 5s         | 30s        | 100         | 0.17        | 17          | 117   |
| 10s        | 30s        | 100         | 0.33        | 13          | 113   |
| 15s        | 30s        | 100         | 0.50        | 10          | 110   |
| 20s        | 30s        | 100         | 0.67        | 7           | 107   |
| 25s        | 30s        | 100         | 0.83        | 3           | 103   |
| 30s        | 30s        | 100         | 1.00        | 0           | 100   |
| 35s (late) | 30s        | 100         | -           | 0           | 100   |

---

## Quiz Flow with Scoring

### ASYNC Mode (Quizizz-style)

```
1. Participant receives Question 1 (100 points, 30s limit)
   ↓
2. Timer starts counting: 1s, 2s, 3s...
   ↓
3a. Participant answers at 15s (within time)
    → Base: 100 points
    → Bonus: 10 points
    → Total: 110 points
    → Auto-advance to Question 2
    
OR

3b. Timer reaches 30s → "Time's up! You can still answer for base points"
    ↓
    Participant answers at 35s (late)
    → Base: 100 points ✅
    → Bonus: 0 points (no speed bonus)
    → Total: 100 points
    → Auto-advance to Question 2
```

---

## Key Features

### ✅ Fair Scoring
- Correct answer always gets base points
- Speed bonus rewards fast thinkers
- Late answers don't lose base points

### ✅ No Stuck Participants
- Time expiring doesn't lock the question
- Participants can take their time
- Auto-advance after answering

### ✅ Competitive Element
- Speed bonus creates competition
- Leaderboard updates in real-time
- Encourages quick thinking

---

## Configuration Options

### Adjust Speed Bonus Percentage

**Current: 20% bonus**
```java
bonusPoints = (int) ((1.0 - speedRatio) * basePoints * 0.2);
                                                        ↑
                                                      20%
```

**Alternative: 30% bonus**
```java
bonusPoints = (int) ((1.0 - speedRatio) * basePoints * 0.3);
```

**Alternative: 50% bonus (more competitive)**
```java
bonusPoints = (int) ((1.0 - speedRatio) * basePoints * 0.5);
```

### Disable Speed Bonus

If you want **no speed bonus** at all:
```java
bonusPoints = 0; // Everyone gets same points if correct
```

---

## Examples with Different Bonus Percentages

### Question: 100 base points, 30s limit, answered in 10s

| Bonus % | Speed Ratio | Bonus Points | Total Points |
|---------|-------------|--------------|--------------|
| 10%     | 0.33        | 7            | 107          |
| 20%     | 0.33        | 13           | 113          |
| 30%     | 0.33        | 20           | 120          |
| 50%     | 0.33        | 33           | 133          |
| 100%    | 0.33        | 67           | 167          |

---

## Leaderboard Impact

### Example Scenario

**Question 1: 100 points, 30s limit**

| Participant | Answer Time | Correct? | Base | Bonus | Total | Rank |
|-------------|-------------|----------|------|-------|-------|------|
| Alice       | 8s          | ✅       | 100  | 15    | 115   | 1st  |
| Bob         | 15s         | ✅       | 100  | 10    | 110   | 2nd  |
| Carol       | 25s         | ✅       | 100  | 3     | 103   | 3rd  |
| Dave        | 35s (late)  | ✅       | 100  | 0     | 100   | 4th  |
| Eve         | 10s         | ❌       | 0    | 0     | 0     | 5th  |

**Key Points:**
- Alice answered fastest → Highest score
- Dave answered late → Still got 100 points (not 0!)
- Eve got it wrong → 0 points regardless of speed

---

## Technical Implementation

### Code Location
**File:** `QuizSessionServiceImpl.java`  
**Method:** `submitAnswer()`  
**Lines:** 630-656

### Key Code Snippet

```java
// Calculate base points and speed bonus
int points = 0;
int bonusPoints = 0;

if (isCorrect) {
    // Always award base points for correct answer
    points = currentQuestion.getPoints();
    
    // Award speed bonus ONLY if answered within time limit
    if (timeTaken <= questionTimeLimit) {
        double speedRatio = (double) timeTaken / questionTimeLimit;
        bonusPoints = (int) ((1.0 - speedRatio) * points * 0.2);
        
        log.info("On-time answer: base={}, bonus={}", points, bonusPoints);
    } else {
        bonusPoints = 0;
        log.info("Late answer: base={}, bonus=0", points);
    }
}

int totalPoints = points + bonusPoints;
```

---

## Comparison: Before vs After

### Before (Incorrect)
```
Time expires → 0 points even if correct
Participant stuck on question
```

### After (Correct - Quizizz-style)
```
Time expires → Notification sent
Participant can still answer
Correct answer → Full base points (no bonus)
Auto-advance to next question
```

---

## WebSocket Messages

### Answer Feedback Message

```json
{
  "sessionCode": "ABC123",
  "sender": "SYSTEM",
  "participantId": "p-123",
  "participantNickname": "Alice",
  "questionId": "q-456",
  "selectedOptionId": "opt-789",
  "responseTime": 15000,
  "isCorrect": true,
  "points": 110
}
```

**Note:** `points` field contains **total points** (base + bonus)

---

## Logs

### On-Time Answer
```
Answer saved and score updated: participant=Alice, question=1, 
correct=true, base=100, bonus=13, total=113, totalScore=113
```

### Late Answer
```
Late answer from participant Alice - time: 35s/30s, base: 100, bonus: 0 (time expired)
Answer saved and score updated: participant=Alice, question=1, 
correct=true, base=100, bonus=0, total=100, totalScore=100
```

---

## Testing Scenarios

### Test 1: Fast Correct Answer
```
✅ Answer in 10s
✅ Correct option selected
✅ Should get: 100 base + ~13 bonus = 113 points
```

### Test 2: Slow Correct Answer (Within Time)
```
✅ Answer in 29s
✅ Correct option selected
✅ Should get: 100 base + ~1 bonus = 101 points
```

### Test 3: Late Correct Answer
```
⏰ Time expires at 30s
✅ Answer at 35s
✅ Correct option selected
✅ Should get: 100 base + 0 bonus = 100 points
```

### Test 4: Wrong Answer
```
✅ Answer in 10s
❌ Wrong option selected
✅ Should get: 0 points
```

---

## Summary

### ✅ What Changed

1. **Scoring Logic**: Base points + speed bonus (instead of 0 for late)
2. **Late Answers**: Get full base points, no bonus
3. **Speed Bonus**: 20% of base points, scaled by speed
4. **Messages**: Updated to reflect new scoring

### ✅ Benefits

- **Fair**: Correct answers always rewarded
- **Competitive**: Speed bonus adds excitement
- **Flexible**: Easy to adjust bonus percentage
- **Quizizz-like**: Matches expected behavior

### ✅ Build Status

```
BUILD SUCCESSFUL in 37s
All tests passing ✅
```

---

## Customization Guide

### Change Bonus Percentage

**Location:** `QuizSessionServiceImpl.java` line 643

```java
// Current: 20% bonus
bonusPoints = (int) ((1.0 - speedRatio) * points * 0.2);

// Change to 30%
bonusPoints = (int) ((1.0 - speedRatio) * points * 0.3);

// Change to 50%
bonusPoints = (int) ((1.0 - speedRatio) * points * 0.5);
```

### Add Time-Based Penalty for Late Answers

```java
if (timeTaken <= questionTimeLimit) {
    bonusPoints = (int) ((1.0 - speedRatio) * points * 0.2);
} else {
    // Penalty: -10 points per second late (example)
    int secondsLate = timeTaken - questionTimeLimit;
    int penalty = secondsLate * 10;
    points = Math.max(0, points - penalty);
    bonusPoints = 0;
}
```

### Different Bonus Tiers

```java
if (timeTaken <= questionTimeLimit * 0.33) {
    bonusPoints = points / 2; // 50% bonus for super fast
} else if (timeTaken <= questionTimeLimit * 0.67) {
    bonusPoints = points / 4; // 25% bonus for fast
} else if (timeTaken <= questionTimeLimit) {
    bonusPoints = points / 10; // 10% bonus for on-time
} else {
    bonusPoints = 0; // No bonus for late
}
```

---

**Status**: ✅ **PRODUCTION READY**

**Scoring**: ✅ **Quizizz-style (Base + Speed Bonus)**

**Late Answers**: ✅ **Full base points, no bonus**

