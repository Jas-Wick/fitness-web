/**
 * 复现评论输入/发布问题。
 * 用法：node scripts/repro-comment.mjs
 * 前置：dev server (5173) + 后端 (8080) 运行中。
 */
import puppeteer from 'puppeteer-core'
import fs from 'node:fs'
import path from 'node:path'

const EDGE = 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe'
const BASE = 'http://localhost:5173'
const OUT = path.resolve('scripts/.snapshots')
fs.mkdirSync(OUT, { recursive: true })

const sleep = (ms) => new Promise((r) => setTimeout(r, ms))

const browser = await puppeteer.launch({
  executablePath: EDGE,
  headless: 'new',
  args: ['--no-sandbox', '--disable-gpu'],
})
const page = await browser.newPage()
await page.setViewport({ width: 1280, height: 900 })

// 记录 /api/comment 请求
let commentPosts = 0
page.on('request', (req) => {
  if (req.url().includes('/api/comment') && req.method() === 'POST') commentPosts++
})

async function login() {
  await page.goto(`${BASE}/login`, { waitUntil: 'networkidle2', timeout: 20000 })
  const inputs = await page.$$('.auth-form-box .el-input__inner')
  await inputs[0].type('admin')
  await inputs[1].type('admin123')
  await page.click('.auth-form-box .submit-btn')
  await page.waitForFunction(() => location.pathname === '/home', { timeout: 15000 })
  await new Promise((r) => setTimeout(r, 1000))
}

const log = (k, v) => console.log(`[${k}]`, v)

await login()

// 去社区
await page.goto(`${BASE}/community`, { waitUntil: 'networkidle2', timeout: 20000 })
const hasPost = await page.$('.post-card')

// 无帖子则先发布一条
if (!hasPost) {
  await page.click('.ph-actions .el-button--primary')
  await page.waitForSelector('.el-dialog', { timeout: 5000 })
  await sleep(400)
  const dlgInputs = await page.$$('.el-dialog .el-input__inner')
  await dlgInputs[0].type('test-post-for-comment')
  await page.type('.el-dialog textarea', 'this is a test post.')
  await page.click('.el-dialog__footer .el-button--primary')
  await sleep(1500)
  log('发布', `commentPosts=${commentPosts}`)
}

// 打开第一条帖子的详情（评论区）
await page.waitForSelector('.post-card', { timeout: 8000 })
const commentBtns = await page.$$('.post-actions .action-btn')
await commentBtns[commentBtns.length - 1].click()
await page.waitForSelector('.el-dialog .comment-input', { timeout: 6000 })
await sleep(600)
log('弹窗', '详情已打开')

// 聚焦评论输入框
const inputSel = '.el-dialog .comment-input .el-input__inner'
const input = await page.$(inputSel)
await input.click()
await input.focus()
await sleep(200)

const focusInfo = await page.evaluate((sel) => {
  const el = document.querySelector(sel)
  const r = el?.getBoundingClientRect()
  const at = r ? document.elementFromPoint(r.x + r.width / 2, r.y + r.height / 2) : null
  return {
    activeTag: document.activeElement?.tagName,
    activeCls: (document.activeElement?.className || '').toString().slice(0, 50),
    hitTag: at?.tagName,
    hitCls: (at?.className || '').toString().slice(0, 60),
    overlays: document.querySelectorAll('.el-overlay').length,
  }
}, inputSel)
log('焦点检查', JSON.stringify(focusInfo))

// 用原生 setter + input 事件注入文本（模拟真实输入法 compositionend 提交）
const afterInsert = await page.evaluate((sel) => {
  const el = document.querySelector(sel)
  const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set
  setter.call(el, 'hello-comment-123')
  el.dispatchEvent(new Event('input', { bubbles: true }))
  return { value: el.value, activeValue: document.activeElement?.value, activeTag: document.activeElement?.tagName }
}, inputSel)
log('注入后', JSON.stringify(afterInsert))

// 按 Enter（模拟中文输入法确认候选后会触发的 keyup.enter）
await page.keyboard.press('Enter')
await sleep(1200)
log('Enter后', `commentPosts=${commentPosts}`)

// 若 Enter 无效，点「评论」按钮
if (commentPosts === 0) {
  await page.click('.el-dialog .comment-input .el-button')
  await sleep(1200)
  log('点按钮后', `commentPosts=${commentPosts}`)
}

const valueAfter = await page.evaluate((sel) => document.querySelector(sel)?.value, inputSel)
log('最终value', JSON.stringify(valueAfter))
await page.screenshot({ path: path.join(OUT, 'comment-bug.png') })

console.log(commentPosts > 0 ? '✅ 评论 POST 已到达后端' : '❌ 评论 POST 未发出')
await browser.close()
