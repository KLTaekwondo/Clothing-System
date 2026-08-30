// 预加载脚本：目前无需暴露 Node 能力，占位保证 contextIsolation 下结构完整
import {contextBridge} from 'electron'

contextBridge.exposeInMainWorld('desktop', {
    platform: process.platform
})