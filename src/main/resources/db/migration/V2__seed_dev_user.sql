-- 阶段 2 的接口以固定 userId=1 调用，需要一条用户记录满足 todos.user_id 外键。
-- {noop} 是 Spring Security DelegatingPasswordEncoder 的"明文"前缀，仅限本地开发。
-- 阶段 3 接入注册接口后，新用户一律使用 bcrypt，这条种子数据应改为 bcrypt 或不入库。
INSERT INTO users (id, username, password_hash, created_at)
VALUES (1, 'demo', '{noop}demo123', CURRENT_TIMESTAMP);
