// ===== FRONTEND FIXES FOR TIME-UP BUG =====

// 🐛 BUG #1: handleTimeUp function (Line 1022-1026)
// ❌ WRONG CODE:
function handleTimeUp() {
  console.log("⏰ Time's up!");
  setShowFeedback(true);  // ❌ This disables answer buttons!
  setFeedback({ timeUp: true });
}

// ✅ FIXED CODE:
function handleTimeUp() {
  console.log("⏰ Time's up! Participant can still answer for base points.");
  // Don't set showFeedback to true - let participant still answer
  setFeedback({ timeUp: true, canStillAnswer: true });
  // Show warning but keep buttons enabled
}

// ===================================

// 🐛 BUG #2: Answer button disabled logic (Line 1426)
// ❌ WRONG CODE:
const isDisabled = answerSelected !== null || showFeedback || isSubmittingAnswer;

// ✅ FIXED CODE:
const isDisabled = answerSelected !== null || (showFeedback && !feedback?.canStillAnswer) || isSubmittingAnswer;
// Now buttons stay enabled if time is up but canStillAnswer is true

// ===================================

// 🐛 BUG #3: Time-up feedback message (Line 1470-1477)
// ❌ WRONG CODE:
{showFeedback && feedback?.timeUp && (
  <motion.div className="mt-6 p-4 bg-red-50 border border-red-200 rounded-lg text-center">
    <p className="text-red-800 font-semibold">⏰ Time's up!</p>
    <p className="text-red-600 text-sm mt-1">No answer submitted</p>
  </motion.div>
)}

// ✅ FIXED CODE:
{feedback?.timeUp && !answerSelected && (
  <motion.div className="mt-6 p-4 bg-yellow-50 border border-yellow-300 rounded-lg text-center">
    <p className="text-yellow-800 font-semibold">⏰ Time's up!</p>
    <p className="text-yellow-700 text-sm mt-1">
      You can still answer for base points (no speed bonus)
    </p>
  </motion.div>
)}

// ===================================

// 🐛 BUG #4: Add TIME_UP message handler in WebSocket subscriptions
// Add this in the useParticipantWebSocket hook

// ✅ NEW CODE TO ADD:
// Handle TIME_UP notifications from backend
stomp.subscribe(`/user/queue/session/${quizCode}/time-up`, (msg) => {
  const timeUpMessage = safeJsonParse(msg.body);
  if (timeUpMessage) {
    console.log("⏰ TIME_UP notification received from server:", timeUpMessage);
    // Show time-up warning but keep buttons enabled
    setFeedback({ timeUp: true, canStillAnswer: true });
  }
});

// ===================================

// COMPLETE FIXED VERSION OF THE KEY FUNCTIONS:

// ✅ Fixed handleTimeUp
function handleTimeUp() {
  console.log("⏰ Time's up! Participant can still answer for base points.");
  setFeedback({ timeUp: true, canStillAnswer: true });
  // Don't disable buttons - participant can still answer
}

// ✅ Fixed handleAnswer
function handleAnswer(optionId: string) {
  // Only check if already answered or currently submitting
  if (!currentQuestion || answerSelected || isSubmittingAnswer) {
    console.warn("⚠️ Cannot answer: already answered or submitting");
    return;
  }
  
  console.log("✅ Answering question:", currentQuestion.id, "with option:", optionId);
  setAnswerSelected(optionId);
  setIsSubmittingAnswer(true);
  
  const success = sendAnswer(optionId, currentQuestion.id);
  if (success) {
    setShowFeedback(true);
    setFeedback({ submitted: true });
  } else {
    setAnswerSelected(null);
    setIsSubmittingAnswer(false);
    setError("Failed to submit answer. Please try again.");
  }
}

// ===================================

// SUMMARY OF CHANGES:

/*
1. ✅ handleTimeUp() - Don't set showFeedback to true
2. ✅ isDisabled logic - Check feedback.canStillAnswer
3. ✅ Time-up message - Show "can still answer" instead of "no answer"
4. ✅ Add TIME_UP WebSocket handler
5. ✅ Remove showFeedback check from handleAnswer (only needed for submitted state)
*/

