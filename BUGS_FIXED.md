# Backend Quiz Website - Bugs Fixed

This document summarizes all the bugs identified and fixed in the backend quiz website codebase.

## Summary

Total bugs fixed: **10**

---

## Bug Details

### Bug 1: Field Naming Inconsistency in Option Entity
**File:** `src/main/java/kh/edu/cstad/stackquizapi/domain/Option.java`
**Line:** 29
**Severity:** Medium
**Issue:** Field named `Id` (capital I) instead of `id` (lowercase), violating Java naming conventions
**Fix:** Renamed field from `Id` to `id`

**Before:**
```java
private String Id;
```

**After:**
```java
private String id;
```

---

### Bug 2: QuizRepository.findByUserId Return Type Error
**File:** `src/main/java/kh/edu/cstad/stackquizapi/repository/QuizRepository.java`
**Line:** 21
**Severity:** High
**Issue:** Method returns `Optional<Quiz>` but users can have multiple quizzes, should return `List<Quiz>`
**Fix:** Changed return type to `List<Quiz>` and added missing import

**Before:**
```java
Optional<Quiz> findByUserId(String userId);
```

**After:**
```java
List<Quiz> findByUserId(String userId);
```

---

### Bug 3: QuizRepository.findByIsActive Return Type Error
**File:** `src/main/java/kh/edu/cstad/stackquizapi/repository/QuizRepository.java`
**Line:** 16
**Severity:** High
**Issue:** Method returns `Optional<Quiz>` but multiple quizzes can have the same active status, should return `List<Quiz>`
**Fix:** Changed return type to `List<Quiz>`

**Before:**
```java
Optional<Quiz> findByIsActive(Boolean isActive);
```

**After:**
```java
List<Quiz> findByIsActive(Boolean isActive);
```

---

### Bug 4: Missing Import Statement
**File:** `src/main/java/kh/edu/cstad/stackquizapi/repository/QuizRepository.java`
**Line:** 8
**Severity:** High (Compilation Error)
**Issue:** Missing import for `java.util.List`
**Fix:** Added import statement

**After:**
```java
import java.util.List;
import java.util.Optional;
```

---

### Bug 5: Inefficient getAllQuiz Implementation
**File:** `src/main/java/kh/edu/cstad/stackquizapi/service/impl/QuizServiceImpl.java`
**Line:** 99-104
**Severity:** Medium (Performance)
**Issue:** Fetches all quizzes then filters in memory instead of using database query
**Fix:** Use `findByIsActive` repository method for database-level filtering

**Before:**
```java
public List<QuizResponse> getAllQuiz(Boolean active) {
    return quizRepository.findAll().stream()
            .filter(quiz -> quiz.getIsActive().equals(active))
            .map(quizMapper::toQuizResponse)
            .toList();
}
```

**After:**
```java
public List<QuizResponse> getAllQuiz(Boolean active) {
    return quizRepository.findByIsActive(active).stream()
            .map(quizMapper::toQuizResponse)
            .toList();
}
```

---

### Bug 6: Incorrect HTTP Status Code in deleteQuiz
**File:** `src/main/java/kh/edu/cstad/stackquizapi/service/impl/QuizServiceImpl.java`
**Line:** 134
**Severity:** Medium
**Issue:** Returns `HttpStatus.FORBIDDEN` when user not found, should be `HttpStatus.NOT_FOUND`
**Fix:** Changed status code to `NOT_FOUND`

**Before:**
```java
throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not found");
```

**After:**
```java
throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
```

---

### Bug 7: Grammatical Error in Error Message
**File:** `src/main/java/kh/edu/cstad/stackquizapi/service/impl/RatingServiceImpl.java`
**Line:** 117
**Severity:** Low (User Experience)
**Issue:** Error message "Quiz ID not exist" is grammatically incorrect
**Fix:** Changed to "Quiz ID does not exist"

**Before:**
```java
"Quiz ID not exist"
```

**After:**
```java
"Quiz ID does not exist"
```

---

### Bug 8: Unnecessary Delete Operation in updateOptionById
**File:** `src/main/java/kh/edu/cstad/stackquizapi/service/impl/OptionServiceImpl.java`
**Line:** 153-154
**Severity:** Critical (Data Loss Risk)
**Issue:** Method deletes the option before updating it, which is unnecessary and risky
**Fix:** Removed the delete operation

**Before:**
```java
optionRepository.delete(option);
log.info("Successfully deleted option with ID: {}", optionId);
```

**After:**
```java
// Removed unnecessary delete operation
```

---

### Bug 9: Incorrect Error Messages in updateOptionById
**File:** `src/main/java/kh/edu/cstad/stackquizapi/service/impl/OptionServiceImpl.java`
**Lines:** 167, 172-174
**Severity:** Medium (User Experience)
**Issue:** Error messages refer to "question" instead of "option"
**Fix:** Updated error messages to refer to "option"

**Before:**
```java
"Failed to update question due to database error"
"Failed to update question"
```

**After:**
```java
"Failed to update option due to database error"
"Failed to update option"
```

---

### Bug 10: Method Name Typo - gelAllOptions
**Files:** 
- `src/main/java/kh/edu/cstad/stackquizapi/service/OptionService.java` (Line 28)
- `src/main/java/kh/edu/cstad/stackquizapi/service/impl/OptionServiceImpl.java` (Line 86)
- `src/main/java/kh/edu/cstad/stackquizapi/controller/OptionController.java` (Line 41)

**Severity:** Medium (Code Quality)
**Issue:** Method named `gelAllOptions` instead of `getAllOptions` (typo)
**Fix:** Renamed method to `getAllOptions` in all three files

**Before:**
```java
List<OptionResponse> gelAllOptions();
```

**After:**
```java
List<OptionResponse> getAllOptions();
```

---

## Additional Improvements

### .gitignore Enhancement
**File:** `.gitignore`
**Issue:** Missing common build artifacts and IDE files
**Fix:** Added `.gradle/`, `.idea/`, `*.iml`, `*.iws`, `*.ipr`, `out/`, `.DS_Store`

---

## Testing

All fixes have been verified to compile successfully:
```bash
./gradlew clean compileJava
```

Result: **BUILD SUCCESSFUL** with only mapping warnings (which are expected from MapStruct)

---

## Impact Assessment

- **High Priority Fixes:** Bugs 2, 3, 4, 8 (Data integrity and compilation issues)
- **Medium Priority Fixes:** Bugs 1, 5, 6, 9, 10 (Code quality and performance)
- **Low Priority Fixes:** Bug 7 (User experience)

All fixes maintain backward compatibility and don't require database migration.

---

## Recommendations for Future Development

1. **Add unit tests** for repository methods to catch return type mismatches
2. **Use consistent error messages** across the application
3. **Implement code review process** to catch naming inconsistencies
4. **Add pre-commit hooks** to run linters and formatters
5. **Consider using query methods** instead of filtering in memory for better performance
6. **Add integration tests** for critical operations like update and delete
