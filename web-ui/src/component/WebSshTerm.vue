<template>
  <div class="web-ssh-term">
    <div id="terminal"></div>
  </div>
</template>

<script>
import {Message} from 'element-ui'
import {Terminal} from '@xterm/xterm'
import {FitAddon} from '@xterm/addon-fit'
// import {AttachAddon} from '@xterm/addon-attach'
import '@xterm/xterm/css/xterm.css'

export default {
  name: "WebSshTerm",
  props: {
    ip: String
  },
  data() {
    return {
      term: null,
      socket: null,
      fitAddon: null,
      connected: false,
    }
  },
  mounted() {
    this.initTerminal()
    this.$nextTick(() => {
      this.connect()
    })
  },
  beforeDestroy() {
    console.log("beforeDestroy..")
    this.disconnect()
  },
  methods: {
    initTerminal() {
      this.term = new Terminal({
        cursorBlink: true,
        fontFamily: 'monospace',
        fontSize: 14
      })

      this.fitAddon = new FitAddon()
      this.term.loadAddon(this.fitAddon)

      const terminalContainer = document.getElementById('terminal')
      this.term.open(terminalContainer)
      this.fitAddon.fit()

      window.addEventListener('resize', () => {
        this.fitAddon.fit()
      })

      // TODO 输入后应当借鉴iterm2的输入方式，支持上下键选历史命令、历史自动提示补全等
      let commandBuffer = '' // 行缓冲
      this.term.onData(data => {
        if (this.socket && this.connected) {
          // 处理回车键（ASCII 13 或 \r）
          if (data === '\r' || data === '\n') {
            // 发送完整命令
            this.socket.send(JSON.stringify({
              type: 'command',
              command: commandBuffer + '\n'  // 添加换行符表示命令结束
            }))
            commandBuffer = ''  // 清空缓冲区
          } else if (data === '\t') { // 处理Tab键，提示
            this.socket.send(JSON.stringify({
              type: 'command',
              command: commandBuffer + '\t'  // 添加换行符表示命令结束
            }))
            commandBuffer = ''  // 清空缓冲区
          } else if (data === '\x7f') {  // 处理退格键
            if (commandBuffer.length > 0) {
              commandBuffer = commandBuffer.substring(0, commandBuffer.length - 1)
              // 移动光标并删除最后一个字符
              this.term.write('\b \b')
            }
          } else if (data.charCodeAt(0) >= 32 && data.charCodeAt(0) <= 126) {
            // 只处理可打印字符（ASCII 32-126）
            commandBuffer += data
            this.term.write(data)  // 回显字符
          }
        }
      })
    },
    connect() {
      if (!this.ip) {
        Message.warning('缺少ip地址')
        return
      }

      const wsUrl = `ws://${window.location.host}/webssh`
      this.socket = new WebSocket(wsUrl)

      this.socket.onopen = () => {
        this.socket.send(JSON.stringify({
          type: 'connect',
          ip: this.ip
        }))

        // const attachAddon = new AttachAddon(this.socket)
        // this.term.loadAddon(attachAddon)

        // 接收服务器返回的数据
        this.socket.onmessage = (event) => {
          this.term.write(event.data)
        }

        this.connected = true
        this.term.focus()
      }

      this.socket.onclose = () => {
        this.connected = false
        this.term.writeln('\r\nSSH连接已关闭')
      }

      this.socket.onerror = (error) => {
        console.error('WebSocket错误:', error)
        this.term.writeln('\r\nSSH连接错误')
        this.connected = false
      }
    },
    disconnect() {
      if (this.socket) {
        this.socket.close()
      }
      if (this.term) {
        this.term.dispose()
      }
    }
  }
}
</script>

<style scoped lang="scss">
.web-ssh-term {
  height: 100%;

  #terminal {
    width: 100%;
    height: 100%;
    background: #000;
    padding: 10px;
    box-sizing: border-box;
  }
}
</style>