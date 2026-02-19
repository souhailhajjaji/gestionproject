-- V2__reset_projects.sql
-- Drop and recreate projects table to fix timestamp column issues

-- Drop existing projects table (will be recreated by JPA/Hibernate)
DROP TABLE IF EXISTS projects CASCADE;

-- Recreate projects table with correct types
CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nom VARCHAR(200) NOT NULL,
    description TEXT,
    date_debut DATE NOT NULL,
    date_fin DATE,
    responsable_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Recreate indexes
CREATE INDEX idx_projects_responsable ON projects(responsable_id);
