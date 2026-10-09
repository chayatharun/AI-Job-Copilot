# AI Job Copilot – MySQL History and Interview Prep update

This bundle contains complete replacement files for the existing project. Copy the `backend` and `frontend` source files into the matching locations in your project.

## What changed
- Successful `POST /api/analyze` requests are saved to MySQL table `job_analyses`.
- `GET /api/analyses` returns recent saved analyses.
- `GET /api/analyses/{id}` returns one saved analysis.
- `POST /api/interview/questions` generates local, rule-based practice questions from a job description and detected skills.
- The dashboard now displays saved history and a functional Interview Prep section.

## Apply the update
1. Back up your existing `AI-Job-Copilot` folder.
2. Copy the files in this bundle into the same relative paths in your project, allowing replacement when asked.
3. Keep your existing `backend/src/main/resources/application.properties` and its local MySQL credentials. Do not replace it with a copy from another machine.
4. Open a terminal in `C:\projects\AI-Job-Copilot\backend` and run:
   `.\mvnw.cmd clean compile`
5. Start the backend:
   `.\mvnw.cmd spring-boot:run`
6. In another terminal, open `C:\projects\AI-Job-Copilot\frontend` and run:
   `npm run dev`
7. Open `http://localhost:5173`, analyze a PDF/DOCX resume against a job description, then check Saved job analyses and Interview Prep.

The project uses `spring.jpa.hibernate.ddl-auto=update`, so Hibernate should create the `job_analyses` table automatically when the backend starts, assuming the configured database user has table-creation privileges.

Interview Prep currently uses local rule-based templates. It does not call a paid LLM or provide personalized factual answers; treat suggested approaches as practice guidance and adapt them to your real experience.
