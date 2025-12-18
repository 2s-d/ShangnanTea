/**
 * 安全清理 UI-DEV 伪代码脚本（v0.1）
 *
 * 目标：
 * - 只做“高把握、可复制、绝不引入新问题”的机械替换
 * - 不确定的地方不自动改，只输出 TODO 清单，交给人工处理
 *
 * 支持：
 * - --check：只输出报告，不写文件
 * - --write：写入文件
 * - --path=src/views（可选）：限制扫描路径，默认 src
 *
 * 当前支持的确定性规则（v0.1）：
 * 1) 删除成对标记块：/* UI-DEV-START *\/ ... /* UI-DEV-END *\/
 * 2) 删除 isDevMode 导入行：import { isDevMode } from '@/utils/devUtils'
 * 3) 删除单独出现的 isDevMode()（若仍存在则记录 TODO，不自动改）
 *
 * 用法示例：
 * - node tools/safe-ui-dev-clean.js --check --path=src
 * - node tools/safe-ui-dev-clean.js --write --path=src/views
 */

const fs = require('fs')
const path = require('path')
const glob = require('glob')
const { spawnSync } = require('child_process')

function parseArgs(argv) {
    const args = {
        write: false,
        check: false,
        prodCheck: false,
        scanPath: 'src',
        out: '',
        verify: false,
        rollbackOnError: false,
        log: ''
    }

    argv.forEach((arg) => {
        if (arg === '--write') {
            args.write = true
        }
        if (arg === '--check') {
            args.check = true
        }
        if (arg === '--prod-check') {
            args.prodCheck = true
        }
        if (arg === '--verify') {
            args.verify = true
        }
        if (arg === '--rollback-on-error') {
            args.rollbackOnError = true
        }
        if (arg.startsWith('--path=')) {
            args.scanPath = arg.slice('--path='.length).trim() || 'src'
        }
        if (arg.startsWith('--out=')) {
            args.out = arg.slice('--out='.length).trim() || ''
        }
        if (arg.startsWith('--log=')) {
            args.log = arg.slice('--log='.length).trim() || ''
        }
    })

    if (!args.write && !args.check) {
        // 默认安全：只检查
        args.check = true
    }

    if (args.prodCheck) {
        // 生产形态检查只读，不写文件；不依赖 check/write 开关
        args.write = false
        args.check = false
        args.verify = false
        args.rollbackOnError = false
    }

    if (args.write && args.check) {
        // 两者同时指定时，以 write 为准，但仍输出报告
        args.check = true
    }

    // verify 只能在 write 时启用（否则没有意义）
    if (args.verify && !args.write) {
        args.verify = false
    }

    return args
}

function toPosixPath(p) {
    return p.replace(/\\/g, '/')
}

function ensureDirForFile(filePath) {
    const dir = path.dirname(filePath)
    if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, { recursive: true })
    }
}

