# CLAUDE.md - 项目规范

## 项目概述
这是一个服装管理系统的前端项目，基于 Vue.js + Vite 构建。

## 开发规范

### ❌ 禁止使用
1. **Agent 工具** - 不允许使用 Agent 进行任何操作
2. **CSS Grid 布局** - 禁止使用 `display: grid` 或任何 Grid 相关属性
3. **flex: 1** - 禁止使用 `flex: 1` 属性，这是最容易导致布局问题的属性
4. **class 多个类名** - div 的 class 只允许写一个类名，禁止 `class="xxx-xxx yyy-yyy"` 这种写法，修改时根本不知道该改哪个

### ✅ 允许使用
1. **Flex 布局** - 统一使用 Flexbox 进行布局
   - `display: flex`
   - `flex-direction`
   - `justify-content`
   - `align-items`
   - `flex-wrap`
   - `gap`
   - 等其他 Flex 属性

### 布局建议
- 使用固定宽度或百分比代替 `flex: 1`
- 优先使用 `gap` 属性替代 margin 来控制间距
- 嵌套 Flex 容器时注意层级清晰

## 文件结构
- `src/` - 源代码目录
- `src/assets/` - 静态资源
- `src/utils/` - 工具函数和 API 调用
- `src/constants/` - 常量定义（没有就创建）
- `src/page/` - 页面组件
- `src/components/` - 公共组件
- `ProductNote/` - 产品相关文档

### 文件组织原则
- 常量 → `constants` 文件夹
- 页面 → `page` 文件夹
- 组件 → `components` 文件夹
- ❌ 禁止全部写到一个文件里面

## 技术栈
- Vue 3
- Vite
- Axios (HTTP 请求)
- Pinia (状态管理)

---

# 代码格式规范（血的教训）

## 核心原则：每个属性单独一行，禁止任何属性写在同一行

### CSS/SCSS 硬性规则
```
/* ✅ 正确 — 每个属性单独一行 */
.detail-card {
    width: calc(33.33% - 10px);
    min-width: 210px;
    padding: 20px;
    background: #fff;
    border-radius: 16px;
}

/* ❌ 错误 — 同一行写多个属性 */
.detail-card { width: calc(33.33% - 10px); min-width: 210px; padding: 20px; background: #fff; border-radius: 16px; }

/* ❌ 错误 — 即使加了空格也还是同一行 */
.detail-card { width: 100%; min-width: 0; }
```

**没有例外。** 不管多少个属性，不管内容多短，都必须每个单独一行。

### Vue template 硬性规则
```
<!-- ✅ 正确 -->
<input
    v-model="form.name"
    type="text"
    placeholder="请输入"
/>

<!-- ❌ 错误 -->
<input v-model="form.name" type="text" placeholder="请输入" />
```

### Vue script 硬性规则
```
// ✅ 正确
async function fetchData() {
    const result = await api.get()
    data.value = result
}

// ❌ 错误
async function fetchData() { const result = await api.get(); data.value = result }
```

> 违反此规则 = 骗工时 + 浪费用户钱。不允许再犯。

# 代码八荣八耻

> 以简洁明了为荣，以过度设计为耻
> ——能三行写完的，别写三十行；能直接改的，别先写策划书。

> 以跑得通为荣，以理论完美为耻
> ——生产环境里能跑的代码，比架构图上画得漂亮的代码强一万倍。

> 以复制粘贴后能改为荣，以复制粘贴后忘改为耻
> ——Ctrl+C/V 不可耻，可耻的是连变量名都不改就提交。

> 以加注释为荣，以写天书为耻
> ——你一个月后回来看自己的代码，如果骂"这谁写的"，说明注释没写够。

> 以测试覆盖为荣，以"我觉得没问题"为耻
> ——你觉得没问题，但服务器不这么觉得。

> 以小步提交为荣，以憋大招为耻
> ——一次提交改 2000 行，出问题了连回滚都找不到坟头。

> 以读文档为荣，以瞎蒙API为耻
> ——官方文档里明明白白写着，你非要靠直觉传参数，然后报错半小时。

> 以及时沟通为荣，以埋头硬干为耻
> ——前后端接口对不上，你俩隔着一个工位却用代码互相传纸条，何必呢？
