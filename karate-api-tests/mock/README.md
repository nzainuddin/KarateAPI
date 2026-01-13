# Users Mock (embedded and json-server)

This folder contains artifacts and instructions for running the Users mock used by tests.

Two mock options are available:

1. Embedded Java mock (no external dependency)
   - Implemented in tests by `examples.runner.UsersMockServerRunner`.
   - Starts a small HTTP server on port `3000` automatically before the test and stops it after.
   - Supports:
     - GET /users
     - GET /users/{id}
     - POST /users (validates required fields: `name`, `username`, `email`)
     - PUT /users/{id}
     - PATCH /users/{id}
     - DELETE /users/{id}
   - Persists state to `target/mock-users.json` so created/updated users survive across runs (useful for iterative testing).
   - To run the mock-backed test via Maven (direct runner):
     ```powershell
     Set-Location 'C:\Users\user\IdeaProjects\PetStoreAPI\karate-api-tests'
     mvn -Dtest="examples.runner.UsersMockServerRunner" test
     ```
   - Or use the provided Maven profile `mock-users` (convenience) which runs only the embedded mock runner:
     ```powershell
     Set-Location 'C:\Users\user\IdeaProjects\PetStoreAPI\karate-api-tests'
     mvn -Pmock-users test
     ```
   - The runner/profile sets `-Dmock.users=true` and `-Dkarate.env=local-mock` so feature files pick the local base URL automatically.

2. json-server (optional, Node.js)
   - If you prefer `json-server` (npm), use the files in this repo: `db.json` and `routes.json`.
   - Install and run:
     ```powershell
     npm install -g json-server
     Set-Location 'C:\Users\user\IdeaProjects\PetStoreAPI\karate-api-tests\mock'
     json-server --watch db.json --routes routes.json --port 3000
     ```
   - Then run the Karate feature against `http://localhost:3000` with `-Dmock.users=true`.

Notes
- The embedded mock validates payloads and supports basic query-parameter filtering on `GET /users` (e.g. `GET /users?username=Bret`).
- The embedded mock is intentionally lightweight; for advanced simulation (delays, conditional responses), consider a dedicated mock tool (WireMock, MockServer) or extend the embedded handler.
- `target/mock-users.json` is created/updated during tests; it is placed under `target/` and is not intended to be committed.