function writeTodoMarkdown(outFile, report, args) {
    const lines = []
    lines.push(`# safe-ui-dev-clean 报告`)
    lines.push(``)
    lines.push(`- mode: ${args.prodCheck ? 'PROD-CHECK' : (args.write ? 'WRITE' : 'CHECK')}`)
    lines.push(`- scanPath: \`${toPosixPath(args.scanPath)}\``)
    lines.push(`- scanned: ${report.scanned}`)
    lines.push(`- changedFiles: ${report.changedFiles.length}`)
    lines.push(`- todos: ${report.todos.length}`)
    if (args.write) {
        lines.push(`- verify: ${args.verify ? 'ON' : 'OFF'}`)
        lines.push(`- rollbackOnError: ${args.rollbackOnError ? 'ON' : 'OFF'}`)
    }
    if (report.verify) {
        lines.push(`- verifyExitCode: ${report.verify.exitCode}`)
        lines.push(`- verifyHasError: ${report.verify.hasError ? 'YES' : 'NO'}`)
        if (report.verify.hasError) {
            lines.push(`- rollbackApplied: ${report.verify.rollbackApplied ? 'YES' : 'NO'}`)
        }
    }
    lines.push(``)

    if (report.changedFiles.length) {
        lines.push(`## changedFiles`)
        lines.push(``)
        report.changedFiles.forEach((f) => lines.push(`- \`${toPosixPath(f)}\``))
        lines.push(``)
    }

    if (report.todos.length) {
        lines.push(`## TODO 清单（脚本未自动修改）`)
        lines.push(``)
        // TODO 类型汇总（帮助按类别批量人工修复/迭代脚本）
        const byType = new Map()
        report.todos.forEach((t) => {
            byType.set(t.type, (byType.get(t.type) || 0) + 1)
        })
        lines.push(`### TODO 类型汇总`)
        lines.push(``)
        Array.from(byType.keys()).sort().forEach((type) => {
            lines.push(`- **${type}**: ${byType.get(type)}`)
        })
        lines.push(``)
        // 按文件分组
        const byFile = new Map()
        report.todos.forEach((t) => {
            if (!byFile.has(t.file)) {
                byFile.set(t.file, [])
            }
            byFile.get(t.file).push({ type: t.type, message: t.message })
        })
        Array.from(byFile.keys()).sort().forEach((file) => {
            lines.push(`### \`${toPosixPath(file)}\``)
            lines.push(``)
            const items = byFile.get(file) || []
            items.forEach((it) => lines.push(`- **${it.type}**: ${it.message}`))
            lines.push(``)
        })
    }

    if (report.prodCheck) {
        lines.push(`## 生产形态检查（prod-check）`)
        lines.push(``)
        lines.push(`### 指标汇总（命中次数）`)
        lines.push(``)
        Object.keys(report.prodCheck.summary).sort().forEach((k) => {
            lines.push(`- **${k}**: ${report.prodCheck.summary[k]}`)
        })
        lines.push(``)
        lines.push(`### 命中文件清单（top 50）`)
        lines.push(``)
        report.prodCheck.hits.slice(0, 50).forEach((h) => {
            lines.push(`- \`${toPosixPath(h.file)}\` (${h.total}): ${h.items.map((i) => `${i.key}=${i.count}`).join(', ')}`)
        })
        if (report.prodCheck.hits.length > 50) {
            lines.push(`- ... +${report.prodCheck.hits.length - 50} more`)
        }
        lines.push(``)
    }

    ensureDirForFile(outFile)
    fs.writeFileSync(outFile, lines.join('\n'), 'utf-8')
}

function appendRunLogMarkdown(logFile, report, args) {
    if (!logFile) {
        return
    }

    const ts = new Date().toISOString()
    const lines = []
    lines.push(`## ${ts}`)
    lines.push(``)
    lines.push(`- mode: ${args.prodCheck ? 'PROD-CHECK' : (args.write ? 'WRITE' : 'CHECK')}`)
    lines.push(`- scanPath: \`${toPosixPath(args.scanPath)}\``)
    lines.push(`- scanned: ${report.scanned}`)
    lines.push(`- changedFiles: ${report.changedFiles.length}`)
    lines.push(`- todos: ${report.todos.length}`)
    if (args.write) {
        lines.push(`- verify: ${args.verify ? 'ON' : 'OFF'}`)
        lines.push(`- rollbackOnError: ${args.rollbackOnError ? 'ON' : 'OFF'}`)
    }
    if (report.verify) {
        lines.push(`- verifyExitCode: ${report.verify.exitCode}`)
        lines.push(`- verifyHasError: ${report.verify.hasError ? 'YES' : 'NO'}`)
        if (report.verify.hasError) {
            lines.push(`- rollbackApplied: ${report.verify.rollbackApplied ? 'YES' : 'NO'}`)
        }
    }
    if (report.changedFiles.length) {
        lines.push(``)
        lines.push(`### changedFiles`)
        lines.push(``)
        report.changedFiles.forEach((f) => lines.push(`- \`${toPosixPath(f)}\``))
    }
    if (report.todos.length) {
        lines.push(``)
        // 类型汇总
        const byType = new Map()
        report.todos.forEach((t) => {
            byType.set(t.type, (byType.get(t.type) || 0) + 1)
        })
        lines.push(`### todosByType`)
        lines.push(``)
        Array.from(byType.keys()).sort().forEach((type) => {
            lines.push(`- **${type}**: ${byType.get(type)}`)
        })
        lines.push(``)
        lines.push(`### todos（top 20）`)
        lines.push(``)
        report.todos.slice(0, 20).forEach((t) => lines.push(`- \`${toPosixPath(t.file)}\` **${t.type}**: ${t.message}`))
        if (report.todos.length > 20) {
            lines.push(`- ... +${report.todos.length - 20} more`)
        }
    }

    if (report.prodCheck) {
        lines.push(``)
        lines.push(`### prodCheckSummary`)
        lines.push(``)
        Object.keys(report.prodCheck.summary).sort().forEach((k) => {
            lines.push(`- **${k}**: ${report.prodCheck.summary[k]}`)
        })
        lines.push(``)
    }
    lines.push(``)

    ensureDirForFile(logFile)
    const exists = fs.existsSync(logFile)
    if (!exists) {
        fs.writeFileSync(logFile, `# safe-ui-dev-clean 运行日志\n\n`, 'utf-8')
    }
    fs.appendFileSync(logFile, lines.join('\n'), 'utf-8')
}

