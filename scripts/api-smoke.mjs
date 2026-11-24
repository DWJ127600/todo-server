#!/usr/bin/env node
/**
 * 阶段 2 接口冒烟测试，零依赖（Node 18+ 原生 fetch）。
 * 阶段 3 起接口需要认证，脚本会先用种子用户 demo 登录换取 token。
 *
 * 用法：node scripts/api-smoke.mjs [baseUrl]
 * 默认 baseUrl = http://localhost:8080
 */

const BASE = (process.argv[2] ?? 'http://localhost:8080').replace(/\/$/, '');
const AUTH = `${BASE}/api/v1/auth`;
const API = `${BASE}/api/v1/todos`;

let passed = 0;
let failed = 0;
const failures = [];

function check(name, condition, extra = '') {
  if (condition) {
    passed += 1;
    console.log(`  OK   ${name}`);
  } else {
    failed += 1;
    failures.push(name);
    console.log(`  FAIL ${name} ${extra}`);
  }
}

async function call(method, url, body) {
  const headers = { Authorization: `Bearer ${TOKEN}` };
  if (body) headers['Content-Type'] = 'application/json';
  const res = await fetch(url, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });
  const text = await res.text();
  let json = null;
  try {
    json = text ? JSON.parse(text) : null;
  } catch {
    json = null;
  }
  return { status: res.status, json, headers: res.headers };
}

console.log(`\n== 目标: ${API}\n`);

const loginRes = await fetch(`${AUTH}/login`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username: 'demo', password: 'demo123' }),
});
if (!loginRes.ok) {
  console.error(`[setup] 登录失败 HTTP ${loginRes.status}。请确认 V2 迁移已执行、demo 用户存在。`);
  process.exit(1);
}
const TOKEN = (await loginRes.json()).token;
console.log('[setup] demo 已登录\n');

console.log('[1] POST 创建');
const created = await call('POST', API, {
  title: '写第一个 Todo',
  description: '验证后端 CRUD',
  dueDate: '2026-11-10T09:00:00Z',
});
check('返回 201', created.status === 201, `实际 ${created.status}`);
check('响应体 id 存在', typeof created.json?.id === 'number');
check('completed 默认 false', created.json?.completed === false);
check('有 Location 头', Boolean(created.headers.get('location')));
check('标题原样返回', created.json?.title === '写第一个 Todo');
const id = created.json?.id;

console.log('[2] GET 列表');
const list = await call('GET', API);
check('返回 200', list.status === 200, `实际 ${list.status}`);
check('items 是数组', Array.isArray(list.json?.items));
check('总数 >= 1', (list.json?.totalElements ?? 0) >= 1);
check('分页字段完整', ['page', 'size', 'totalElements', 'totalPages', 'hasNext']
  .every((k) => k in (list.json ?? {})));

console.log('[3] GET 详情');
const one = await call('GET', `${API}/${id}`);
check('返回 200', one.status === 200, `实际 ${one.status}`);
check('id 匹配', one.json?.id === id);
check('时间格式为 ISO-8601', typeof one.json?.createdAt === 'string' && one.json.createdAt.includes('T'));

console.log('[4] PATCH 标记完成');
const done = await call('PATCH', `${API}/${id}`, { completed: true });
check('返回 200', done.status === 200, `实际 ${done.status}`);
check('completed 变 true', done.json?.completed === true);
check('标题未被清空', done.json?.title === '写第一个 Todo');
check('dueDate 未被误清空', done.json?.dueDate != null, `实际 ${done.json?.dueDate}`);

console.log('[5] PATCH 改标题');
const renamed = await call('PATCH', `${API}/${id}`, { title: '改过的标题' });
check('返回 200', renamed.status === 200, `实际 ${renamed.status}`);
check('标题已更新', renamed.json?.title === '改过的标题');
check('completed 保持 true', renamed.json?.completed === true);

console.log('[6] 按状态筛选');
const filtered = await call('GET', `${API}?completed=true&page=0&size=10`);
check('返回 200', filtered.status === 200);
check('结果全为已完成', (filtered.json?.items ?? []).every((t) => t.completed === true));

console.log('[7] 校验失败：空标题');
const invalid = await call('POST', API, { title: '   ' });
check('返回 400', invalid.status === 400, `实际 ${invalid.status}`);
check('是 ProblemDetail', typeof invalid.json?.title === 'string' && typeof invalid.json?.detail === 'string');
check('errors 指出 title', Array.isArray(invalid.json?.errors)
  && invalid.json.errors.some((e) => e.field === 'title'));

console.log('[8] 不存在：GET 999999');
const missing = await call('GET', `${API}/999999`);
check('返回 404', missing.status === 404, `实际 ${missing.status}`);
check('detail 包含 id', String(missing.json?.detail ?? '').includes('999999'));

console.log('[9] DELETE');
const deleted = await call('DELETE', `${API}/${id}`);
check('返回 204', deleted.status === 204, `实际 ${deleted.status}`);

console.log('[10] 删除后再查');
const gone = await call('GET', `${API}/${id}`);
check('返回 404', gone.status === 404, `实际 ${gone.status}`);

console.log(`\n结果: ${passed} passed, ${failed} failed`);
if (failed > 0) {
  console.log(`失败项: ${failures.join(' / ')}`);
  process.exit(1);
}
