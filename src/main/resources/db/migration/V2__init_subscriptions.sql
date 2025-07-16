-- 创建用户订阅表
CREATE TABLE subscriptions (
    user_id INTEGER PRIMARY KEY,
    stripe_customer_id TEXT NOT NULL UNIQUE,
    current_plan TEXT NOT NULL,
    current_credit INTEGER NOT NULL,
    last_reset_at TIMESTAMPTZ,
    next_reset_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