function listTargetFiles(scanPath) {
    const patterns = [
        `${scanPath.replace(/\\/g, '/')}/**/*.vue`,
        `${scanPath.replace(/\\/g, '/')}/**/*.js`
    ]

    const files = []
    patterns.forEach((pattern) => {
        files.push(...glob.sync(pattern, { ignore: ['**/node_modules/**', '**/dist/**'] }))
    })

    return [...new Set(files)]
}

function uiDevBlockLooksSafeToRemove(blockContent) {
    // 极度保守：只要出现“定义/声明/导出”，就认为不安全（脚本不自动删）
    // 这些很可能定义了后续模板/逻辑会用到的变量，一删就会 no-undef
    const unsafePatterns = [
        // import 也属于“声明”，一旦删掉会导致 ref/computed/useRouter/... 等变成 no-undef
        /\bimport\b/,
        /\bconst\b/,
        /\blet\b/,
        /\bvar\b/,
        /\bfunction\b/,
        /\bclass\b/,
        /\bexport\b/,
        // 进一步保守：出现赋值/箭头函数时也不自动删（可能在 UI-DEV 块内构造数据/函数）
        /=>/,
        /[^=!<>]=[^=]/ // 尽量排除 ==/===/!=/<=/>= 等比较
    ]
    return !unsafePatterns.some((re) => re.test(blockContent))
}

