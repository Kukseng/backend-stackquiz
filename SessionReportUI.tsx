// SessionReportUI.tsx
// Quizizz-style Session Report Component for Host Dashboard
// Place this file in: components/SessionReportUI.tsx

"use client";
import React, { useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import axios from "axios";

// ===== INTERFACES =====
interface SessionReportData {
  sessionId: string;
  sessionCode: string;
  sessionName: string;
  quizTitle: string;
  hostName: string;
  startTime: string;
  endTime: string;
  duration: number;
  status: string;
  statistics: SessionStatistics;
  questionAnalysis: QuestionAnalysis[];
  participantReports: ParticipantReport[];
  performanceInsights: PerformanceInsights;
}

interface SessionStatistics {
  totalParticipants: number;
  completedParticipants: number;
  completionRate: number;
  averageScore: number;
  averageAccuracy: number;
  averageResponseTime: number;
  totalQuestions: number;
  totalAnswers: number;
  correctAnswers: number;
  incorrectAnswers: number;
  engagementRate: number;
}

interface QuestionAnalysis {
  questionNumber: number;
  questionText: string;
  questionType: string;
  difficulty: string;
  totalAttempts: number;
  correctAttempts: number;
  incorrectAttempts: number;
  accuracyRate: number;
  averageResponseTime: number;
  options: OptionAnalysis[];
}

interface OptionAnalysis {
  optionText: string;
  isCorrect: boolean;
  selectionCount: number;
  selectionPercentage: number;
}

interface ParticipantReport {
  participantId: string;
  nickname: string;
  avatarId: string;
  totalScore: number;
  rank: number;
  questionsAnswered: number;
  correctAnswers: number;
  incorrectAnswers: number;
  accuracy: number;
  averageResponseTime: number;
  completionStatus: string;
  performance: PerformanceMetrics;
}

interface PerformanceMetrics {
  scorePercentile: number;
  accuracyPercentile: number;
  speedPercentile: number;
  consistencyScore: number;
  improvementTrend: string;
}

interface PerformanceInsights {
  strengths: string[];
  weaknesses: string[];
  recommendations: string[];
  dropoffRate: number;
  engagementLevel: string;
  difficultyBalance: string;
}

// ===== MAIN COMPONENT =====
export default function SessionReportUI({ sessionCode }: { sessionCode: string }) {
  const [reportData, setReportData] = useState<SessionReportData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [activeTab, setActiveTab] = useState<"overview" | "questions" | "participants" | "insights">("overview");
  const [exportFormat, setExportFormat] = useState<"PDF" | "CSV" | "EXCEL">("PDF");

  useEffect(() => {
    fetchSessionReport();
  }, [sessionCode]);

  const fetchSessionReport = async () => {
    setLoading(true);
    setError("");
    
    try {
      const response = await axios.get(
        `http://localhost:9999/api/v1/reports/session/${sessionCode}`,
        {
          params: {
            reportType: "DETAILED",
            includeDetailedAnswers: true,
            includePerformanceInsights: true,
            includeRecommendations: true,
          },
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );
      
      setReportData(response.data);
    } catch (err: any) {
      console.error("Error fetching session report:", err);
      setError(err.response?.data?.message || "Failed to load session report");
    } finally {
      setLoading(false);
    }
  };

  const handleExport = async () => {
    try {
      const response = await axios.get(
        `http://localhost:9999/api/v1/reports/session/${sessionCode}/export`,
        {
          params: {
            format: exportFormat,
            reportType: "DETAILED",
          },
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
          responseType: "blob",
        }
      );

      // Create download link
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", `session_report_${sessionCode}.${exportFormat.toLowerCase()}`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      console.error("Error exporting report:", err);
      alert("Failed to export report");
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-gradient-to-br from-purple-50 to-blue-50">
        <motion.div
          animate={{ rotate: 360 }}
          transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
          className="w-16 h-16 border-4 border-purple-600 border-t-transparent rounded-full"
        />
      </div>
    );
  }

  if (error || !reportData) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-gradient-to-br from-purple-50 to-blue-50">
        <div className="bg-white rounded-xl p-8 shadow-lg max-w-md">
          <div className="text-6xl text-center mb-4">😞</div>
          <h2 className="text-2xl font-bold text-gray-800 mb-2 text-center">Report Not Available</h2>
          <p className="text-gray-600 text-center mb-4">{error || "Unable to load session report"}</p>
          <button
            onClick={fetchSessionReport}
            className="w-full px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700"
          >
            Try Again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-50 via-blue-50 to-indigo-50 p-6">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          className="bg-white rounded-xl shadow-lg p-6 mb-6"
        >
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-gray-800 mb-2">
                📊 Session Report
              </h1>
              <p className="text-gray-600">
                {reportData.quizTitle} • Session: {reportData.sessionCode}
              </p>
              <p className="text-sm text-gray-500">
                Hosted by {reportData.hostName} • {new Date(reportData.startTime).toLocaleString()}
              </p>
            </div>
            
            {/* Export Button */}
            <div className="flex items-center space-x-2">
              <select
                value={exportFormat}
                onChange={(e) => setExportFormat(e.target.value as any)}
                className="px-3 py-2 border rounded-lg focus:ring-2 focus:ring-purple-500"
              >
                <option value="PDF">PDF</option>
                <option value="CSV">CSV</option>
                <option value="EXCEL">Excel</option>
              </select>
              <button
                onClick={handleExport}
                className="px-6 py-2 bg-gradient-to-r from-purple-600 to-indigo-600 text-white rounded-lg hover:from-purple-700 hover:to-indigo-700 font-semibold shadow-lg"
              >
                📥 Export
              </button>
            </div>
          </div>
        </motion.div>

        {/* Tabs */}
        <div className="bg-white rounded-xl shadow-lg mb-6 overflow-hidden">
          <div className="flex border-b">
            {[
              { id: "overview", label: "📈 Overview", icon: "📈" },
              { id: "questions", label: "❓ Questions", icon: "❓" },
              { id: "participants", label: "👥 Participants", icon: "👥" },
              { id: "insights", label: "💡 Insights", icon: "💡" },
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id as any)}
                className={`flex-1 px-6 py-4 font-semibold transition-all ${
                  activeTab === tab.id
                    ? "bg-purple-600 text-white"
                    : "text-gray-600 hover:bg-gray-50"
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>

          {/* Tab Content */}
          <div className="p-6">
            <AnimatePresence mode="wait">
              {activeTab === "overview" && (
                <OverviewTab key="overview" data={reportData} />
              )}
              {activeTab === "questions" && (
                <QuestionsTab key="questions" questions={reportData.questionAnalysis} />
              )}
              {activeTab === "participants" && (
                <ParticipantsTab key="participants" participants={reportData.participantReports} />
              )}
              {activeTab === "insights" && (
                <InsightsTab key="insights" insights={reportData.performanceInsights} />
              )}
            </AnimatePresence>
          </div>
        </div>
      </div>
    </div>
  );
}

// ===== OVERVIEW TAB =====
function OverviewTab({ data }: { data: SessionReportData }) {
  const stats = data.statistics;

  return (
    <motion.div
      initial={{ opacity: 0, x: -20 }}
      animate={{ opacity: 1, x: 0 }}
      exit={{ opacity: 0, x: 20 }}
      className="space-y-6"
    >
      {/* Key Metrics */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <MetricCard
          icon="👥"
          label="Total Participants"
          value={stats.totalParticipants}
          color="blue"
        />
        <MetricCard
          icon="✅"
          label="Completion Rate"
          value={`${stats.completionRate.toFixed(1)}%`}
          color="green"
        />
        <MetricCard
          icon="🎯"
          label="Average Accuracy"
          value={`${stats.averageAccuracy.toFixed(1)}%`}
          color="purple"
        />
        <MetricCard
          icon="⏱️"
          label="Avg Response Time"
          value={`${stats.averageResponseTime.toFixed(1)}s`}
          color="orange"
        />
      </div>

      {/* Score Distribution */}
      <div className="bg-gray-50 rounded-xl p-6">
        <h3 className="text-xl font-bold text-gray-800 mb-4">📊 Score Distribution</h3>
        <ScoreDistributionChart participants={data.participantReports} />
      </div>

      {/* Quick Stats */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-gradient-to-br from-blue-50 to-blue-100 rounded-xl p-6">
          <h3 className="text-lg font-bold text-blue-800 mb-4">📝 Quiz Details</h3>
          <div className="space-y-2 text-sm">
            <div className="flex justify-between">
              <span className="text-blue-700">Total Questions:</span>
              <span className="font-bold text-blue-900">{stats.totalQuestions}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-blue-700">Total Answers:</span>
              <span className="font-bold text-blue-900">{stats.totalAnswers}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-blue-700">Correct Answers:</span>
              <span className="font-bold text-green-600">{stats.correctAnswers}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-blue-700">Incorrect Answers:</span>
              <span className="font-bold text-red-600">{stats.incorrectAnswers}</span>
            </div>
          </div>
        </div>

        <div className="bg-gradient-to-br from-purple-50 to-purple-100 rounded-xl p-6">
          <h3 className="text-lg font-bold text-purple-800 mb-4">⏰ Session Timeline</h3>
          <div className="space-y-2 text-sm">
            <div className="flex justify-between">
              <span className="text-purple-700">Started:</span>
              <span className="font-bold text-purple-900">
                {new Date(data.startTime).toLocaleString()}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-purple-700">Ended:</span>
              <span className="font-bold text-purple-900">
                {new Date(data.endTime).toLocaleString()}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-purple-700">Duration:</span>
              <span className="font-bold text-purple-900">
                {Math.floor(data.duration / 60)}m {data.duration % 60}s
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-purple-700">Status:</span>
              <span className="font-bold text-purple-900">{data.status}</span>
            </div>
          </div>
        </div>
      </div>
    </motion.div>
  );
}

// ===== QUESTIONS TAB =====
function QuestionsTab({ questions }: { questions: QuestionAnalysis[] }) {
  return (
    <motion.div
      initial={{ opacity: 0, x: -20 }}
      animate={{ opacity: 1, x: 0 }}
      exit={{ opacity: 0, x: 20 }}
      className="space-y-4"
    >
      {questions.map((question, index) => (
        <motion.div
          key={index}
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: index * 0.1 }}
          className="bg-white border-2 border-gray-200 rounded-xl p-6 hover:shadow-lg transition-shadow"
        >
          {/* Question Header */}
          <div className="flex items-start justify-between mb-4">
            <div className="flex-1">
              <div className="flex items-center space-x-3 mb-2">
                <span className="text-2xl font-bold text-purple-600">
                  Q{question.questionNumber}
                </span>
                <span className={`px-3 py-1 rounded-full text-xs font-bold ${
                  question.difficulty === "EASY" ? "bg-green-100 text-green-700" :
                  question.difficulty === "MEDIUM" ? "bg-yellow-100 text-yellow-700" :
                  "bg-red-100 text-red-700"
                }`}>
                  {question.difficulty}
                </span>
                <span className="px-3 py-1 bg-blue-100 text-blue-700 rounded-full text-xs font-bold">
                  {question.questionType}
                </span>
              </div>
              <p className="text-gray-800 font-medium">{question.questionText}</p>
            </div>
            
            {/* Accuracy Badge */}
            <div className="text-center ml-4">
              <div className={`text-3xl font-bold ${
                question.accuracyRate >= 70 ? "text-green-600" :
                question.accuracyRate >= 40 ? "text-yellow-600" :
                "text-red-600"
              }`}>
                {question.accuracyRate.toFixed(0)}%
              </div>
              <div className="text-xs text-gray-500">Accuracy</div>
            </div>
          </div>

          {/* Stats */}
          <div className="grid grid-cols-3 gap-4 mb-4">
            <div className="text-center p-3 bg-gray-50 rounded-lg">
              <div className="text-lg font-bold text-gray-800">{question.totalAttempts}</div>
              <div className="text-xs text-gray-600">Attempts</div>
            </div>
            <div className="text-center p-3 bg-green-50 rounded-lg">
              <div className="text-lg font-bold text-green-600">{question.correctAttempts}</div>
              <div className="text-xs text-green-700">Correct</div>
            </div>
            <div className="text-center p-3 bg-red-50 rounded-lg">
              <div className="text-lg font-bold text-red-600">{question.incorrectAttempts}</div>
              <div className="text-xs text-red-700">Incorrect</div>
            </div>
          </div>

          {/* Options Analysis */}
          <div className="space-y-2">
            {question.options.map((option, optIndex) => (
              <div key={optIndex} className="relative">
                <div className={`flex items-center justify-between p-3 rounded-lg ${
                  option.isCorrect ? "bg-green-50 border-2 border-green-300" : "bg-gray-50"
                }`}>
                  <div className="flex items-center space-x-3 flex-1">
                    <span className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold ${
                      option.isCorrect ? "bg-green-500 text-white" : "bg-gray-300 text-gray-700"
                    }`}>
                      {String.fromCharCode(65 + optIndex)}
                    </span>
                    <span className="text-sm font-medium text-gray-800">{option.optionText}</span>
                    {option.isCorrect && <span className="text-green-600 font-bold">✓ Correct</span>}
                  </div>
                  <div className="text-right">
                    <div className="text-sm font-bold text-gray-800">
                      {option.selectionCount} ({option.selectionPercentage.toFixed(1)}%)
                    </div>
                  </div>
                </div>
                {/* Progress Bar */}
                <div className="absolute bottom-0 left-0 h-1 bg-purple-500 rounded-full"
                     style={{ width: `${option.selectionPercentage}%` }}
                />
              </div>
            ))}
          </div>

          {/* Avg Response Time */}
          <div className="mt-4 text-center text-sm text-gray-600">
            ⏱️ Average response time: <span className="font-bold">{question.averageResponseTime.toFixed(1)}s</span>
          </div>
        </motion.div>
      ))}
    </motion.div>
  );
}

