-- 创建任务表
CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    task_id VARCHAR(255) UNIQUE NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    
    -- 任务基本信息
    task_type VARCHAR(50) NOT NULL, -- 'TEXT_TO_MUSIC', 'MUSIC_EDITING', 'VIDEO_SOUNDTRACK'
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED', 'CANCELLED'
    priority INTEGER NOT NULL DEFAULT 0,
    
    -- 任务参数
    prompt TEXT,
    duration INTEGER,
    source_audio_url VARCHAR(1000),
    source_video_url VARCHAR(1000),
    parameters TEXT, -- JSON格式存储其他参数
    
    -- 任务结果
    result_audio_url VARCHAR(1000),
    error_message TEXT,
    progress INTEGER DEFAULT 0,
    
    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_tasks_task_id ON tasks(task_id);
CREATE INDEX IF NOT EXISTS idx_tasks_user_id ON tasks(user_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_user_created ON tasks(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_tasks_user_status ON tasks(user_id, status);
CREATE INDEX IF NOT EXISTS idx_tasks_priority_created ON tasks(priority DESC, created_at ASC);