function classifyUiDevBlockUnsafeReason(blockInner) {
    // 按“最容易引入新问题”的优先级分类，方便人工按类处理、反向升级脚本规则
    if (/\bimport\b/.test(blockInner)) {
        return { type: 'TODO-IMPORT', message: 'UI-DEV 块内包含 import，删除会导致依赖缺失（no-undef），脚本不自动删除' }
    }
    if (/\bcomponents\s*:\s*\{/.test(blockInner)) {
        return {
            type: 'TODO-COMPONENTS-REGISTER',
            message: 'UI-DEV 块内包含 components 注册，删除可能导致组件未注册，脚本不自动删除'
        }
    }
    if (/\bexport\b/.test(blockInner)) {
        return { type: 'TODO-EXPORT', message: 'UI-DEV 块内包含 export，脚本不自动删除（避免影响模块导出）' }
    }
    if (/\bconst\b|\blet\b|\bvar\b|\bfunction\b|\bclass\b/.test(blockInner)) {
        return { type: 'TODO-DECLARATION', message: 'UI-DEV 块内包含变量/函数/类声明，脚本不自动删除（避免引入 no-undef）' }
    }
    if (/=>/.test(blockInner) || /[^=!<>]=[^=]/.test(blockInner)) {
        return {
            type: 'TODO-ASSIGNMENT',
            message: 'UI-DEV 块内包含赋值/箭头函数，疑似构造 mock 数据或流程，脚本不自动删除'
        }
    }
    return { type: 'TODO-UNSAFE', message: 'UI-DEV 块疑似不安全，脚本不自动删除（请人工确认）' }
}

function removeUiDevBlocks(content, filePath, report) {
    const blockStart = '/* UI-DEV-START */'
    const blockEnd = '/* UI-DEV-END */'

    let changed = false
    let out = ''
    let cursor = 0

    while (cursor < content.length) {
        const startIdx = content.indexOf(blockStart, cursor)
        if (startIdx === -1) {
            out += content.slice(cursor)
            break
        }

        // 先拼接 start 之前的内容
        out += content.slice(cursor, startIdx)

        const endIdx = content.indexOf(blockEnd, startIdx + blockStart.length)
        if (endIdx === -1) {
            // 没有找到结束标记：保留剩余内容并退出（不做破坏性修改）
            out += content.slice(startIdx)
            break
        }

        const blockInner = content.slice(startIdx + blockStart.length, endIdx)
        const afterEnd = endIdx + blockEnd.length

        if (!uiDevBlockLooksSafeToRemove(blockInner)) {
            const reason = classifyUiDevBlockUnsafeReason(blockInner)
            report.todos.push({ file: filePath, type: reason.type, message: reason.message })
            // 不删除：把整段块原样保留
            out += content.slice(startIdx, afterEnd)
        } else {
            // 安全删除：跳过该块
            changed = true
        }

        // 推进游标到 blockEnd 后
        cursor = afterEnd
    }

    return { content: out, changed }
}

function removeIsDevModeImportLine(content) {
    const patterns = [
        /(\r?\n)?import\s+\{\s*isDevMode\s*\}\s+from\s+['"]@\/utils\/devUtils['"]\s*;?\s*(\r?\n)?/g
    ]

    let changed = false
    let out = content
    patterns.forEach((re) => {
        const next = out.replace(re, '\n')
        if (next !== out) {
            changed = true
            out = next
        }
    })

    return { content: out, changed }
}

function hasIsDevModeCall(content) {
    return /\bisDevMode\s*\(/.test(content)
}

function runEslintOnFiles(files) {
    if (!files || files.length === 0) {
        return { exitCode: 0, output: '', hasError: false }
    }

    const eslintBin = process.platform === 'win32' ? 'node_modules\\.bin\\eslint.cmd' : 'node_modules/.bin/eslint'
    const args = ['--ext', '.js,.vue', ...files]
    const res = spawnSync(eslintBin, args, { encoding: 'utf-8', shell: true })
    const output = `${res.stdout || ''}${res.stderr || ''}`.trim()
    const exitCode = typeof res.status === 'number' ? res.status : 1
    // eslint exitCode==1：存在 lint 问题（含 error / warning，取决于规则）
    // 我们只把“出现 error”视为需要回滚（warning 不回滚）
    const hasError = /\berror\b/i.test(output) && /\bproblems\b/i.test(output)
    return { exitCode, output, hasError }
}

function rollbackFilesByGitCheckout(files) {
    if (!files || files.length === 0) {
        return { ok: true, output: '' }
    }
    const cmd = process.platform === 'win32'
        ? `git checkout -- ${files.map((f) => `"${f}"`).join(' ')}`
        : `git checkout -- ${files.map((f) => `'${f}'`).join(' ')}`
    const res = spawnSync(cmd, { encoding: 'utf-8', shell: true })
    const output = `${res.stdout || ''}${res.stderr || ''}`.trim()
    const ok = res.status === 0
    return { ok, output }
}

function countRegexMatches(text, regex) {
    // regex should have /g
    const m = text.match(regex)
    return m ? m.length : 0
}

function runProdCheck(files) {
    // A. 结构性“生产形态”检查指标（可量化、目标为 0）
    const rules = [
        { key: 'UI_DEV_MARKERS', regex: /\/\*\s*UI-DEV-(START|END)\s*\*\//g },
        { key: 'IS_DEVMODE_CALL', regex: /\bisDevMode\s*\(/g },
        { key: 'DEVUTILS_IMPORT', regex: /@\/utils\/devUtils/g },
        { key: 'TODO_SCRIPT_MARK', regex: /\bTODO-SCRIPT\b/g },
        { key: 'SET_TIMEOUT', regex: /\bsetTimeout\s*\(/g },
        { key: 'GENERATE_MOCK', regex: /\bgenerateMock[A-Za-z0-9_]*\s*\(/g },
        { key: 'MOCK_IMAGES', regex: /\/mock-images\//g },
        { key: 'DEV_TOKEN', regex: /\bdev_token_|mock_token_|ui_dev_token_/g }
    ]

    const summary = {}
    rules.forEach((r) => { summary[r.key] = 0 })

    const hits = []
    files.forEach((file) => {
        const raw = fs.readFileSync(file, 'utf-8')
        const items = []
        let total = 0
        rules.forEach((r) => {
            const c = countRegexMatches(raw, r.regex)
            if (c > 0) {
                items.push({ key: r.key, count: c })
                summary[r.key] += c
                total += c
            }
        })
        if (total > 0) {
            hits.push({ file, total, items })
        }
    })

    hits.sort((a, b) => b.total - a.total)
    return { summary, hits }
}

function main() {
    const args = parseArgs(process.argv.slice(2))
    console.log(`[safe-ui-dev-clean] preparing file list... (scanPath=${args.scanPath})`)
    const files = listTargetFiles(args.scanPath)
    console.log(`[safe-ui-dev-clean] file list ready: ${files.length} files`)

    const report = {
        scanned: files.length,
        changedFiles: [],
        todos: [],
        verify: null,
        prodCheck: null
    }

    if (args.prodCheck) {
        const pc = runProdCheck(files)
        report.prodCheck = pc
        console.log(`\n[safe-ui-dev-clean] mode=PROD-CHECK scanPath=${args.scanPath}`)
        Object.keys(pc.summary).sort().forEach((k) => {
            console.log(`- ${k}: ${pc.summary[k]}`)
        })
        if (args.out) {
            writeTodoMarkdown(args.out, report, args)
            console.log(`- report written: ${args.out}`)
        }
        if (args.log) {
            appendRunLogMarkdown(args.log, report, args)
            console.log(`- log appended: ${args.log}`)
        }
        // prod-check 的退出码：只要有任何命中就返回 2（提醒仍未完全生产化）
        const anyHit = Object.values(pc.summary).some((v) => v > 0)
        if (anyHit) {
            process.exitCode = 2
        }
        return
    }

    files.forEach((file) => {
        // 进度提示：避免大项目扫描时“看起来卡住”
        if (report.scanned > 200 && report.changedFiles.length % 200 === 0 && report.changedFiles.length > 0) {
            console.log(`[safe-ui-dev-clean] progress... changedFiles=${report.changedFiles.length}, todos=${report.todos.length}`)
        }
        const raw = fs.readFileSync(file, 'utf-8')
        let next = raw
        let changed = false

        const r1 = removeUiDevBlocks(next, file, report)
        next = r1.content
        changed = changed || r1.changed

        // 只有当文件内不再存在 isDevMode() 调用时，才删除 import（避免删掉 import 后出现运行/编译问题）
        if (!hasIsDevModeCall(next)) {
            const r2 = removeIsDevModeImportLine(next)
            next = r2.content
            changed = changed || r2.changed
        } else {
            report.todos.push({
                file,
                type: 'TODO-SCRIPT',
                message: '仍存在 isDevMode() 调用，脚本 v0.1 不自动删除 import/分支（请人工处理或后续升级脚本）'
            })
        }

        if (changed) {
            report.changedFiles.push(file)
            if (args.write) {
                fs.writeFileSync(file, next, 'utf-8')
            }
        }
    })

    // 输出报告
    const mode = args.write ? 'WRITE' : 'CHECK'
    console.log(`\n[safe-ui-dev-clean] mode=${mode} scanPath=${args.scanPath}`)
    console.log(`- scanned: ${report.scanned}`)
    console.log(`- changedFiles: ${report.changedFiles.length}`)
    if (report.changedFiles.length) {
        report.changedFiles.slice(0, 50).forEach((f) => console.log(`  - ${f}`))
        if (report.changedFiles.length > 50) {
            console.log(`  ... +${report.changedFiles.length - 50} more`)
        }
    }
    console.log(`- todos: ${report.todos.length}`)
    if (report.todos.length) {
        report.todos.slice(0, 50).forEach((t) => console.log(`  - ${t.file}: ${t.message}`))
        if (report.todos.length > 50) {
            console.log(`  ... +${report.todos.length - 50} more`)
        }
    }

    // write 后可选 verify：只校验本次改动的文件（changedFiles），避免全项目 warning 噪音
    if (args.write && args.verify) {
        console.log(`\n[safe-ui-dev-clean] verify: running eslint on changedFiles...`)
        const v = runEslintOnFiles(report.changedFiles)
        const verify = {
            exitCode: v.exitCode,
            hasError: v.hasError,
            rollbackApplied: false
        }
        report.verify = verify
        if (v.output) {
            console.log(v.output.slice(0, 2000))
            if (v.output.length > 2000) {
                console.log(`[safe-ui-dev-clean] (verify output truncated)`)
            }
        }
        console.log(`[safe-ui-dev-clean] verifyExitCode=${verify.exitCode}, hasError=${verify.hasError ? 'YES' : 'NO'}`)

        if (verify.hasError && args.rollbackOnError) {
            console.log(`[safe-ui-dev-clean] rollback-on-error: applying git checkout on changedFiles...`)
            const rb = rollbackFilesByGitCheckout(report.changedFiles)
            verify.rollbackApplied = rb.ok
            if (!rb.ok) {
                console.log(`[safe-ui-dev-clean] rollback failed. output:\n${rb.output}`)
            } else {
                console.log(`[safe-ui-dev-clean] rollback applied.`)
            }
        }
    }

    // 退出码：有 TODO 时返回 2（方便 CI/脚本判断）
    if (report.todos.length) {
        process.exitCode = 2
    }

    if (args.out) {
        writeTodoMarkdown(args.out, report, args)
        console.log(`- report written: ${args.out}`)
    }

    if (args.log) {
        appendRunLogMarkdown(args.log, report, args)
        console.log(`- log appended: ${args.log}`)
    }
}

main()


