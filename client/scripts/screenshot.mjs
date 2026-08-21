/**
 * 取证工具：对运行中的应用在各宽度下截图 + 收集越界/溢出诊断。
 * 用法：node scripts/screenshot.mjs
 * 前置：dev server (5173) + 后端 (8080) 已在运行，登录账号 admin/admin123。
 */
import puppeteer from 'puppeteer-core'
import fs from 'node:fs'
import path from 'node:path'

const EDGE = 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe'
const BASE = 'http://localhost:5173'
const OUT = path.resolve('scripts/.snapshots')

const ROUTES = ['/home', '/training', '/exercise', '/analysis', '/food', '/body', '/community', '/profile', '/ai']
const WIDTHS = [1280, 1024, 900, 768]

const browser = await puppeteer.launch({
  executablePath: EDGE,
  headless: 'new',
  args: ['--no-sandbox', '--disable-gpu'],
})

async function login(page) {
  await page.goto(`${BASE}/login`, { waitUntil: 'networkidle2', timeout: 20000 })
  const inputs = await page.$$('.auth-form-box .el-input__inner')
  if (inputs.length < 2) throw new Error('登录表单未找到')
  await inputs[0].type('admin')
  await inputs[1].type('admin123')
  await page.click('.auth-form-box .submit-btn')
  await page.waitForFunction(() => location.pathname === '/home', { timeout: 15000 })
  await new Promise((r) => setTimeout(r, 1200))
}

function diagnostics(page) {
  return page.evaluate(() => {
    const w = window.innerWidth
    // 是否位于可滚动/裁切容器内（其溢出由容器自身消化，不构成视觉重叠）
    const insideClip = (el) => {
      let n = el.parentElement
      while (n) {
        const o = getComputedStyle(n).overflowX
        if (o === 'auto' || o === 'hidden' || o === 'scroll' || o === 'clip') return true
        n = n.parentElement
      }
      return false
    }
    const issues = []
    for (const el of document.querySelectorAll('body *')) {
      const cs = getComputedStyle(el)
      if (cs.display === 'none' || cs.visibility === 'hidden') continue
      const r = el.getBoundingClientRect()
      if (r.width === 0 && r.height === 0) continue
      const cls = (typeof el.className === 'string' ? el.className : '').slice(0, 50)
      if (el.closest('.el-overlay')) continue
      if (r.right > w + 1 && !insideClip(el)) {
        issues.push({ type: 'out-of-bounds', tag: el.tagName, cls, right: Math.round(r.right), width: Math.round(r.width) })
      }
      if (el.scrollWidth > el.clientWidth + 1 && cs.overflowX === 'visible' && !insideClip(el)) {
        issues.push({ type: 'overflow-x', tag: el.tagName, cls, sw: el.scrollWidth, cw: el.clientWidth })
      }
    }
    return issues.slice(0, 40)
  })
}

const page = await browser.newPage()
await login(page)

for (const width of WIDTHS) {
  await page.setViewport({ width, height: 900 })
  for (const route of ROUTES) {
    const issues = []
    const consoleErr = []
    const onErr = (msg) => consoleErr.push(msg.text())
    page.on('console', (m) => m.type() === 'error' && onErr(m))
    page.on('pageerror', (e) => consoleErr.push(String(e)))

    await page.goto(`${BASE}${route}`, { waitUntil: 'networkidle2', timeout: 20000 })
    // 等图表渲染
    await new Promise((r) => setTimeout(r, 1600))

    const dir = path.join(OUT, String(width))
    fs.mkdirSync(dir, { recursive: true })
    const shot = path.join(dir, route.slice(1) + '.png')
    await page.screenshot({ path: shot, fullPage: true })

    const diag = await diagnostics(page)
    issues.push(...diag)

    page.off('console', onErr)
    page.off('pageerror', onErr)

    console.log(`[${width}] ${route} → issues:${issues.length} consoleErr:${consoleErr.length}`)
    for (const it of issues) console.log('   ', JSON.stringify(it))
  }
}

await browser.close()
console.log('done →', OUT)
