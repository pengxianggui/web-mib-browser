<template>
  <div class="connection-ops padding-10">
    <el-select class="select" size="small" v-model="ip" @change="handleChange" placeholder="请选择设备">
      <el-option v-for="c in connections" :key="c.ip + c.port" :label="c.ip + ':' + c.port" :value="c.ip"></el-option>
    </el-select>&nbsp;
    <el-button type="primary" size="small" @click="addOrUpdate" v-if="this.ip">更新</el-button>
    <el-button type="primary" size="small" @click="addOrUpdate">新增</el-button>
  </div>
</template>

<script>
import http from "@/http";
import {util} from 'fast-crud-ui'
import ConnectionForm from "@/component/ConnectionForm.vue";

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
    addOrUpdate() {
      const connection = this.connections.find(c => c.ip === this.ip)
      util.openDialog({
        component: ConnectionForm,
        props: {
          value: connection
        },
        dialogProps: {
          title: this.ip ? '更新设备' : '新增设备',
          width: '50%',
          buttons: [{
            text: '确定',
            type: 'primary',
            onClick: (instance) => instance.submit()
          }, {
            text: '取消',
            onClick: (/*instance*/) => {
              return Promise.reject();
            }
          }]
        }
      }).then(conn => {
        this.ip = conn.ip;
        this.handleChange()
        this.initConnections()
      }).catch(err => {
        console.error(err)
      });
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