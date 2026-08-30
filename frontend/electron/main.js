// Electron 主进程：加载前端构建产物（dist/index.html）
// 收银桌面端：店内可信设备，关闭 webSecurity 以直连后端 API（CORS 不再拦截）
import {app, BrowserWindow, shell, session} from 'electron'
import path from 'node:path'
import {fileURLToPath} from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))

// file:// 页面请求后端属于"跨站"，Chromium 默认 SameSite=Lax 会拒绝带登录 cookie：
// 禁用 SameSite 默认行为，让登录态 cookie 在桌面端正常携带（前后端无需修改）
app.commandLine.appendSwitch('disable-features', 'SameSiteByDefaultCookies,CookiesWithoutSameSiteMustBeSecure')

function createWindow() {
    const win = new BrowserWindow({
        width: 1440,
        height: 900,
        minWidth: 1280,
        minHeight: 800,
        title: '服装管理系统-收银端',
        autoHideMenuBar: true,
        webPreferences: {
            // 店内封闭环境：放开跨域，直连后端接口（收银机不存敏感数据）
            webSecurity: false,
            contextIsolation: true,
            nodeIntegration: false,
            preload: path.join(__dirname, 'preload.js')
        }
    })

    win.loadFile(path.join(__dirname, '../dist/index.html'))

    // 外部链接交给系统浏览器，不在窗口内打开
    win.webContents.setWindowOpenHandler(({url}) => {
        shell.openExternal(url)
        return {action: 'deny'}
    })
}

app.whenReady().then(() => {
    // Web Serial / Web USB 权限：网页版小票打印需要放行
    session.defaultSession.setPermissionCheckHandler((webContents, permission) => {
        return permission === 'serial' || permission === 'usb'
    })
    session.defaultSession.setPermissionRequestHandler((webContents, permission, callback) => {
        callback(permission === 'serial' || permission === 'usb')
    })

    createWindow()

    app.on('activate', () => {
        if (BrowserWindow.getAllWindows().length === 0) createWindow()
    })
})

app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') app.quit()
})