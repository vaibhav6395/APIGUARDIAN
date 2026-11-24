CREATE TABLE test_runs (
  id BIGSERIAL PRIMARY KEY,
  project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
  created_by BIGINT NOT NULL REFERENCES app_users(id),
  total_tests INTEGER NOT NULL DEFAULT 0,
  passed_tests INTEGER NOT NULL DEFAULT 0,
  failed_tests INTEGER NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL CHECK (status IN ('COMPLETED','FAILED')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_test_runs_project_created ON test_runs(project_id,created_at DESC);
ALTER TABLE policy_evaluations ADD COLUMN run_id BIGINT REFERENCES test_runs(id) ON DELETE SET NULL;
CREATE INDEX idx_evaluations_run ON policy_evaluations(run_id);
