/**
 * 全部动作生成 SVG 动作剪影，输出到 client/public/exercises（随仓库提交），
 * 并生成 update_exercise_images_svg.sql（image_url = /exercises/{slug}.svg）。
 * 用法：node svg-only.mjs
 */
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const OUT_DIR = path.resolve(__dirname, '../../client/public/exercises')
fs.mkdirSync(OUT_DIR, { recursive: true })

const EXERCISES = [
  [1, '胸部', 'barbell-bench-press'],
  [2, '胸部', 'incline-barbell-bench-press'],
  [3, '胸部', 'dumbbell-bench-press'],
  [4, '胸部', 'incline-dumbbell-bench-press'],
  [5, '胸部', 'dumbbell-fly'],
  [6, '胸部', 'cable-crossover'],
  [7, '胸部', 'dips'],
  [8, '胸部', 'push-up'],
  [9, '背部', 'pull-up'],
  [10, '背部', 'lat-pulldown'],
  [11, '背部', 'seated-cable-row'],
  [12, '背部', 'barbell-bent-over-row'],
  [13, '背部', 'one-arm-dumbbell-row'],
  [14, '背部', 'deadlift'],
  [15, '背部', 'straight-arm-pulldown'],
  [16, '肩部', 'dumbbell-shoulder-press'],
  [17, '肩部', 'barbell-overhead-press'],
  [18, '肩部', 'dumbbell-lateral-raise'],
  [19, '肩部', 'dumbbell-front-raise'],
  [20, '肩部', 'bent-over-lateral-raise'],
  [21, '肩部', 'face-pull'],
  [22, '腿部', 'barbell-squat'],
  [23, '腿部', 'front-squat'],
  [24, '腿部', 'lunge'],
  [25, '腿部', 'leg-press'],
  [26, '腿部', 'leg-extension'],
  [27, '腿部', 'lying-leg-curl'],
  [28, '腿部', 'standing-calf-raise'],
  [29, '腿部', 'romanian-deadlift'],
  [30, '手臂', 'dumbbell-curl'],
  [31, '手臂', 'barbell-curl'],
  [32, '手臂', 'hammer-curl'],
  [33, '手臂', 'cable-triceps-pushdown'],
  [34, '手臂', 'skull-crusher'],
  [35, '手臂', 'close-grip-bench-press'],
  [36, '核心', 'plank'],
  [37, '核心', 'crunch'],
  [38, '核心', 'reverse-crunch'],
  [39, '核心', 'russian-twist'],
  [40, '核心', 'mountain-climber'],
  [41, '核心', 'hanging-leg-raise'],
  [42, '核心', 'side-plank'],
]

function makeSvg(part, file) {
  const colors = { 胸部: '#f97316', 背部: '#3b82f6', 肩部: '#8b5cf6', 腿部: '#16a34a', 手臂: '#f59e0b', 核心: '#ef4444' }
  const color = colors[part] || '#f97316'
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="320" height="200" viewBox="0 0 320 200">
  <rect width="320" height="200" rx="8" fill="#f3f4f6"/>
  <circle cx="160" cy="82" r="26" fill="${color}" opacity="0.85"/>
  <rect x="132" y="108" width="56" height="66" rx="14" fill="${color}" opacity="0.85"/>
  <rect x="102" y="118" width="24" height="52" rx="11" fill="${color}" opacity="0.6"/>
  <rect x="194" y="118" width="24" height="52" rx="11" fill="${color}" opacity="0.6"/>
  <rect x="112" y="52" width="15" height="58" rx="7" fill="${color}" opacity="0.55"/>
  <rect x="193" y="52" width="15" height="58" rx="7" fill="${color}" opacity="0.55"/>
  <text x="160" y="186" text-anchor="middle" font-size="13" fill="#6b7280">${part}</text>
</svg>`
  fs.writeFileSync(file, svg)
}

// 生成全部 SVG（文件名 {slug}.svg，image_url = /exercises/{slug}.svg）
for (const [, part, slug] of EXERCISES) {
  makeSvg(part, path.join(OUT_DIR, `${slug}.svg`))
}

// 生成转换 SQL：把既有 image_url（/uploads/exercises/{id}_{slug}.svg）统一改为 /exercises/{slug}.svg
// 全新部署由 DataInitializer 直接播种 /exercises/{slug}.svg，此 SQL 对已存在 DB 生效
const sql = `UPDATE t_exercise SET image_url = CONCAT('/exercises/', SUBSTRING_INDEX(SUBSTRING_INDEX(image_url, '/', -1), '_', -1)) WHERE image_url LIKE '/uploads/exercises/%';\n`
fs.writeFileSync(path.resolve(__dirname, 'update_exercise_images_svg.sql'), sql)

console.log(`已生成 ${EXERCISES.length} 个 SVG → client/public/exercises/；SQL → update_exercise_images_svg.sql`)
