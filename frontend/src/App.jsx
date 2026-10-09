import { useEffect, useMemo, useState } from "react";
import "./App.css";

const API_URL = "http://localhost:8080";
const APPLICATION_STATUSES = ["Saved", "Applied", "Assessment", "Interview", "Offer", "Rejected", "Withdrawn"];

const emptyApplication = {
  jobTitle: "",
  company: "",
  jobUrl: "",
  status: "Saved",
  appliedDate: new Date().toISOString().slice(0, 10),
  notes: "",
};

async function readResponse(response) {
  const data = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(data.error || `Request failed (${response.status}).`);
  return data;
}

function App() {
  const [backendStatus, setBackendStatus] = useState("Checking connection...");
  const [resume, setResume] = useState(null);
  const [jobDescription, setJobDescription] = useState("");
  const [analysis, setAnalysis] = useState(null);
  const [analyzing, setAnalyzing] = useState(false);
  const [error, setError] = useState("");
  const [savedAnalyses, setSavedAnalyses] = useState([]);
  const [historyError, setHistoryError] = useState("");
  const [applications, setApplications] = useState([]);
  const [applicationForm, setApplicationForm] = useState(emptyApplication);
  const [savingApplication, setSavingApplication] = useState(false);
  const [applicationError, setApplicationError] = useState("");
  const [applicationNotice, setApplicationNotice] = useState("");
  const [interviewQuestions, setInterviewQuestions] = useState([]);
  const [interviewLoading, setInterviewLoading] = useState(false);
  const [interviewError, setInterviewError] = useState("");
  const [answers, setAnswers] = useState({});
  const [feedback, setFeedback] = useState({});
  const [feedbackLoading, setFeedbackLoading] = useState({});
  const [aiRecommendations, setAiRecommendations] = useState("");
  const [aiRecommendationLoading, setAiRecommendationLoading] = useState(false);
  const [aiError, setAiError] = useState("");
  const [aiConfigured, setAiConfigured] = useState(false);

  const backendConnected = backendStatus === "Backend is running successfully!" || backendStatus === "Backend connected";
  const applicationStats = useMemo(() => ({
    total: applications.length,
    interviews: applications.filter((item) => item.status === "Interview").length,
    offers: applications.filter((item) => item.status === "Offer").length,
  }), [applications]);

  useEffect(() => {
    checkBackend();
    loadHistory();
    loadApplications();
    checkAiStatus();
  }, []);

  async function checkBackend() {
    try {
      const data = await readResponse(await fetch(`${API_URL}/api/health`));
      setBackendStatus(data.message || "Backend connected");
    } catch {
      setBackendStatus("Backend is unavailable");
    }
  }

  async function loadHistory() {
    try {
      const data = await readResponse(await fetch(`${API_URL}/api/analyses`));
      setSavedAnalyses(Array.isArray(data) ? data : []);
      setHistoryError("");
    } catch {
      setHistoryError("Saved history is unavailable. Make sure the backend and MySQL are running.");
    }
  }

  async function loadApplications() {
    try {
      const data = await readResponse(await fetch(`${API_URL}/api/applications`));
      setApplications(Array.isArray(data) ? data : []);
      setApplicationError("");
    } catch (err) {
      setApplicationError(err.message || "Could not load applications. Check the backend.");
    }
  }

  async function checkAiStatus() {
    try {
      const data = await readResponse(await fetch(`${API_URL}/api/ai/status`));
      setAiConfigured(Boolean(data.configured));
    } catch {
      setAiConfigured(false);
    }
  }

  function handleResumeChange(event) {
    const file = event.target.files?.[0];
    setResume(null);
    setAnalysis(null);
    setError("");
    setAiRecommendations("");
    if (!file) return;
    if (!/\.(pdf|docx)$/i.test(file.name)) {
      setError("Please upload a PDF or DOCX resume.");
      event.target.value = "";
      return;
    }
    if (file.size > 10 * 1024 * 1024) {
      setError("Your resume must be 10 MB or smaller.");
      event.target.value = "";
      return;
    }
    setResume(file);
  }

  async function analyzeJob(event) {
    event?.preventDefault();
    setError("");
    setAnalysis(null);
    setAiRecommendations("");
    setAiError("");
    if (!resume) return setError("Please upload your resume first.");
    if (!jobDescription.trim()) return setError("Please paste the job description first.");
    if (!backendConnected) {
      await checkBackend();
      return setError("The backend is unavailable. Check that Spring Boot is running on port 8080.");
    }
    setAnalyzing(true);
    try {
      const formData = new FormData();
      formData.append("resume", resume);
      formData.append("jobDescription", jobDescription.trim());
      const data = await readResponse(await fetch(`${API_URL}/api/analyze`, { method: "POST", body: formData }));
      setAnalysis(data);
      await loadHistory();
    } catch (err) {
      setError(err.message || "Unable to analyze your resume.");
    } finally {
      setAnalyzing(false);
    }
  }

  async function generateAiRecommendations() {
    if (!analysis) return;
    setAiRecommendationLoading(true);
    setAiError("");
    setAiRecommendations("");
    try {
      const data = await readResponse(await fetch(`${API_URL}/api/ai/recommendations`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          jobDescription,
          resumeSkills: analysis.resumeSkills || [],
          jobSkills: analysis.jobSkills || [],
          matchingSkills: analysis.matchingSkills || [],
          missingSkills: analysis.missingSkills || [],
          matchScore: analysis.matchScore || 0,
        }),
      }));
      setAiRecommendations(data.recommendations || "No recommendations were returned.");
    } catch (err) {
      setAiError(err.message || "Could not generate Smart recommendations.");
    } finally {
      setAiRecommendationLoading(false);
    }
  }

  async function saveApplication(event) {
    event.preventDefault();
    setApplicationError("");
    setApplicationNotice("");
    setSavingApplication(true);
    try {
      const data = await readResponse(await fetch(`${API_URL}/api/applications`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          ...applicationForm,
          appliedDate: applicationForm.appliedDate || null,
          jobUrl: applicationForm.jobUrl.trim() || null,
          notes: applicationForm.notes.trim() || null,
        }),
      }));
      setApplications((current) => [data, ...current]);
      setApplicationForm(emptyApplication);
      setApplicationNotice("Application saved to MySQL.");
    } catch (err) {
      setApplicationError(err.message || "Could not save this application.");
    } finally {
      setSavingApplication(false);
    }
  }

  async function updateApplicationStatus(id, status) {
    setApplicationError("");
    try {
      const updated = await readResponse(await fetch(`${API_URL}/api/applications/${id}/status`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ status }),
      }));
      setApplications((current) => current.map((item) => item.id === id ? updated : item));
    } catch (err) {
      setApplicationError(err.message || "Could not update status.");
    }
  }

  async function deleteApplication(id) {
    if (!window.confirm("Delete this application from your tracker?")) return;
    setApplicationError("");
    try {
      const response = await fetch(`${API_URL}/api/applications/${id}`, { method: "DELETE" });
      if (!response.ok && response.status !== 204) await readResponse(response);
      setApplications((current) => current.filter((item) => item.id !== id));
      setApplicationNotice("Application deleted.");
    } catch (err) {
      setApplicationError(err.message || "Could not delete this application.");
    }
  }

  async function generateInterviewQuestions() {
    setInterviewError("");
    setInterviewQuestions([]);
    setAnswers({});
    setFeedback({});
    if (!jobDescription.trim()) {
      setInterviewError("Paste a job description in Resume Analysis first.");
      return;
    }
    setInterviewLoading(true);
    try {
      const data = await readResponse(await fetch(`${API_URL}/api/interview/questions`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ jobDescription: jobDescription.trim(), skills: analysis?.jobSkills || [] }),
      }));
      setInterviewQuestions(Array.isArray(data) ? data : []);
    } catch (err) {
      setInterviewError(err.message || "Could not generate interview questions.");
    } finally {
      setInterviewLoading(false);
    }
  }

  async function submitInterviewAnswer(index, question) {
    const answer = (answers[index] || "").trim();
    if (!answer) {
      setInterviewError(`Write your answer to question ${index + 1} before requesting feedback.`);
      return;
    }
    setInterviewError("");
    setFeedbackLoading((current) => ({ ...current, [index]: true }));
    setFeedback((current) => ({ ...current, [index]: "" }));
    try {
      const data = await readResponse(await fetch(`${API_URL}/api/ai/interview-feedback`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ jobDescription, question: question.question, answer }),
      }));
      setFeedback((current) => ({ ...current, [index]: data.feedback || "No feedback returned." }));
    } catch (err) {
      setInterviewError(err.message || "Could not evaluate your answer.");
    } finally {
      setFeedbackLoading((current) => ({ ...current, [index]: false }));
    }
  }

  function viewSavedAnalysis(item) {
    setAnalysis(item);
    setJobDescription(item.jobDescription || "");
    setAiRecommendations("");
    document.getElementById("resume")?.scrollIntoView({ behavior: "smooth" });
  }

  return (
    <div className="app">
      <aside className="sidebar">
        <div className="brand"><div className="brand-icon">J</div><span>JobCopilot</span></div>
        <div className="sidebar-label">WORKSPACE</div>
        <nav className="navigation">
          <a href="#dashboard" className="nav-item"><span>▦</span> Dashboard</a>
          <a href="#applications" className="nav-item"><span>▤</span> Application Tracker</a>
          <a href="#interview" className="nav-item"><span>◉</span> Interview Prep</a>
          <a href="#resume" className="nav-item"><span>▧</span> Resume Analysis</a>
        </nav>
        <div className="sidebar-bottom">
          <div className="help-card"><div className="help-icon">✦</div><strong>Your career, accelerated.</strong><p>Prepare smarter for your next opportunity.</p></div>
          <div className="profile"><div className="avatar">CT</div><div className="profile-info"><strong>Chaya Tharun</strong><span>Job seeker</span></div><span className="profile-menu">•••</span></div>
        </div>
      </aside>

      <main className="main-content" id="dashboard">
        <header className="topbar">
          <div><div className="breadcrumb">Workspace / Dashboard</div><h1>Dashboard</h1></div>
          <div className="topbar-right"><div className={`connection-status ${backendConnected ? "connected" : "disconnected"}`} title={backendStatus}><span className="status-dot" />{backendConnected ? "Backend connected" : backendStatus}</div><div className="avatar top-avatar">CT</div></div>
        </header>

        <section className="welcome-section">
          <div><div className="eyebrow">YOUR CAREER WORKSPACE</div><h2>Find the jobs<br /><span>you're built for.</span></h2><p>Analyze job descriptions, track applications, and practise for your next interview.</p></div>
          <div className="welcome-decoration" aria-hidden="true"><div className="decoration-orbit orbit-one" /><div className="decoration-orbit orbit-two" /><div className="decoration-center">✦</div><div className="decoration-bubble bubble-one">AI</div><div className="decoration-bubble bubble-two">✓</div></div>
        </section>

        <section className="stats-grid" aria-label="Career statistics">
          <article className="stat-card"><div className="stat-heading"><span>Applications</span><span className="stat-icon purple">▤</span></div><div className="stat-number">{applicationStats.total}</div><div className="stat-caption">Tracked opportunities</div></article>
          <article className="stat-card"><div className="stat-heading"><span>Latest Match</span><span className="stat-icon blue">◎</span></div><div className="stat-number">{analysis ? `${analysis.matchScore}%` : "—"}</div><div className="stat-caption">Latest resume analysis</div></article>
          <article className="stat-card"><div className="stat-heading"><span>Interviews</span><span className="stat-icon orange">◷</span></div><div className="stat-number">{applicationStats.interviews}</div><div className="stat-caption">Applications at interview stage</div></article>
          <article className="stat-card"><div className="stat-heading"><span>Offers</span><span className="stat-icon green">✧</span></div><div className="stat-number">{applicationStats.offers}</div><div className="stat-caption">Offers received</div></article>
        </section>

        <section className="workspace-grid">
          <article className="panel analysis-panel" id="resume">
            <div className="panel-header"><div><div className="eyebrow">RESUME INTELLIGENCE</div><h3>Analyze a job match</h3><p>Compare your resume with your target role.</p></div><div className="panel-icon">✦</div></div>
            <form onSubmit={analyzeJob}>
              <div className="form-section"><label htmlFor="resume-upload">Your resume</label><label className="upload-box" htmlFor="resume-upload"><input id="resume-upload" type="file" accept=".pdf,.docx" onChange={handleResumeChange} /><div className="upload-icon">↑</div><div className="upload-text"><strong>{resume ? resume.name : "Click to upload your resume"}</strong><span>{resume ? `${(resume.size / 1024).toFixed(1)} KB` : "PDF or DOCX · Max 10 MB"}</span></div><span className="browse-label">{resume ? "Change" : "Browse"}</span></label></div>
              <div className="form-section"><div className="label-row"><label htmlFor="job-description">Job description</label><span className="optional-label">Required</span></div><textarea id="job-description" rows={7} placeholder="Paste the job description here. Include responsibilities, required skills, and qualifications..." value={jobDescription} onChange={(event) => { setJobDescription(event.target.value); setAnalysis(null); setAiRecommendations(""); }} /><div className="character-count">{jobDescription.length} characters</div></div>
              {error && <div className="error-message" role="alert">{error}</div>}
              <button className="primary-button" type="submit" disabled={analyzing}>{analyzing ? "Analyzing your resume..." : "✦ Analyze Job Match"}{!analyzing && <span>→</span>}</button>
            </form>
            <p className="privacy-note"><span>♧</span> Resume text is processed by your backend. AI features send only the text needed for the requested coaching action.</p>
          </article>

          <div className="right-column">
            <article className="panel results-panel">
              <div className="panel-header"><div><h3>Match insights</h3><p>{analysis ? "Your resume analysis results" : "Your job analysis will appear here."}</p></div><span className="insights-icon">◈</span></div>
              {!analysis ? <div className="empty-state"><div className="empty-illustration"><div className="empty-document"><span /><span /><span /><div className="empty-sparkle">✦</div></div></div><strong>Ready to discover your fit?</strong><p>Upload your resume and add a job description to discover matching and missing skills.</p></div> : <div className="analysis-result" aria-live="polite">
                <div className="result-success">✓ Analysis complete</div><div className="score-display"><span>Job match score</span><strong>{analysis.matchScore}%</strong></div>
                <div className="score-track" role="progressbar" aria-label="Job match score" aria-valuemin={0} aria-valuemax={100} aria-valuenow={analysis.matchScore}><div className="score-fill" style={{ width: `${analysis.matchScore}%` }} /></div>
                <div className="result-summary"><div><strong>{analysis.matchingSkills?.length || 0}</strong><span>Matching skills</span></div><div><strong>{analysis.missingSkills?.length || 0}</strong><span>Missing skills</span></div></div>
                <div className="skill-group"><h4>Matching skills</h4>{analysis.matchingSkills?.length ? <div className="skill-list">{analysis.matchingSkills.map((skill) => <span className="skill-tag matched" key={skill}>✓ {skill}</span>)}</div> : <p>No matching skills detected.</p>}</div>
                <div className="skill-group"><h4>Skills to develop</h4>{analysis.missingSkills?.length ? <div className="skill-list">{analysis.missingSkills.map((skill) => <span className="skill-tag missing" key={skill}>+ {skill}</span>)}</div> : <p>No missing skills were detected in the supported skill list.</p>}</div>
                <div className="skill-group"><h4>Rule-based recommendations</h4><ul className="recommendation-list">{(analysis.recommendations || []).map((recommendation, index) => <li key={`${index}-${recommendation}`}>{recommendation}</li>)}</ul></div>
                <button className="primary-button ai-action-button" type="button" onClick={generateAiRecommendations} disabled={aiRecommendationLoading}>{aiRecommendationLoading ? "Generating Local coaching..." : "✦ Get Smart recommendations"}</button>
                {!aiConfigured && <p className="small-hint">Local rule-based coaching is ready. No external API key is needed.</p>}
                {aiError && <div className="error-message" role="alert">{aiError}</div>}
                {aiRecommendations && <div className="ai-feedback"><h4>Local career coach</h4><div className="feedback-text">{aiRecommendations}</div></div>}
                <div className="result-notice">This score compares detected skills only. It does not measure overall experience, proficiency, or semantic fit.</div>
                <button className="secondary-button" type="button" onClick={() => { setAnalysis(null); setAiRecommendations(""); setError(""); }}>Analyze another job</button>
              </div>}
            </article>
            <article className="panel connection-panel"><div className="connection-heading"><div className="connection-symbol">⌘</div><div><h3>System connection</h3><p>Spring Boot API</p></div></div><div className="connection-detail"><span>Backend</span><span className={backendConnected ? "text-success" : ""}><span className="status-dot" />{backendConnected ? "Online" : backendStatus}</span></div><div className="connection-detail"><span>Analysis API</span><code>POST /api/analyze</code></div><div className="connection-detail"><span>Local coaching</span><span>{aiConfigured ? "Configured" : "API key needed"}</span></div><div className="connection-detail"><span>Port</span><code>8080</code></div></article>
          </div>
        </section>

        <section className="panel applications-panel" id="applications">
          <div className="panel-header"><div><div className="eyebrow">JOB SEARCH ORGANIZER</div><h3>Application tracker</h3><p>Save opportunities, update their status, and keep notes in MySQL.</p></div><span className="interview-panel-icon">▤</span></div>
          <form className="application-form" onSubmit={saveApplication}>
            <div className="application-form-grid">
              <div className="form-section"><label htmlFor="app-job-title">Job title *</label><input id="app-job-title" required maxLength={200} value={applicationForm.jobTitle} onChange={(e) => setApplicationForm({ ...applicationForm, jobTitle: e.target.value })} placeholder="e.g. Junior Java Developer" /></div>
              <div className="form-section"><label htmlFor="app-company">Company *</label><input id="app-company" required maxLength={200} value={applicationForm.company} onChange={(e) => setApplicationForm({ ...applicationForm, company: e.target.value })} placeholder="e.g. Example Technologies" /></div>
              <div className="form-section"><label htmlFor="app-status">Status</label><select id="app-status" value={applicationForm.status} onChange={(e) => setApplicationForm({ ...applicationForm, status: e.target.value })}>{APPLICATION_STATUSES.map((status) => <option key={status}>{status}</option>)}</select></div>
              <div className="form-section"><label htmlFor="app-date">Applied / saved date</label><input id="app-date" type="date" value={applicationForm.appliedDate} onChange={(e) => setApplicationForm({ ...applicationForm, appliedDate: e.target.value })} /></div>
              <div className="form-section application-url-field"><label htmlFor="app-url">Job link (optional)</label><input id="app-url" type="url" value={applicationForm.jobUrl} onChange={(e) => setApplicationForm({ ...applicationForm, jobUrl: e.target.value })} placeholder="https://..." /></div>
              <div className="form-section application-notes-field"><label htmlFor="app-notes">Notes (optional)</label><textarea id="app-notes" rows={2} maxLength={3000} value={applicationForm.notes} onChange={(e) => setApplicationForm({ ...applicationForm, notes: e.target.value })} placeholder="Recruiter contact, deadline, follow-up..." /></div>
            </div>
            <button className="primary-button tracker-save-button" type="submit" disabled={savingApplication}>{savingApplication ? "Saving application..." : "＋ Save application"}</button>
          </form>
          {applicationError && <div className="error-message" role="alert">{applicationError}</div>}
          {applicationNotice && <div className="success-message" role="status">{applicationNotice}</div>}
          <div className="tracker-heading"><h4>Your applications</h4><button className="secondary-button compact-button" type="button" onClick={loadApplications}>Refresh</button></div>
          {applications.length === 0 ? <div className="applications-empty"><div className="applications-empty-icon">▤</div><strong>No applications tracked yet</strong><p>Add a job above to start tracking your search.</p></div> : <div className="tracker-list">{applications.map((item) => <article className="tracker-item" key={item.id}><div className="tracker-company-mark">{(item.company || "?").slice(0, 1).toUpperCase()}</div><div className="tracker-item-main"><strong>{item.jobTitle}</strong><span>{item.company}</span><small>{item.appliedDate ? `Date: ${item.appliedDate}` : "Date not set"}{item.jobUrl ? <> · <a href={item.jobUrl} target="_blank" rel="noreferrer">Job posting ↗</a></> : null}</small>{item.notes && <p>{item.notes}</p>}</div><div className="tracker-item-actions"><select aria-label={`Status for ${item.jobTitle}`} value={item.status} onChange={(e) => updateApplicationStatus(item.id, e.target.value)}>{APPLICATION_STATUSES.map((status) => <option key={status}>{status}</option>)}</select><button className="delete-button" type="button" onClick={() => deleteApplication(item.id)} aria-label={`Delete ${item.jobTitle}`}>Delete</button></div></article>)}</div>}
        </section>

        <section className="panel applications-panel" id="history">
          <div className="panel-header"><div><div className="eyebrow">MYSQL HISTORY</div><h3>Saved job analyses</h3><p>Completed resume matches are stored in your local database.</p></div><button className="secondary-button compact-button" type="button" onClick={loadHistory}>Refresh history</button></div>
          {historyError && <div className="error-message" role="alert">{historyError}</div>}
          {savedAnalyses.length === 0 ? <div className="applications-empty"><div className="applications-empty-icon">▤</div><strong>No saved analyses yet</strong><p>Analyze a resume and job description. The result will be saved automatically.</p></div> : <div className="history-list">{savedAnalyses.map((item) => <article className="history-item" key={item.id}><div className="history-score">{item.matchScore}%</div><div className="history-details"><strong>{item.resumeFilename || "Resume analysis"}</strong><span>{item.jobSkills?.length || 0} detected job skills · {item.matchingSkills?.length || 0} matches</span><small>{item.createdAt ? new Date(item.createdAt).toLocaleString() : "Saved analysis"}</small></div><button className="secondary-button compact-button" type="button" onClick={() => viewSavedAnalysis(item)}>View result</button></article>)}</div>}
        </section>

        <section className="panel interview-panel" id="interview">
          <div className="panel-header"><div><div className="eyebrow">INTERVIEW PRACTICE</div><h3>Practise and get Practice feedback</h3><p>Generate role-specific questions, write your answer, and request coaching.</p></div><span className="interview-panel-icon">✦</span></div>
          <div className="interview-intro"><strong>{analysis ? "Questions can use your latest detected job skills." : "Start with a job description."}</strong><p>Question templates and answer feedback use local rules. No external API key or paid service is required.</p></div>
          {interviewError && <div className="error-message" role="alert">{interviewError}</div>}
          <button className="primary-button interview-generate" type="button" onClick={generateInterviewQuestions} disabled={interviewLoading}>{interviewLoading ? "Preparing questions..." : "Generate interview questions"}{!interviewLoading && <span>→</span>}</button>
          {!aiConfigured && <p className="small-hint">Local practice feedback is available without an API key.</p>}
          {interviewQuestions.length > 0 && <div className="question-list"><div className="question-count">{interviewQuestions.length} practice questions</div>{interviewQuestions.map((item, index) => <article className="question-card" key={`${item.category}-${index}`}><div className="question-meta"><span>{item.category}</span><span>Question {index + 1}</span></div><h4>{item.question}</h4><p className="question-guidance">Suggested approach: {item.suggestedAnswer}</p><label className="answer-label" htmlFor={`interview-answer-${index}`}>Your answer</label><textarea id={`interview-answer-${index}`} rows={4} value={answers[index] || ""} onChange={(e) => setAnswers((current) => ({ ...current, [index]: e.target.value }))} placeholder="Write your answer here. Use real examples from your own experience..." /><button className="secondary-button feedback-button" type="button" onClick={() => submitInterviewAnswer(index, item)} disabled={Boolean(feedbackLoading[index])}>{feedbackLoading[index] ? "Reviewing answer..." : "Get Practice feedback"}</button>{feedback[index] && <div className="ai-feedback"><h4>Interview coach feedback</h4><div className="feedback-text">{feedback[index]}</div></div>}</article>)}</div>}
        </section>
        <footer className="app-footer">AI Job Copilot · Your resume and career data should be handled carefully. Practice feedback is advisory, not a hiring decision.</footer>
      </main>
    </div>
  );
}

export default App;
