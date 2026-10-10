# AI Job Copilot — Python backend

This backend replaces the Java/Spring Boot API with Flask while keeping the existing React/Vite frontend API routes compatible. It uses local rule-based logic and does not require paid AI APIs.

## Features
- PDF and DOCX resume text extraction
- Rule-based skill comparison and match score
- Saved analysis history
- Application tracker (create, update status, delete)
- Interview question generation
- Rule-based interview answer feedback
- Rule-based skill recommendations
- SQLite works out of the box; MySQL can be configured using `DATABASE_URL`

## Windows setup
Run PowerShell commands from the project root:

```powershell
cd C:\projects\AI-Job-Copilot
py -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install --upgrade pip
pip install -r .\backend-python\requirements.txt
Copy-Item .\backend-python\.env.example .\backend-python\.env
python .\backend-python\app.py
```

The API runs at `http://localhost:8080`. Check `http://localhost:8080/api/health`.

If PowerShell blocks activation, run `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass` in that terminal, then activate again.

## MySQL configuration
Create/use a MySQL database named `jobcopilot`. In `backend-python\.env`, set:

```env
DATABASE_URL=mysql+pymysql://YOUR_USER:YOUR_PASSWORD@localhost:3306/jobcopilot?charset=utf8mb4
PORT=8080
```

Use your own credentials locally. Do not commit `.env` or paste credentials into chat. Tables are created automatically on first run. If your password contains reserved URL characters, URL-encode them.

## Frontend
The existing frontend uses `http://localhost:8080`, so no API URL change is needed. Start it in a second terminal:

```powershell
cd C:\projects\AI-Job-Copilot\frontend
npm install
npm run dev
```

Open the URL Vite prints (normally `http://localhost:5173`).

## API routes
- `GET /api/health`
- `GET /api/analyses`
- `POST /api/analyze` (multipart: `resume`, `jobDescription`)
- `GET /api/applications`
- `POST /api/applications`
- `PATCH /api/applications/<id>/status`
- `DELETE /api/applications/<id>`
- `GET /api/ai/status`
- `POST /api/ai/recommendations`
- `POST /api/interview/questions`
- `POST /api/ai/interview-feedback`

## Important migration notes
- This folder is provided as a separate `backend-python` directory so you can test it without deleting the currently committed Java backend.
- The default SQLite database is only for local smoke testing. Set `DATABASE_URL` in `backend-python\.env` to use MySQL.
- After testing all screens, remove the old Java `backend` folder from Git and promote this folder to the final `backend` directory (or keep the name and update your README). Do not delete the Java backend until the Python backend has been tested.
- Match scores are a keyword-based estimate, not an AI prediction of hiring success.