// ===== PARTICIPANTS TAB =====
function ParticipantsTab({ participants }: { participants: ParticipantReport[] }) {
  const [sortBy, setSortBy] = useState<"rank" | "score" | "accuracy">("rank");
  const [sortedParticipants, setSortedParticipants] = useState(participants);

  useEffect(() => {
    const sorted = [...participants].sort((a, b) => {
      switch (sortBy) {
        case "rank":
          return a.rank - b.rank;
        case "score":
          return b.totalScore - a.totalScore;
        case "accuracy":
          return b.accuracy - a.accuracy;
        default:
          return 0;
      }
    });
    setSortedParticipants(sorted);
  }, [sortBy, participants]);

  return (
    <motion.div
      initial={{ opacity: 0, x: -20 }}
      animate={{ opacity: 1, x: 0 }}
      exit={{ opacity: 0, x: 20 }}
      className="space-y-4"
    >
      {/* Sort Controls */}
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-bold text-gray-800">
          {participants.length} Participants
        </h3>
        <div className="flex items-center space-x-2">
          <span className="text-sm text-gray-600">Sort by:</span>
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value as any)}
            className="px-3 py-2 border rounded-lg focus:ring-2 focus:ring-purple-500"
          >
            <option value="rank">Rank</option>
            <option value="score">Score</option>
            <option value="accuracy">Accuracy</option>
          </select>
        </div>
      </div>

      {/* Participants List */}
      <div className="space-y-3">
        {sortedParticipants.map((participant, index) => (
          <motion.div
            key={participant.participantId}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: index * 0.05 }}
            className={`flex items-center justify-between p-4 rounded-xl border-2 transition-all hover:shadow-lg ${
              participant.rank === 1 ? "bg-gradient-to-r from-yellow-50 to-orange-50 border-yellow-300" :
              participant.rank === 2 ? "bg-gradient-to-r from-gray-50 to-gray-100 border-gray-300" :
              participant.rank === 3 ? "bg-gradient-to-r from-orange-50 to-orange-100 border-orange-300" :
              "bg-white border-gray-200"
            }`}
          >
            {/* Rank and Avatar */}
            <div className="flex items-center space-x-4">
              <div className={`text-2xl font-bold ${
                participant.rank === 1 ? "text-yellow-600" :
                participant.rank === 2 ? "text-gray-600" :
                participant.rank === 3 ? "text-orange-600" :
                "text-gray-400"
              }`}>
                {participant.rank === 1 ? "🥇" :
                 participant.rank === 2 ? "🥈" :
                 participant.rank === 3 ? "🥉" :
                 `#${participant.rank}`}
              </div>
              <div>
                <div className="font-bold text-gray-800">{participant.nickname}</div>
                <div className="text-xs text-gray-500">ID: {participant.avatarId}</div>
              </div>
            </div>

            {/* Stats */}
            <div className="flex items-center space-x-6">
              <div className="text-center">
                <div className="text-xl font-bold text-purple-600">{participant.totalScore}</div>
                <div className="text-xs text-gray-500">Score</div>
              </div>
              <div className="text-center">
                <div className="text-lg font-bold text-green-600">{participant.accuracy.toFixed(1)}%</div>
                <div className="text-xs text-gray-500">Accuracy</div>
              </div>
              <div className="text-center">
                <div className="text-lg font-bold text-blue-600">
                  {participant.correctAnswers}/{participant.questionsAnswered}
                </div>
                <div className="text-xs text-gray-500">Correct</div>
              </div>
              <div className="text-center">
                <div className="text-sm font-bold text-orange-600">
                  {participant.averageResponseTime.toFixed(1)}s
                </div>
                <div className="text-xs text-gray-500">Avg Time</div>
              </div>
            </div>
          </motion.div>
        ))}
      </div>
    </motion.div>
  );
}

