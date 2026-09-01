<template>
    <div class="ai-chat-page">
        <div class="page-toolbar">
            <div class="page-label">
                <h2 class="page-label-title">AI 助手</h2>
                <p class="page-label-desc">向 AI 提问，获取经营分析与业务建议</p>
                <hr class="label-hr"/>
            </div>
        </div>

        <div class="chat-card">
            <div
                ref="messageListRef"
                class="chat-messages"
            >
                <div
                    v-for="msg in messages"
                    :key="msg.id"
                    :class="msg.role === 'user' ? 'message-row-user' : 'message-row-ai'"
                    class="message-row"
                >
                    <div
                        :class="msg.role === 'user' ? 'message-bubble-user' : 'message-bubble-ai'"
                        class="message-bubble"
                    >
                        <span class="message-role">{{ msg.role === 'user' ? '我' : 'AI' }}</span>
                        <span
                            v-if="msg.role === 'user'"
                            class="message-content"
                        >{{ msg.content }}</span>
                        <div
                            v-else
                            class="message-content message-markdown"
                            v-html="renderMarkdown(msg.content)"
                        ></div>
                    </div>
                </div>
                <div
                    v-if="loading"
                    class="message-row message-row-ai"
                >
                    <div class="message-bubble message-bubble-ai">
                        <span class="message-role">AI</span>
                        <span class="message-content message-typing">正在思考…</span>
                    </div>
                </div>
            </div>

            <div class="chat-input-bar">
                <textarea
                    v-model="question"
                    class="chat-input"
                    placeholder="输入你的问题，例如：查询最近7天的销售额和利润"
                    rows="1"
                    @keydown.enter.exact.prevent="handleSend"
                ></textarea>
                <button
                    :disabled="loading || !question.trim()"
                    class="btn-primary chat-send"
                    type="button"
                    @click="handleSend"
                >{{ loading ? '思考中…' : '发送' }}</button>
            </div>
        </div>
    </div>
</template>

<script setup>
import {nextTick, ref} from 'vue'
import {marked} from 'marked'
import DOMPurify from 'dompurify'
import aiChatInterface from '../../../axios/interface/AIChatInterface.js'

const question = ref('')
const loading = ref(false)
const messages = ref([])
const messageListRef = ref(null)
let messageId = 0

// AI 返回的 markdown → 安全 HTML（DOMPurify 防 XSS）
function renderMarkdown(text) {
    if (!text) return ""
    const rawHtml = marked.parse(text, {async: false})
    return DOMPurify.sanitize(rawHtml)
}

async function handleSend() {
    const text = question.value.trim()
    if (!text || loading.value) return

    messages.value.push({
        id: ++messageId,
        role: 'user',
        content: text
    })
    question.value = ''
    scrollToBottom()

    loading.value = true
    try {
        const reply = await aiChatInterface.chat(text)
        messages.value.push({
            id: ++messageId,
            role: 'ai',
            content: reply
        })
    } finally {
        loading.value = false
        scrollToBottom()
    }
}

async function scrollToBottom() {
    await nextTick()
    if (messageListRef.value) {
        messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
}
</script>

<style scoped>
.ai-chat-page {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.chat-card {
    display: flex;
    flex-direction: column;
    height: calc(100vh - 260px);
    min-height: 420px;
    border-radius: 16px;
    background: var(--bg-card);
    border: 1px solid var(--border);
    box-shadow: var(--shadow-sm);
    overflow: hidden;
}

.chat-messages {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
    display: flex;
    flex-direction: column;
    gap: 14px;
}

.message-row {
    display: flex;
    width: 100%;
}

.message-row-user {
    justify-content: flex-end;
}

.message-row-ai {
    justify-content: flex-start;
}

.message-bubble {
    max-width: 76%;
    padding: 10px 14px;
    border-radius: 14px;
    display: flex;
    flex-direction: column;
    gap: 4px;
    font-size: 14px;
    line-height: 1.6;
}

.message-bubble-user {
    background: var(--primary-gradient);
    color: var(--text-invert);
    border-bottom-right-radius: 4px;
}

.message-bubble-ai {
    background: var(--bg-body);
    color: var(--text);
    border: 1px solid var(--border);
    border-bottom-left-radius: 4px;
}

.message-role {
    font-size: 11px;
    font-weight: 700;
    opacity: 0.75;
}

.message-content {
    white-space: pre-wrap;
    word-break: break-word;
}

.message-markdown {
    white-space: normal;
}

.message-markdown :deep(p) {
    margin: 6px 0;
}

.message-markdown :deep(p:first-child) {
    margin-top: 0;
}

.message-markdown :deep(p:last-child) {
    margin-bottom: 0;
}

.message-markdown :deep(h1),
.message-markdown :deep(h2),
.message-markdown :deep(h3),
.message-markdown :deep(h4) {
    margin: 12px 0 6px;
    font-weight: 700;
    line-height: 1.4;
}

.message-markdown :deep(h1) {
    font-size: 18px;
}

.message-markdown :deep(h2) {
    font-size: 16px;
}

.message-markdown :deep(h3) {
    font-size: 15px;
}

.message-markdown :deep(h4) {
    font-size: 14px;
}

.message-markdown :deep(ul),
.message-markdown :deep(ol) {
    margin: 6px 0;
    padding-left: 20px;
}

.message-markdown :deep(li) {
    margin: 3px 0;
}

.message-markdown :deep(code) {
    padding: 2px 5px;
    border-radius: 4px;
    background: rgba(127, 127, 127, 0.15);
    font-family: ui-monospace, SFMono-Regular, Consolas, monospace;
    font-size: 13px;
}

.message-markdown :deep(pre) {
    margin: 8px 0;
    padding: 10px 12px;
    border-radius: 8px;
    background: rgba(0, 0, 0, 0.55);
    color: #e5e7eb;
    overflow-x: auto;
}

.message-markdown :deep(pre code) {
    padding: 0;
    background: transparent;
}

.message-markdown :deep(blockquote) {
    margin: 8px 0;
    padding: 4px 12px;
    border-left: 3px solid var(--primary);
    color: var(--text-secondary);
    background: var(--bg-body);
    border-radius: 0 8px 8px 0;
}

.message-markdown :deep(table) {
    margin: 8px 0;
    border-collapse: collapse;
    font-size: 13px;
}

.message-markdown :deep(th),
.message-markdown :deep(td) {
    padding: 6px 10px;
    border: 1px solid var(--border);
    text-align: left;
}

.message-markdown :deep(th) {
    background: var(--bg-body);
    font-weight: 700;
}

.message-markdown :deep(a) {
    color: var(--primary);
    text-decoration: underline;
}

.message-markdown :deep(hr) {
    margin: 10px 0;
    border: none;
    border-top: 1px solid var(--border);
}

.message-markdown :deep(strong) {
    font-weight: 700;
}

.message-markdown :deep(em) {
    font-style: italic;
}

.message-typing {
    opacity: 0.6;
}

.chat-input-bar {
    display: flex;
    gap: 10px;
    padding: 14px;
    border-top: 1px solid var(--border);
    background: var(--bg-card);
}

.chat-input {
    flex: 1;
    resize: none;
    padding: 10px 14px;
    border: 1px solid var(--border);
    border-radius: 12px;
    font-size: 14px;
    color: var(--text);
    background: var(--bg-body);
    outline: none;
    line-height: 1.5;
}

.chat-input:focus {
    border-color: var(--primary);
    box-shadow: 0 0 0 3px var(--primary-focus);
}

.chat-send {
    align-self: flex-end;
    padding: 10px 24px;
    border-radius: 999px;
    white-space: nowrap;
}
</style>
