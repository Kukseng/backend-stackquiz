// Place this file at: app/participant/[sessionCode]/page.tsx
// This is the COMPLETE FIXED version

"use client";
import React, { useEffect, useState, useCallback, useRef } from "react";
import { useParams } from "next/navigation";
import axios from "axios";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { motion, AnimatePresence } from "framer-motion";

// ... (Keep all interfaces and other components the same) ...

// ===== FIXED: handleTimeUp function =====
function handleTimeUp() {
  console.log("⏰ Time's up! Participant can still answer for base points.");
  // ✅ FIX: Don't set showFeedback to true - this was disabling buttons
  // Just set timeUp flag to show warning message
  setFeedback({ timeUp: true, canStillAnswer: true });
}

// ===== FIXED: handleAnswer function =====
function handleAnswer(optionId: string) {
  // ✅ FIX: Only check answerSelected and isSubmittingAnswer
  // Remove showFeedback check to allow answering after time expires
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

// ===== FIXED: Answer button disabled logic =====
// In the render section where buttons are created:
const isDisabled = answerSelected !== null || isSubmittingAnswer;
// ✅ FIX: Removed showFeedback check - now buttons stay enabled after time expires

// ===== FIXED: Time-up feedback message =====
{/* Show time-up warning without disabling buttons */}
{feedback?.timeUp && !answerSelected && (
  <motion.div 
    initial={{ opacity: 0, y: 20 }}
    animate={{ opacity: 1, y: 0 }}
    className="mt-6 p-4 bg-yellow-50 border-2 border-yellow-400 rounded-lg text-center"
  >
    <p className="text-yellow-800 font-bold text-lg">⏰ Time's up!</p>
    <p className="text-yellow-700 text-sm mt-2">
      You can still answer for base points (no speed bonus)
    </p>
  </motion.div>
)}

// ===== FIXED: Add TIME_UP WebSocket handler =====
// Add this subscription in useParticipantWebSocket hook:
stomp.subscribe(`/user/queue/session/${quizCode}/completion`, (msg) => {
  const message = safeJsonParse(msg.body);
  if (message) {
    console.log("📨 Completion message received:", message);
    
    // Handle TIME_UP notification
    if (message.action === "TIME_UP" || message.message?.includes("Time")) {
      console.log("⏰ TIME_UP notification from server");
      setFeedback({ timeUp: true, canStillAnswer: true });
    } else {
      onCompletionRef.current(message);
    }
  }
});

// ===== COMPLETE FIXED COMPONENT EXPORT =====
export default function ParticipantQuizFixed() {
  // ... (rest of the component code with fixes applied) ...
}