// ===== INSIGHTS TAB =====
function InsightsTab({ insights }: { insights: PerformanceInsights }) {
  return (
    <motion.div
      initial={{ opacity: 0, x: -20 }}
      animate={{ opacity: 1, x: 0 }}
      exit={{ opacity: 0, x: 20 }}
      className="space-y-6"
    >
      {/* Strengths */}
      <div className="bg-gradient-to-br from-green-50 to-green-100 rounded-xl p-6">
        <h3 className="text-xl font-bold text-green-800 mb-4 flex items-center">
          <span className="mr-2">💪</span> Strengths
        </h3>
        <ul className="space-y-2">
          {insights.strengths.map((strength, index) => (
            <li key={index} className="flex items-start space-x-2">
              <span className="text-green-600 mt-1">✓</span>
              <span className="text-green-800">{strength}</span>
            </li>
          ))}
        </ul>
      </div>

      {/* Weaknesses */}
      <div className="bg-gradient-to-br from-red-50 to-red-100 rounded-xl p-6">
        <h3 className="text-xl font-bold text-red-800 mb-4 flex items-center">
          <span className="mr-2">⚠️</span> Areas for Improvement
        </h3>
        <ul className="space-y-2">
          {insights.weaknesses.map((weakness, index) => (
            <li key={index} className="flex items-start space-x-2">
              <span className="text-red-600 mt-1">!</span>
              <span className="text-red-800">{weakness}</span>
            </li>
          ))}
        </ul>
      </div>

      {/* Recommendations */}
      <div className="bg-gradient-to-br from-blue-50 to-blue-100 rounded-xl p-6">
        <h3 className="text-xl font-bold text-blue-800 mb-4 flex items-center">
          <span className="mr-2">💡</span> Recommendations
        </h3>
        <ul className="space-y-2">
          {insights.recommendations.map((recommendation, index) => (
            <li key={index} className="flex items-start space-x-2">
              <span className="text-blue-600 mt-1">→</span>
              <span className="text-blue-800">{recommendation}</span>
            </li>
          ))}
        </ul>
      </div>

      {/* Engagement Metrics */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-white rounded-xl p-6 border-2 border-purple-200">
          <div className="text-center">
            <div className="text-3xl font-bold text-purple-600">
              {(100 - insights.dropoffRate).toFixed(1)}%
            </div>
            <div className="text-sm text-gray-600 mt-2">Retention Rate</div>
          </div>
        </div>
        <div className="bg-white rounded-xl p-6 border-2 border-blue-200">
          <div className="text-center">
            <div className="text-2xl font-bold text-blue-600">
              {insights.engagementLevel}
            </div>
            <div className="text-sm text-gray-600 mt-2">Engagement Level</div>
          </div>
        </div>
        <div className="bg-white rounded-xl p-6 border-2 border-green-200">
          <div className="text-center">
            <div className="text-2xl font-bold text-green-600">
              {insights.difficultyBalance}
            </div>
            <div className="text-sm text-gray-600 mt-2">Difficulty Balance</div>
          </div>
        </div>
      </div>
    </motion.div>
  );
}

// ===== HELPER COMPONENTS =====
function MetricCard({ icon, label, value, color }: any) {
  const colorClasses = {
    blue: "from-blue-50 to-blue-100 text-blue-800 border-blue-200",
    green: "from-green-50 to-green-100 text-green-800 border-green-200",
    purple: "from-purple-50 to-purple-100 text-purple-800 border-purple-200",
    orange: "from-orange-50 to-orange-100 text-orange-800 border-orange-200",
  };

  return (
    <motion.div
      whileHover={{ scale: 1.02 }}
      className={`bg-gradient-to-br ${colorClasses[color]} rounded-xl p-6 border-2`}
    >
      <div className="text-3xl mb-2">{icon}</div>
      <div className="text-2xl font-bold">{value}</div>
      <div className="text-sm opacity-75">{label}</div>
    </motion.div>
  );
}

function ScoreDistributionChart({ participants }: { participants: ParticipantReport[] }) {
  // Calculate score ranges
  const ranges = [
    { label: "0-20%", min: 0, max: 20, count: 0 },
    { label: "21-40%", min: 21, max: 40, count: 0 },
    { label: "41-60%", min: 41, max: 60, count: 0 },
    { label: "61-80%", min: 61, max: 80, count: 0 },
    { label: "81-100%", min: 81, max: 100, count: 0 },
  ];

  participants.forEach((p) => {
    const percentage = p.accuracy;
    const range = ranges.find((r) => percentage >= r.min && percentage <= r.max);
    if (range) range.count++;
  });

  const maxCount = Math.max(...ranges.map((r) => r.count));

  return (
    <div className="space-y-3">
      {ranges.map((range, index) => (
        <div key={index} className="flex items-center space-x-4">
          <div className="w-20 text-sm font-medium text-gray-700">{range.label}</div>
          <div className="flex-1 bg-gray-200 rounded-full h-8 relative overflow-hidden">
            <motion.div
              initial={{ width: 0 }}
              animate={{ width: `${(range.count / maxCount) * 100}%` }}
              transition={{ duration: 0.5, delay: index * 0.1 }}
              className="h-full bg-gradient-to-r from-purple-500 to-indigo-500 rounded-full flex items-center justify-end pr-3"
            >
              {range.count > 0 && (
                <span className="text-white font-bold text-sm">{range.count}</span>
              )}
            </motion.div>
          </div>
        </div>
      ))}
    </div>
  );
}

