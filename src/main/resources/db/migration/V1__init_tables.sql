-- 创建用户表
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    clerk_id VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建用户订阅表
CREATE TABLE subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL, -- 去掉外键约束
    stripe_customer_id VARCHAR(255) NOT NULL,
    stripe_subscription_id VARCHAR(255) UNIQUE, -- free plan时为null

    -- 订阅计划信息
    plan_type VARCHAR(20) NOT NULL DEFAULT 'FREE', -- 'FREE', 'PRO', 'PREMIUM'
    billing_cycle VARCHAR(20), -- 'MONTHLY', 'YEARLY', null for free
    stripe_product_id VARCHAR(255), -- 对应stripe产品ID
    stripe_price_id VARCHAR(255), -- 对应stripe价格ID

    -- 订阅状态
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE', 'CANCELLED', 'EXPIRED'
    cancel_at_period_end BOOLEAN DEFAULT FALSE, -- 是否在周期结束时取消

    -- 时间信息
    current_period_start TIMESTAMP, -- 当前周期开始时间
    current_period_end TIMESTAMP, -- 当前周期结束时间
    next_reset_time TIMESTAMP, -- 下次积分重置时间

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- 约束
    UNIQUE(user_id) -- 一个用户只能有一个订阅记录
);

-- 创建用户积分表
CREATE TABLE credits (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL, -- 去掉外键约束

    -- 积分数量
    permanent_credits INTEGER NOT NULL DEFAULT 0, -- 永久积分
    renewable_credits INTEGER NOT NULL DEFAULT 0, -- 可重置积分当前余额

    -- 重置相关信息
    last_reset_time TIMESTAMP, -- 上次重置时间

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- 约束
    UNIQUE(user_id) -- 一个用户只能有一条积分记录
);

-- 创建用户积分变更记录表
CREATE TABLE credit_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL, -- 去掉外键约束

    -- 交易信息
    transaction_type VARCHAR(20) NOT NULL, -- 'CONSUME', 'PURCHASE', 'REWARD', 'RESET'
    credit_type VARCHAR(20) NOT NULL, -- 'PERMANENT', 'RENEWABLE'
    amount INTEGER NOT NULL, -- 变动数量，消耗为负数
    balance_after INTEGER NOT NULL, -- 变动后余额

    -- 消耗相关信息
    task_type VARCHAR(50), -- 消耗时的任务类型
    trigger_source VARCHAR(20), -- 'UI', 'API'
    description TEXT, -- 描述信息

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Webhook事件记录表 (用于重试和调试)
CREATE TABLE webhook_events (
    id BIGSERIAL PRIMARY KEY,
    stripe_event_id VARCHAR(255) UNIQUE NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    processed BOOLEAN DEFAULT FALSE,
    retry_count INTEGER DEFAULT 0,
    error_message TEXT,
    event_data JSONB, -- 存储完整的webhook数据

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
);

-- 创建索引
-- users 表索引
CREATE INDEX idx_users_clerk_id ON users(clerk_id);
CREATE INDEX idx_users_email ON users(email);

-- subscriptions 表索引
CREATE INDEX idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX idx_subscriptions_stripe_customer ON subscriptions(stripe_customer_id);
CREATE INDEX idx_subscriptions_stripe_subscription ON subscriptions(stripe_subscription_id);

-- credits 表索引
CREATE INDEX idx_credits_user_id ON credits(user_id);

-- credit_transactions 表索引
CREATE INDEX idx_credit_transactions_user_id ON credit_transactions(user_id);
CREATE INDEX idx_credit_transactions_user_time ON credit_transactions(user_id, created_at);
CREATE INDEX idx_credit_transactions_type ON credit_transactions(transaction_type);

-- webhook_events 表索引
CREATE INDEX idx_webhook_events_stripe_event ON webhook_events(stripe_event_id);
CREATE INDEX idx_webhook_events_processed ON webhook_events(processed);
CREATE INDEX idx_webhook_events_event_type ON webhook_events(event_type);
