CREATE TABLE policy_evaluations (
  id BIGSERIAL PRIMARY KEY,
  project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
  created_by BIGINT NOT NULL REFERENCES app_users(id),
  role_name VARCHAR(80) NOT NULL,
  resource VARCHAR(300) NOT NULL,
  action VARCHAR(30) NOT NULL,
  expected_decision VARCHAR(10) NOT NULL CHECK (expected_decision IN ('ALLOW','DENY')),
  observed_decision VARCHAR(10) NOT NULL CHECK (observed_decision IN ('ALLOW','DENY')),
  matched BOOLEAN NOT NULL,
  explanation VARCHAR(1000) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_evaluations_project_created ON policy_evaluations(project_id,created_at DESC);

CREATE TABLE findings (
  id BIGSERIAL PRIMARY KEY,
  project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
  evaluation_id BIGINT NOT NULL UNIQUE REFERENCES policy_evaluations(id) ON DELETE CASCADE,
  title VARCHAR(200) NOT NULL,
  severity VARCHAR(20) NOT NULL CHECK (severity IN ('CRITICAL','HIGH','MEDIUM','LOW')),
  status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN','ACKNOWLEDGED','RESOLVED')),
  details VARCHAR(1000) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_findings_project_status ON findings(project_id,status,created_at DESC);
