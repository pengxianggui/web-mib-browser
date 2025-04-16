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

      let cacheInput = ''
      this.term.onData(data => {
        cacheInput = cacheInput + data
        if (cacheInput.trim() === 'clear' && (data === '\r' || data === '\n')) {
          for (let i = 0; i < cacheInput.length; i++) {
            // 删除已经输入的clear
            this.socket.send(JSON.stringify({
              type: 'command',
              command: '\x7f'
            }))
          }
          this.term.clear()
          cacheInput = ''
          return
        }

        if (data === '\r' || data === '\n') {
          cacheInput = ''
        }

        this.socket.send(JSON.stringify({
          type: 'command',
          command: data
        }))
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