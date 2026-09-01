import aiChatApI from '../api/AIChatAPI.js'

function aiChatInterface() {
    const chat = async (question) => {
        const data = await aiChatApI.chat(question)
        return data ? data : ""
    }

    return {
        chat,
    }
}

export default aiChatInterface()
