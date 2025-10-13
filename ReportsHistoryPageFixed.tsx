// ReportsHistoryPage.tsx - UPDATED VERSION
// Quizizz-style Reports History Page for Hosts
// Place this file in: app/reports/page.tsx

"use client";
import React, { useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { useRouter } from "next/navigation";
import axios from "axios";

// ===== INTERFACES =====
interface SessionSummary {
  sessionId: string;
  sessionCode: string;
  sessionName: string;
  quizTitle: string;
  status: "WAITING" | "SCHEDULED" | "IN_PROGRESS" | "PAUSED" | "COMPLETED" | "CANCELLED";
  startTime: string;
  endTime: string;
  totalParticipants: number;
  averageAccuracy: number;
  completionRate: number;
  hostName: string;
  totalQuestions: number;
}

type FilterType = "ALL" | "RUNNING" | "SCHEDULED" | "COMPLETED" | "PAUSED";

// ===== MAIN COMPONENT =====
export default function ReportsHistoryPage() {
  const router = useRouter();
  const [sessions, setSessions] = useState<SessionSummary[]>([]);
  const [filteredSessions, setFilteredSessions] = useState<SessionSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [activeFilter, setActiveFilter] = useState<FilterType>("ALL");
  const [searchQuery, setSearchQuery] = useState("");
  const [sortBy, setSortBy] = useState<"date" | "participants" | "accuracy">("date");
  const [sortOrder, setSortOrder] = useState<"asc" | "desc">("desc");

  useEffect(() => {
    fetchHostSessions();
  }, []);

  useEffect(() => {
    filterAndSortSessions();
  }, [sessions, activeFilter, searchQuery, sortBy, sortOrder]);

  const fetchHostSessions = async () => {
    setLoading(true);
    setError("");

    try {
      // ✅ UPDATED: Use the new reports-history endpoint
      const response = await axios.get(
        `http://localhost:9999/api/v1/reports-history/my-sessions`,
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );

      setSessions(response.data);
    } catch (err: any) {
      console.error("Error fetching sessions:", err);
      setError(err.response?.data?.message || "Failed to load sessions");
    } finally {
      setLoading(false);
    }
  };

  const filterAndSortSessions = () => {
    let filtered = [...sessions];

    // Apply status filter
    if (activeFilter !== "ALL") {
      filtered = filtered.filter((session) => {
        switch (activeFilter) {
          case "RUNNING":
            return session.status === "IN_PROGRESS";
          case "SCHEDULED":
            return session.status === "SCHEDULED";
          case "COMPLETED":
            return session.status === "COMPLETED";
          case "PAUSED":
            return session.status === "PAUSED";
          default:
            return true;
        }
      });
    }

    // Apply search filter
    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(
        (session) =>
          session.quizTitle.toLowerCase().includes(query) ||
          session.sessionName.toLowerCase().includes(query) ||
          session.sessionCode.toLowerCase().includes(query)
      );
    }

    // Apply sorting
    filtered.sort((a, b) => {
      let comparison = 0;

      switch (sortBy) {
        case "date":
          comparison = new Date(a.startTime).getTime() - new Date(b.startTime).getTime();
          break;
        case "participants":
          comparison = a.totalParticipants - b.totalParticipants;
          break;
        case "accuracy":
          comparison = a.averageAccuracy - b.averageAccuracy;
          break;
      }

      return sortOrder === "asc" ? comparison : -comparison;
    });

    setFilteredSessions(filtered);
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "IN_PROGRESS":
        return "bg-green-100 text-green-700 border-green-300";
      case "COMPLETED":
        return "bg-blue-100 text-blue-700 border-blue-300";
      case "SCHEDULED":
        return "bg-purple-100 text-purple-700 border-purple-300";
      case "PAUSED":
        return "bg-yellow-100 text-yellow-700 border-yellow-300";
      case "WAITING":
        return "bg-gray-100 text-gray-700 border-gray-300";
      default:
        return "bg-gray-100 text-gray-700 border-gray-300";
    }
  };

  const getStatusLabel = (status: string) => {
    switch (status) {
      case "IN_PROGRESS":
        return "Running";
      case "COMPLETED":
        return "Completed";
      case "SCHEDULED":
        return "Scheduled";
      case "PAUSED":
        return "Paused";
      case "WAITING":
        return "Waiting";
      default:
        return status;
    }
  };

  const getAccuracyColor = (accuracy: number) => {
    if (accuracy >= 70) return "text-green-600";
    if (accuracy >= 40) return "text-yellow-600";
    return "text-red-600";
  };

  const handleViewReport = (sessionCode: string) => {
    router.push(`/host/${sessionCode}/report`);
  };

  const getFilterCount = (filter: FilterType) => {
    if (filter === "ALL") return sessions.length;

    return sessions.filter((session) => {
      switch (filter) {
        case "RUNNING":
          return session.status === "IN_PROGRESS";
        case "SCHEDULED":
          return session.status === "SCHEDULED";
        case "COMPLETED":
          return session.status === "COMPLETED";
        case "PAUSED":
          return session.status === "PAUSED";
        default:
          return false;
      }
    }).length;
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

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-50 via-blue-50 to-indigo-50">
      <div className="max-w-7xl mx-auto p-6">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-8"
        >
          <h1 className="text-4xl font-bold text-gray-800 mb-2">📊 Reports</h1>
          <p className="text-gray-600">View and manage your quiz session reports</p>
        </motion.div>

        {/* Search and Filters */}
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="bg-white rounded-xl shadow-lg p-6 mb-6"
        >
          {/* Search Bar */}
          <div className="mb-6">
            <div className="relative">
              <input
                type="text"
                placeholder="Search by report name, quiz title, or code..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full px-4 py-3 pl-12 border-2 border-gray-200 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
              />
              <span className="absolute left-4 top-3.5 text-gray-400 text-xl">🔍</span>
            </div>
          </div>

          {/* Status Filters */}
          <div className="flex flex-wrap gap-3 mb-6">
            {[
              { id: "ALL", label: "All" },
              { id: "RUNNING", label: "Running" },
              { id: "SCHEDULED", label: "Scheduled" },
              { id: "COMPLETED", label: "Completed" },
              { id: "PAUSED", label: "Paused" },
            ].map((filter) => (
              <button
                key={filter.id}
                onClick={() => setActiveFilter(filter.id as FilterType)}
                className={`px-6 py-2 rounded-full font-semibold transition-all ${
                  activeFilter === filter.id
                    ? "bg-purple-600 text-white shadow-lg"
                    : "bg-gray-100 text-gray-700 hover:bg-gray-200"
                }`}
              >
                {filter.label} ({getFilterCount(filter.id as FilterType)})
              </button>
            ))}
          </div>

          {/* Sort Controls */}
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-4">
              <span className="text-sm font-medium text-gray-700">Sort by:</span>
              <select
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value as any)}
                className="px-4 py-2 border-2 border-gray-200 rounded-lg focus:ring-2 focus:ring-purple-500"
              >
                <option value="date">Date hosted</option>
                <option value="participants">Participants</option>
                <option value="accuracy">Accuracy</option>
              </select>
              <button
                onClick={() => setSortOrder(sortOrder === "asc" ? "desc" : "asc")}
                className="px-4 py-2 bg-gray-100 hover:bg-gray-200 rounded-lg font-medium transition-colors"
              >
                {sortOrder === "asc" ? "↑ Ascending" : "↓ Descending"}
              </button>
            </div>
            <div className="text-sm text-gray-600">
              Showing {filteredSessions.length} of {sessions.length} sessions
            </div>
          </div>
        </motion.div>

        {/* Sessions Table */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
          className="bg-white rounded-xl shadow-lg overflow-hidden"
        >
          {error && (
            <div className="p-6 bg-red-50 border-b border-red-200">
              <p className="text-red-700">{error}</p>
            </div>
          )}

          {filteredSessions.length === 0 ? (
            <div className="p-12 text-center">
              <div className="text-6xl mb-4">📭</div>
              <h3 className="text-xl font-bold text-gray-800 mb-2">No sessions found</h3>
              <p className="text-gray-600">
                {searchQuery
                  ? "Try adjusting your search or filters"
                  : "Start hosting quizzes to see reports here"}
              </p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50 border-b-2 border-gray-200">
                  <tr>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-gray-700">
                      Activity name
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-gray-700">
                      Date hosted
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-gray-700">
                      Participants
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-gray-700">
                      Accuracy
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-gray-700">
                      Code
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-gray-700">
                      Status
                    </th>
                    <th className="px-6 py-4 text-right text-sm font-semibold text-gray-700">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <AnimatePresence>
                    {filteredSessions.map((session, index) => (
                      <motion.tr
                        key={session.sessionId}
                        initial={{ opacity: 0, y: 20 }}
                        animate={{ opacity: 1, y: 0 }}
                        exit={{ opacity: 0, y: -20 }}
                        transition={{ delay: index * 0.05 }}
                        className="border-b border-gray-100 hover:bg-gray-50 transition-colors cursor-pointer"
                        onClick={() => session.status === "COMPLETED" && handleViewReport(session.sessionCode)}
                      >
                        {/* Activity Name */}
                        <td className="px-6 py-4">
                          <div>
                            <div className="font-semibold text-gray-800">
                              {session.quizTitle}
                            </div>
                            <div className="text-sm text-gray-500">{session.sessionName}</div>
                          </div>
                        </td>

                        {/* Date */}
                        <td className="px-6 py-4">
                          <div className="text-sm text-gray-700">
                            {session.startTime ? new Date(session.startTime).toLocaleDateString("en-US", {
                              month: "short",
                              day: "numeric",
                              year: "numeric",
                            }) : "Not started"}
                          </div>
                        </td>

                        {/* Participants */}
                        <td className="px-6 py-4">
                          <div className="text-sm font-semibold text-gray-800">
                            {session.totalParticipants}
                          </div>
                        </td>

                        {/* Accuracy */}
                        <td className="px-6 py-4">
                          <div className="flex items-center space-x-2">
                            <div
                              className={`text-sm font-bold ${getAccuracyColor(
                                session.averageAccuracy
                              )}`}
                            >
                              {session.averageAccuracy.toFixed(0)}%
                            </div>
                            <AccuracyCircle accuracy={session.averageAccuracy} />
                          </div>
                        </td>

                        {/* Code */}
                        <td className="px-6 py-4">
                          <div className="text-sm font-mono text-gray-600">
                            {session.sessionCode}
                          </div>
                        </td>

                        {/* Status */}
                        <td className="px-6 py-4">
                          <span
                            className={`px-3 py-1 rounded-full text-xs font-bold border ${getStatusColor(
                              session.status
                            )}`}
                          >
                            {getStatusLabel(session.status)}
                          </span>
                        </td>

                        {/* Actions */}
                        <td className="px-6 py-4 text-right">
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              handleViewReport(session.sessionCode);
                            }}
                            disabled={session.status !== "COMPLETED"}
                            className={`px-4 py-2 rounded-lg font-semibold transition-all ${
                              session.status === "COMPLETED"
                                ? "bg-purple-600 text-white hover:bg-purple-700 shadow-md hover:shadow-lg"
                                : "bg-gray-200 text-gray-400 cursor-not-allowed"
                            }`}
                          >
                            {session.status === "COMPLETED" ? "View Report" : "Not Available"}
                          </button>
                        </td>
                      </motion.tr>
                    ))}
                  </AnimatePresence>
                </tbody>
              </table>
            </div>
          )}
        </motion.div>
      </div>
    </div>
  );
}

// ===== HELPER COMPONENTS =====
function AccuracyCircle({ accuracy }: { accuracy: number }) {
  const getColor = (acc: number) => {
    if (acc >= 70) return "#10b981"; // green
    if (acc >= 40) return "#f59e0b"; // yellow
    return "#ef4444"; // red
  };

  const radius = 12;
  const circumference = 2 * Math.PI * radius;
  const offset = circumference - (accuracy / 100) * circumference;

  return (
    <svg width="30" height="30" className="transform -rotate-90">
      {/* Background circle */}
      <circle
        cx="15"
        cy="15"
        r={radius}
        stroke="#e5e7eb"
        strokeWidth="3"
        fill="none"
      />
      {/* Progress circle */}
      <circle
        cx="15"
        cy="15"
        r={radius}
        stroke={getColor(accuracy)}
        strokeWidth="3"
        fill="none"
        strokeDasharray={circumference}
        strokeDashoffset={offset}
        strokeLinecap="round"
      />
    </svg>
  );
}

