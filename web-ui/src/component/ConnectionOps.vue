<template>
  <div class="connection-ops padding-10">
    <el-select class="select" size="small" v-model="ip" @change="handleChange" clearable filterable
               placeholder="请选择设备">
      <template #prefix>
        <i class="el-icon-third-dian" :class="onlineOrOffline"></i>
      </template>
      <el-option class="flex-container" v-for="c in connections" :key="c.ip + c.port" :label="c.ip + ':' + c.port" :value="c.ip">
        <i class="el-icon-third-dian" :class="c.reachable ? 'online': 'offline'"></i>&nbsp;
        <span>{{ c.ip }}:{{ c.port }}</span>
        <span class="flex"></span>
        <el-button type="text" icon="el-icon-third-terminal-box-fill" style="color: black;"
                   @click.native.stop="$emit('open-terminal', c.ip)"></el-button>&nbsp;
      </el-option>
    </el-select>&nbsp;
    <el-button type="primary" size="small" @click="toOpsConnection">连接管理</el-button>
  </div>
</template>

<script>
import {Notification} from 'element-ui'
import http from "@/http";
import {util} from 'fast-crud-ui'
import ConnectionManager from "./ConnectionManager.vue";
import SocketService from "@/websocket";

export default {
  name: "ConnectionOps",
  props: {
    value: String,
    ws: SocketService
  },
  data() {
    return {
      connections: [],
      ip: this.value
    }
  },
  computed: {
    onlineOrOffline() {
      if (!this.ip) {
        return 'un-know'
      }
      const connection = this.connections.find(c => c.ip === this.ip)
      if (!connection) {
        return 'un-know'
      }
      return connection.reachable ? 'online' : 'offline'
    }
  },
  watch: {
    value(newV) {
      this.ip = newV
    }
  },
  created() {
    this.ws.on("CONNECTION_REACHABLE_REFRESH", (map) => {
      const ips = Object.keys(map)
      for (let i = 0; i < ips.length; i++) {
        const ip = ips[i];
        const newReachable = map[ip]
        const connection = this.connections.find(c => c.ip === ip)
        connection.reachable = newReachable
        setTimeout(() => {
          if (newReachable) {
            Notification.success({
              message: `${ip}已恢复连接`,
              duration: 20000,
              offset: 30
            })
          } else {
            Notification.warning({
              message: `${ip}已断开连接`,
              duration: 20000,
              offset: 30
            })
          }
        }, i * 1000)
      }
    })
  },
  mounted() {
    this.initConnections();
  },
  methods: {
    initConnections() {
      http.get('/connections').then(res => {
        this.connections = res.data;
      })
    },
    toOpsConnection() {
      util.openDialog({
        component: ConnectionManager,
        dialogProps: {
          title: '连接管理',
          width: '80%',
          beforeClose: (done) => {
            this.initConnections()
            done()
          }
        }
      })
    },
    handleChange() {
      this.$emit('input', this.ip);
    }
  }
}
</script>

<style scoped lang="scss">
.connection-ops {
  display: flex;
  flex-direction: row;
  align-items: center;
}
</style>