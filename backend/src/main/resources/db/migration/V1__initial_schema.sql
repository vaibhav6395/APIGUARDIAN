CREATE TABLE app_users (
  id BIGSERIAL PRIMARY KEY,
  email VARCHAR(254) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  display_name VARCHAR(100) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE workspaces (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  created_by BIGINT NOT NULL REFERENCES app_users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE workspace_memberships (
  id BIGSERIAL PRIMARY KEY,
  workspace_id BIGINT NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
  user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
  role VARCHAR(30) NOT NULL CHECK (role IN ('OWNER','ADMIN','MEMBER','VIEWER')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_workspace_member UNIQUE(workspace_id,user_id)
);
CREATE INDEX idx_memberships_user ON workspace_memberships(user_id);
CREATE TABLE projects (
  id BIGSERIAL PRIMARY KEY,
  workspace_id BIGINT NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
  name VARCHAR(120) NOT NULL,
  description VARCHAR(1000),
  created_by BIGINT NOT NULL REFERENCES app_users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_projects_workspace ON projects(workspace_id);
CREATE TABLE policy_rules (
  id BIGSERIAL PRIMARY KEY,
  project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
  role_name VARCHAR(80) NOT NULL,
  resource VARCHAR(300) NOT NULL,
  action VARCHAR(30) NOT NULL,
  expected_decision VARCHAR(10) NOT NULL CHECK (expected_decision IN ('ALLOW','DENY')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_policy_rule UNIQUE(project_id,role_name,resource,action)
);
CREATE INDEX idx_policies_project ON policy_rules(project_id);
