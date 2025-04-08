<template>
  <fast-table :option="tableOption" ref="tableOption">
<!--    <fast-table-column prop="id" label="ID" width="180"/>-->
    <fast-table-column-input prop="ip" label="IP" first-filter required/>
    <fast-table-column-number prop="port" label="端口" :default-val="161" required/>
    <fast-table-column-input prop="community" label="community" default-val="public"/>
    <fast-table-column-select prop="version" label="版本" :default-val="2" required
                              :options="[{label:'1',value:1},{label:'2',value:2}]"/>
    <fast-table-column prop="reachable" label="ping通?" :editable="false" :filter="false">
      <template #default="{row: {row}}">
        <i class="el-icon-s-opportunity" :class="row.reachable ? 'online': 'offline'"></i>
      </template>
    </fast-table-column>
    <fast-table-column-input prop="sshUsername" label="telnet用户名"/>
    <fast-table-column-input prop="sshPassword" label="telnet密码"/>
    <fast-table-column-date-picker prop="createdTime" label="创建时间" :editable="false"/>
    <fast-table-column-date-picker prop="updatedTime" label="更新时间" :editable="false"/>
    <template #button="{size}">
      <el-button type="primary" :size="size" @click="ping">Ping所有</el-button>
    </template>
  </fast-table>
</template>

<script>
import {Message} from 'element-ui'
import {FastTableOption} from 'fast-crud-ui'
import http from "@/http";

export default {
  name: "ConnectionManager",
  data() {
    return {
      tableOption: new FastTableOption({
        context: this,
        module: 'connection',
        insertFail: () => Promise.reject(),
        updateFail: () => Promise.reject(),
        loadFail: () => Promise.reject(),
      })
    }
  },
  methods: {
    ping() {
      http.post('/connection/ping').then(() => {
        Message.success('状态刷新成功')
        this.$refs.tableOption.pageLoad()
      })
    }
  }
}
</script>

<style scoped lang="scss">

</style>