# Product requirements

## Users
- **Workspace owner:** manages organization members and projects.
- **Security engineer:** imports API contracts, defines authorization expectations and runs tests.
- **Developer:** reviews findings and records remediation.
- **Viewer:** reads dashboards and reports.

## Problem
API authentication proves who a caller is; authorization determines what that caller may do. Regressions in object-level or function-level authorization can expose another user's data or privileged operations.

## MVP workflow
1. Create a workspace and project.
2. Import or select an OpenAPI contract.
3. Define test personas and expected endpoint outcomes.
4. Execute only against an allowlisted, explicitly authorized environment.
5. Store request metadata, response status and policy expectation as evidence.
6. Review findings, assign owners and compare runs.

## MVP acceptance criteria
- A developer can run the demo locally with Docker Compose.
- The demo API contains at least two roles and owner-scoped resources.
- The test runner exercises only the bundled local demo API by default.
- A policy mismatch creates a finding with reproducible evidence.
- A passing run and a failing run can be compared.
- No secrets or real personal data are required for the demo.

## Out of scope for the first release
Arbitrary internet scanning, exploit payload generation, production credential storage, automatic destructive remediation, and claims of full compliance certification.
