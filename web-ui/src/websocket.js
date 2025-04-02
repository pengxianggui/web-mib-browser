export default class SocketService {
    constructor(url) {
        this.ws = null
        this.url = url
        this.callbacks = {}
        this.reconnectAttempts = 0
        this.maxReconnectAttempts = 5
        this.reconnectInterval = 3000
    }

    connect() {
        this.ws = new WebSocket(this.url)

        this.ws.onopen = () => {
            console.log('WebSocket connected')
            this.reconnectAttempts = 0
        }

        this.ws.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data)
                if (data.type && this.callbacks[data.type]) {
                    this.callbacks[data.type](data.payload)
                }
            } catch (e) {
                console.log(event)
            }
        }

        this.ws.onclose = () => {
            console.log('WebSocket disconnected')
            this.reconnect()
        }

        this.ws.onerror = (error) => {
            console.error('WebSocket error:', error)
            console.error(error.message)
        }

        setInterval(() => {
            this.ws.send("ping")
        }, 60000)
    }

    on(type, callback) {
        this.callbacks[type] = callback
    }

    send(type, payload) {
        if (this.ws?.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify({type, payload}))
        } else {
            console.warn('WebSocket not connected')
        }
    }

    reconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++
            setTimeout(() => {
                console.log(`Reconnecting attempt ${this.reconnectAttempts}`)
                this.connect()
            }, this.reconnectInterval)
        }
    }

    disconnect() {
        this.ws?.close()
    }
}