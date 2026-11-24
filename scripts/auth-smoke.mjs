#!/usr/bin/env node
/**
 * 阶段 3 认证与越权隔离冒烟测试，零依赖（Node 18+ 原生 fetch）。
 * 用法：node scripts/auth-smoke.mjs [baseUrl]
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

async function call(method, url, body, token) {
  const headers = {};
  if (body) headers['Content-Type'] = 'application/json';
  if (token) headers['Authorization'] = `Bearer ${token}`;
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

const stamp = Date.now().toString().slice(-8);
const userA = { username: `alice_${stamp}`, password: 'Passw0rd!123' };
const userB = { username: `bob_${stamp}`, password: 'Passw0rd!123' };

console.log(`\n== 目标: ${BASE}\n`);

console.log('[1] 注册用户 A');
const regA = await call('POST', `${AUTH}/register`, userA);
check('返回 201', regA.status === 201, `实际 ${regA.status}`);
check('不返回密码字段', regA.json && !('password' in regA.json) && !('passwordHash' in regA.json));
check('用户名正确', regA.json?.username === userA.username);

console.log('[2] 重复注册');
const regDup = await call('POST', `${AUTH}/register`, userA);
check('返回 409', regDup.status === 409, `实际 ${regDup.status}`);
check('是 ProblemDetail', typeof regDup.json?.title === 'string');

console.log('[3] 注册校验：密码过短');
const regBad = await call('POST', `${AUTH}/register`, { username: `x_${stamp}`, password: '123' });
check('返回 400', regBad.status === 400, `实际 ${regBad.status}`);
check('errors 指出 password', Array.isArray(regBad.json?.errors)
  && regBad.json.errors.some((e) => e.field === 'password'));

console.log('[4] 注册校验：用户名非法字符');
const regBadName = await call('POST', `${AUTH}/register`, { username: 'bad name!', password: 'Passw0rd!123' });
check('返回 400', regBadName.status === 400, `实际 ${regBadName.status}`);

console.log('[5] 登录成功');
const loginA = await call('POST', `${AUTH}/login`, userA);
check('返回 200', loginA.status === 200, `实际 ${loginA.status}`);
check('返回 token', typeof loginA.json?.token === 'string' && loginA.json.token.split('.').length === 3);
check('tokenType 为 Bearer', loginA.json?.tokenType === 'Bearer');
check('有 expiresAt', typeof loginA.json?.expiresAt === 'string');
const tokenA = loginA.json?.token;

console.log('[6] 登录失败');
const wrongPwd = await call('POST', `${AUTH}/login`, { username: userA.username, password: 'WrongPass!999' });
const noUser = await call('POST', `${AUTH}/login`, { username: `ghost_${stamp}`, password: 'WrongPass!999' });
check('密码错误返回 401', wrongPwd.status === 401, `实际 ${wrongPwd.status}`);
check('用户不存在返回 401', noUser.status === 401, `实际 ${noUser.status}`);
check('两种失败提示一致（不泄露用户是否存在）',
  wrongPwd.json?.detail === noUser.json?.detail,
  `"${wrongPwd.json?.detail}" vs "${noUser.json?.detail}"`);

console.log('[7] 无 token 访问受保护资源');
const noToken = await call('GET', API);
check('返回 401', noToken.status === 401, `实际 ${noToken.status}`);
check('是 ProblemDetail', typeof noToken.json?.title === 'string' && noToken.json?.status === 401);
check('响应体不含内部异常堆栈', !JSON.stringify(noToken.json ?? {}).includes('Exception'));

console.log('[8] 无效 token');
const badToken = await call('GET', API, null, 'not-a-real-token');
check('返回 401', badToken.status === 401, `实际 ${badToken.status}`);

console.log('[9] 伪造签名的 token');
const parts = tokenA.split('.');
const forged = `${parts[0]}.${Buffer.from(JSON.stringify({ sub: '1', username: 'demo', exp: 9999999999 })).toString('base64url')}.${parts[2]}`;
const forgedRes = await call('GET', API, null, forged);
check('返回 401', forgedRes.status === 401, `实际 ${forgedRes.status}`);

console.log('[10] 带 token 访问');
const withToken = await call('GET', API, null, tokenA);
check('返回 200', withToken.status === 200, `实际 ${withToken.status}`);
check('items 是数组', Array.isArray(withToken.json?.items));

console.log('[11] 用户 A 创建 Todo');
const todoA = await call('POST', API, { title: 'Alice 的私有任务' }, tokenA);
check('返回 201', todoA.status === 201, `实际 ${todoA.status}`);
const todoAId = todoA.json?.id;

console.log('[12] 注册并登录用户 B');
const regB = await call('POST', `${AUTH}/register`, userB);
check('注册返回 201', regB.status === 201, `实际 ${regB.status}`);
const loginB = await call('POST', `${AUTH}/login`, userB);
check('登录返回 200', loginB.status === 200, `实际 ${loginB.status}`);
const tokenB = loginB.json?.token;

console.log('[13] 越权隔离');
const listB = await call('GET', API, null, tokenB);
check('B 的列表为空', (listB.json?.items ?? []).length === 0, `实际 ${(listB.json?.items ?? []).length} 条`);
const getAByB = await call('GET', `${API}/${todoAId}`, null, tokenB);
check('B 读 A 的任务返回 404（不是 403，避免探测存在性）',
  getAByB.status === 404, `实际 ${getAByB.status}`);
const delAByB = await call('DELETE', `${API}/${todoAId}`, null, tokenB);
check('B 删 A 的任务返回 404', delAByB.status === 404, `实际 ${delAByB.status}`);
const stillThere = await call('GET', `${API}/${todoAId}`, null, tokenA);
check('A 的任务未被删除', stillThere.status === 200, `实际 ${stillThere.status}`);

console.log('[14] 历史种子用户（{noop} 前缀）仍可登录');
const demoLogin = await call('POST', `${AUTH}/login`, { username: 'demo', password: 'demo123' });
check('返回 200', demoLogin.status === 200, `实际 ${demoLogin.status}`);

console.log(`\n结果: ${passed} passed, ${failed} failed`);
if (failed > 0) {
  console.log(`失败项: ${failures.join(' / ')}`);
  process.exit(1);
}
