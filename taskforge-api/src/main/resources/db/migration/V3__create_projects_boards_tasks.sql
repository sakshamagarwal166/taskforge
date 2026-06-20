CREATE TABLE projects (
    id          UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id   UUID         NOT NULL REFERENCES tenants(id),
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    project_key VARCHAR(10)  NOT NULL,
    owner_id    UUID         NOT NULL REFERENCES users(id),
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_projects_tenant_key UNIQUE (tenant_id, project_key)
);

CREATE TABLE board_columns (
    id         UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID        NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name       VARCHAR(50) NOT NULL,
    position   INT         NOT NULL,
    color      VARCHAR(7),
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tasks (
    id          UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id   UUID         NOT NULL REFERENCES tenants(id),
    project_id  UUID         NOT NULL REFERENCES projects(id),
    column_id   UUID         NOT NULL REFERENCES board_columns(id),
    task_number INT          NOT NULL,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    priority    VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    assignee_id UUID         REFERENCES users(id),
    reporter_id UUID         NOT NULL REFERENCES users(id),
    position    INT          NOT NULL DEFAULT 0,
    due_date    DATE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_tasks_project_number UNIQUE (project_id, task_number)
);

CREATE TABLE task_comments (
    id         UUID      PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id  UUID      NOT NULL REFERENCES tenants(id),
    task_id    UUID      NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    author_id  UUID      NOT NULL REFERENCES users(id),
    content    TEXT      NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_projects_tenant_id    ON projects(tenant_id);
CREATE INDEX idx_tasks_tenant_id       ON tasks(tenant_id);
CREATE INDEX idx_tasks_column_id       ON tasks(column_id);
CREATE INDEX idx_tasks_assignee_id     ON tasks(assignee_id);
CREATE INDEX idx_task_comments_task_id ON task_comments(task_id);
