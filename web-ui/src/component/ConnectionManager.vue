<template>
  <fast-table :option="tableOption">
    <fast-table-column prop="id" label="ID" width="180"/>
    <fast-table-column-input prop="ip" label="IP" first-filter required/>
    <fast-table-column-number prop="port" label="端口" :default-val="161" required/>
    <fast-table-column-input prop="community" label="community" default-val="public"/>
    <fast-table-column-select prop="version" label="版本" :default-val="2" required
                              :options="[{label:'1',value:1},{label:'2',value:2}]"/>
    <fast-table-column-date-picker prop="createdTime" label="创建时间" :editable="false"/>
    <fast-table-column-date-picker prop="updatedTime" label="更新时间" :editable="false"/>
  </fast-table>
</template>

<script>
import {FastTableOption} from 'fast-crud-ui'

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
  }
}
</script>

<style scoped lang="scss">

</style>