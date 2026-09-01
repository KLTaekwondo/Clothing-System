import backendService from '../backendService.js'

const aiChatApI = {
    chat: (question) => backendService.post('/ai/chat', {question}),
}

export default aiChatApI;
