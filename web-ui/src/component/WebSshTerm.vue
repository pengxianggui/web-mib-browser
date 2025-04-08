<template>
  <div class="web-ssh-term">
    <div class="terminal" :id="id"></div>
    <div ref="history" class="history" v-show="history.visible">
      <div class="history-item" :class="{'active': history.selectIndex === index}"
           v-for="(item, index) in history.data" :key="index">{{ item }}
      </div>
    </div>
  </div>
</template>

<script>
import {Message} from 'element-ui'
import {Terminal} from '@xterm/xterm'
import {FitAddon} from '@xterm/addon-fit'
import '@xterm/xterm/css/xterm.css'

export default {
  name: "WebSshTerm",
  props: {
    id: String,
    ip: String
  },
  data() {
    return {
      term: null,
      socket: null,
      fitAddon: null,
      connected: false,
      history: {
        data: [],
        selectIndex: -1,
        visible: false
      }
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.initTerminal()
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

      const terminalContainer = document.getElementById(this.id)
      this.term.open(terminalContainer)
      this.fitAddon.fit()

      window.addEventListener('resize', () => {
        this.fitAddon.fit()
      })

      let commandBuffer = '' // 行缓冲
      this.term.onData(data => {
        if (!(this.socket && this.connected)) {
          return
        }
        // 处理回车键（ASCII 13 或 \r）
        if (data === '\r' || data === '\n') {
          const selectedHistoryCommand = this.historySelected();
          if (this.history.visible === true && selectedHistoryCommand !== undefined && commandBuffer === '') {
            commandBuffer = selectedHistoryCommand
            this.history.selectIndex = -1
            this.term.write(commandBuffer)  // 回显字符
          }
          this.history.visible = false
          if (commandBuffer === 'clear') {
            this.term.clear()
            commandBuffer = ''
            this.socket.send(JSON.stringify({
              type: 'command',
              command: '\n'  // 添加换行符表示命令结束
            }))
            return;
          }
          // 发送完整命令
          this.socket.send(JSON.stringify({
            type: 'command',
            command: commandBuffer + '\n'  // 添加换行符表示命令结束
          }))
          if (commandBuffer) {
            this.history.data.push(commandBuffer)
          }
          commandBuffer = ''  // 清空缓冲区
        } else if (data === '\t') { // 处理Tab键，以获取提示
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
        } else if (data === '\x1b[A' || data === '\x1b[B') { // 处理上下键
          this.selectHistory(data === '\x1b[A')
        } else if (data.charCodeAt(0) >= 32 && data.charCodeAt(0) <= 126) {
          // 只处理可打印字符（ASCII 32-126）
          commandBuffer += data
          this.term.write(data)  // 回显字符
        }
      })
    },
    // 打开历史面板选择
    selectHistory(up) {
      if (this.history.data.length === 0) {
        return
      }
      if (this.history.visible === false) {
        this.history.visible = true;
      }
      if (up) {
        this.history.selectIndex = (this.history.selectIndex - 1 + this.history.data.length) % this.history.data.length
      } else {
        this.history.selectIndex = (this.history.selectIndex + 1) % this.history.data.length
      }
    },
    // 返回选中的历史命令
    historySelected() {
      if (this.history.data.length === 0) {
        return undefined
      }
      return this.history.data[this.history.selectIndex]
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

  .terminal {
    width: 100%;
    height: 100%;
    //height: calc(100%);
    background: #000;
    padding: 10px;
    box-sizing: border-box;
  }

  .history {
    position: absolute;
    min-height: 100px;
    max-height: 300px;
    width: 300px;
    top: 30px;
    right: 50px;
    background-color: rgba(91, 91, 91, 0.5);
    text-align: left;
    padding: 5px 10px;
    color: #9d9d9d;
    overflow: hidden auto;

    & > .active {
      color: #f3f397;
    }
  }
}
</style>