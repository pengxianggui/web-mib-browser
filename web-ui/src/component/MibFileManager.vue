<template>
  <fast-table :option="tableOption">
    <fast-table-column prop="id" label="ID" width="180"/>
    <fast-table-column-input prop="type" label="类型" first-filter required/>
    <fast-table-column-file prop="url" label="mib文件地址" required :response-handler="responseHandler"/>
    <fast-table-column-date-picker prop="createdTime" label="创建时间" :editable="false"/>
    <fast-table-column-date-picker prop="updatedTime" label="更新时间" :editable="false"/>
  </fast-table>
</template>

<script>
import {FastTableOption} from 'fast-crud-ui'

export default {
  name: "MibFileManager",
  data() {
    return {
      tableOption: new FastTableOption({
        context: this,
        module: 'mib_file'
      }),
      responseHandler: function (response) {
        return response.data
      }
    }
  }
}
</script>

<style scoped lang="scss">

</style>