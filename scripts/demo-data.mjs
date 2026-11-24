#!/usr/bin/env node
/**
 * 生成演示数据，方便在模拟器里看到非空列表。
 * 用法：node scripts/demo-data.mjs [baseUrl]
 */

const BASE = (process.argv[2] ?? 'http://localhost:8080').replace(/\/$/, '');
const USER = { username: 'alice_demo', password: 'Passw0rd123' };

const TITLES = [
  '学习 Flutter 状态管理',
  '对接 Spring Boot 接口',
  '给 Todo 加截止时间',
  '写一篇阶段总结',
];

const loginRes = await fetch(`${BASE}/api/v1/auth/login`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(USER),
});

if (!loginRes.ok) {
  console.error(`登录失败 HTTP ${loginRes.status}，请先注册 ${USER.username}`);
  process.exit(1);
}

const { token } = await loginRes.json();

for (const title of TITLES) {
  const res = await fetch(`${BASE}/api/v1/todos`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ title }),
  });
  console.log(`${res.status}  ${title}`);
}

console.log('done');
