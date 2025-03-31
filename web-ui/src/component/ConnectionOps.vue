<template>
  <div class="connection-ops padding-10">
    <el-select class="select" size="small" v-model="ip" @change="handleChange" placeholder="请选择设备">
      <el-option v-for="c in connections" :key="c.ip + c.port" :label="c.ip + ':' + c.port" :value="c.ip">
        <i class="el-icon-s-opportunity" :class="c.reachable ? 'online': 'offline'"></i>&nbsp;
        <span>{{ c.ip }}:{{ c.port }}</span>
      </el-option>
    </el-select>&nbsp;
    <el-button type="primary" size="small" @click="toOpsConnection">连接管理</el-button>
  </div>
</template>

<script>
import http from "@/http";
import {util} from 'fast-crud-ui'
import ConnectionManager from "./ConnectionManager.vue";

export default {
  name: "ConnectionOps",
  props: {
    value: String
  },
  data() {
    return {
      connections: [],
      ip: this.value
    }
  },
  watch: {
    value(newV) {
      this.ip = newV
    }
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