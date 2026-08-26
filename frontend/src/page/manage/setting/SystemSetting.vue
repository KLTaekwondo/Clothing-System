<template>
    <div class="setting-page">
        <div class="page-toolbar">
            <div class="page-label">
                <h2 class="page-label-title">系统设置</h2>
                <p class="page-label-desc">外观与主题偏好设置，切换后立即生效并自动保存</p>
                <hr class="label-hr"/>
            </div>
        </div>

        <div class="setting-card">
            <div class="card-header">
                <span class="card-title">外观模式</span>
            </div>
            <p class="setting-desc">选择深色或浅色模式，全站界面会同步切换</p>
            <div class="mode-options">
                <button
                    :class="theme === 'light' ? 'mode-option-active' : 'mode-option'"
                    type="button"
                    @click="setTheme('light')"
                >
                    <img
                        :src="sunIcon"
                        alt=""
                        class="mode-icon"
                    />
                    <span class="mode-name">浅色模式</span>
                    <span class="mode-preview-light">
                        <i class="preview-side"></i>
                        <i class="preview-line"></i>
                        <i class="preview-line"></i>
                    </span>
                </button>
                <button
                    :class="theme === 'dark' ? 'mode-option-active' : 'mode-option'"
                    type="button"
                    @click="setTheme('dark')"
                >
                    <img
                        :src="moonIcon"
                        alt=""
                        class="mode-icon"
                    />
                    <span class="mode-name">深色模式</span>
                    <span class="mode-preview-dark">
                        <i class="preview-side"></i>
                        <i class="preview-line"></i>
                        <i class="preview-line"></i>
                    </span>
                </button>
            </div>
        </div>

        <div class="setting-card">
            <div class="card-header">
                <span class="card-title">主题色</span>
            </div>
            <p class="setting-desc">主色会应用到按钮、链接、选中态、徽章等品牌元素</p>
            <div class="accent-options">
                <button
                    v-for="item in ACCENTS"
                    :key="item"
                    :class="accent === item ? 'accent-option-active' : 'accent-option'"
                    type="button"
                    @click="setAccent(item)"
                >
                    <span
                        :style="{background: ACCENT_COLORS[item]}"
                        class="accent-color"
                    ></span>
                    <span class="accent-name">{{ ACCENT_LABELS[item] }}</span>
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import sunIcon from '../../../assets/icons/theme-sun.svg'
import moonIcon from '../../../assets/icons/theme-moon.svg'
import {theme, setTheme, accent, setAccent, ACCENTS, ACCENT_LABELS} from '../../../stores/theme.js'

const ACCENT_COLORS = {
    teal: '#0d9488',
    blue: '#3b82f6',
    orange: '#ea580c',
    purple: '#8b5cf6',
    rose: '#e11d48'
}
</script>

<style scoped>
.setting-page {
    width: 100%;
    min-width: 0;
    max-width: 720px;
}

.setting-card {
    margin-bottom: 20px;
}

.setting-desc {
    margin-bottom: 20px;
    color: var(--text-secondary);
    font-size: var(--font-sm);
    line-height: 1.6;
}

/* ── 外观模式 ── */
.mode-options {
    display: flex;
    gap: 16px;
}

.mode-option,
.mode-option-active {
    width: calc(50% - 8px);
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 18px;
    border: 1px solid var(--border);
    border-radius: var(--radius-lg);
    background: var(--bg-card);
    text-align: left;
    cursor: pointer;
    transition: var(--transition);
}

.mode-option:hover {
    border-color: var(--border-hover);
    background: var(--bg-hover);
}

.mode-option-active {
    border-color: var(--primary);
    background: var(--primary-light);
    box-shadow: 0 0 0 3px var(--primary-focus);
}

.mode-icon {
    width: 22px;
    height: 22px;
    color: var(--text-secondary);
}

.mode-name {
    color: var(--text);
    font-size: 14px;
    font-weight: 700;
}

.mode-option-active .mode-name {
    color: var(--primary-dark);
}

/* 迷你界面预览 */
.mode-preview-light,
.mode-preview-dark {
    height: 58px;
    display: flex;
    align-items: flex-start;
    gap: 6px;
    padding: 9px;
    border-radius: var(--radius);
    border: 1px solid var(--border-light);
}

.mode-preview-light {
    background: #f6f6f5;
}

.mode-preview-dark {
    background: #141414;
    border-color: #333333;
}

.preview-side {
    width: 14px;
    height: 100%;
    border-radius: 3px;
    background: #ffffff;
    border: 1px solid #f0f0f0;
}

.mode-preview-dark .preview-side {
    background: #1e1e1e;
    border-color: #333333;
}

.preview-line {
    width: 30px;
    height: 7px;
    border-radius: 2px;
    background: #e6e6e6;
    margin-top: 8px;
}

.preview-line:nth-of-type(2) {
    background: var(--primary);
}

.preview-line:nth-of-type(3) {
    width: 20px;
    background: #9d9d9d;
}

.mode-preview-dark .preview-line {
    background: #333333;
}

.mode-preview-dark .preview-line:nth-of-type(2) {
    background: var(--primary);
}

.mode-preview-dark .preview-line:nth-of-type(3) {
    background: #7d7d7d;
}

/* ── 主题色 ── */
.accent-options {
    display: flex;
    flex-wrap: wrap;
    gap: 14px;
}

.accent-option,
.accent-option-active {
    width: 96px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 14px 10px;
    border: 1px solid var(--border);
    border-radius: var(--radius-lg);
    background: var(--bg-card);
    cursor: pointer;
    transition: var(--transition);
}

.accent-option:hover {
    border-color: var(--border-hover);
    background: var(--bg-hover);
}

.accent-option-active {
    border-color: var(--primary);
    background: var(--primary-light);
    box-shadow: 0 0 0 3px var(--primary-focus);
}

.accent-color {
    width: 26px;
    height: 26px;
    border-radius: 50%;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

.accent-name {
    color: var(--text-secondary);
    font-size: 12px;
}

.accent-option-active .accent-name {
    color: var(--primary-dark);
    font-weight: 700;
}

@media (max-width: 560px) {
    .mode-options {
        flex-direction: column;
    }

    .mode-option,
    .mode-option-active {
        width: 100%;
    }

    .accent-options {
        gap: 10px;
    }

    .accent-option,
    .accent-option-active {
        width: calc(33.33% - 7px);
    }
}
</style>