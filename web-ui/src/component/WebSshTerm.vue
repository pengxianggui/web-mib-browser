<template>
  <div class="web-ssh-term">
    <div class="terminal" :id="id"></div>
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

      let isPasswordMode = false
      let cacheInput = ''
      this.term.onData(data => {
        const enter = (data === '\r' || data === '\n')
        cacheInput = cacheInput + data
        if (enter && cacheInput.trim() === 'clear') { // 处理自定义指令clear
          for (let i = 0; i < cacheInput.length; i++) {
            this.socket.send('\x7f') // 删除已经输入的clear
          }
          this.term.clear()
          cacheInput = ''
          return
        }

        if (isPasswordMode) {
          if (enter) {
            this.socket.send(cacheInput)
            isPasswordMode = false
          }
          return;
        }

        if (enter) {
          cacheInput = ''
          isPasswordMode = false
        }

        this.socket.send(data)
      })

      if (!this.ip) {
        Message.warning('缺少ip地址')
        return
      }
      const wsUrl = `ws://${window.location.host}/webssh?ip=${this.ip}`
      this.socket = new WebSocket(wsUrl)
      this.socket.onopen = () => {
        this.connected = true
        this.term.focus()
      }
      // 接收服务器返回的数据
      this.socket.onmessage = (e) => {
        isPasswordMode = e.data.toLowerCase().includes('password:')
        if (isPasswordMode) {
          console.log('等候输入密码..')
        }
        this.term.write(e.data)
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
  position: relative;

  .terminal {
    width: 100%;
    height: 100%;
    background: #000;
    padding: 10px;
    box-sizing: border-box;
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
  }
}
</style>