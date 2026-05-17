/**
 * Run ESLint --fix only for the directory where `npm run lint:fix-1` is invoked.
 * NPM exposes that location via INIT_CWD.
 */
const path = require('path')
const { spawnSync } = require('child_process')

const projectRoot = path.resolve(__dirname, '..')
const invokedCwd = process.env.INIT_CWD
    ? path.resolve(process.env.INIT_CWD)
    : process.cwd()

const normalizedRoot = projectRoot.toLowerCase()
const normalizedCwd = invokedCwd.toLowerCase()
const isInsideProject =
    normalizedCwd === normalizedRoot ||
    normalizedCwd.startsWith(normalizedRoot + path.sep.toLowerCase())

const targetPath = isInsideProject
    ? path.relative(projectRoot, invokedCwd) || '.'
    : '.'

console.log(`[lint:fix-1] target: ${targetPath}`)

const npxCommand = process.platform === 'win32' ? 'npx.cmd' : 'npx'
const result = spawnSync(
    npxCommand,
    ['eslint', targetPath, '--ext', '.vue,.js', '--fix'],
    {
        cwd: projectRoot,
        stdio: 'inherit'
    }
)

process.exit(result.status || 0)
