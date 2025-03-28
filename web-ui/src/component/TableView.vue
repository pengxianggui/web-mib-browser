<template>
  <div class="table-view">
    <fast-table :option="tableOption">
      <component :is="c.component" v-for="c in columns" :prop="c.name" :label="c.name"
                 v-bind="c.props" :key="c.name" :editable="c.editable"></component>
    </fast-table>
  </div>
</template>

<script>
import {Message} from 'element-ui'
import {FastTableOption} from 'fast-crud-ui';
import * as CompUtil from "@/node_component";

export default {
  name: "TableView",
  props: {
    type: {
      type: String,
      required: true
    },
    node: {
      type: Object,
      required: true
    },
    ip: String,
    operate: String
  },
  computed: {
    // TODO 分页问题：前端静态 or 后端模拟？
    tableOption() {
      const {node: {name}, type, ip, operate} = this
      return new FastTableOption({
        context: this,
        // title: name,
        pageUrl: `${type}/getTableData?ip=${ip}&nodeName=${name}`,
        updateUrl: `${type}/setRowData?ip=${ip}`,
        batchUpdateUrl: `${type}/multiSetRowData?ip=${ip}`,
        insertable: false,
        deletable: false,
        enableMulti: false,
        enableColumnFilter: false,
        lazyLoad: operate !== 'tableView',
        style: {
          flexHeight: true
        },
        // 刚更新完后再刷新，设备有时反应不过来，给的还是旧数据，这里虽然可以缓解，但是体验不好
        // beforeLoad: () => {
        //   return new Promise(resolve => setTimeout(resolve, 1000))
        // },
        loadSuccess: ({data}) => {
          return Promise.resolve({
            records: data,
            total: data.length
          })
        },
        loadFail: () => Promise.reject(), // 禁用内置错误提示
        updateFail: () => Promise.reject(), // 禁用内置错误提示
        beforeUpdate: ({fatRows, rows, editRows}) => {
          // debugger
          const postData = []
          const config = fatRows[0].config
          for (let i = 0; i < editRows.length; i++) {
            const newR = {}
            const r = editRows[i]
            for (let key in r) {
              if (key === 'index') {
                newR[key] = r[key]
                continue
              }
              // 如果可写,并且已更改，则加，防止SNMP 写失败(无写权限)
              const canWrite = Object.prototype.hasOwnProperty.call(config, key) && config[key] && config[key].editable === true
              const changed = r[key] !== rows[i][key]
              if (canWrite && changed) {
                newR[key] = r[key]
              }
            }
            if (Object.keys(newR).length > 1) { // 包括index
              postData.push(newR)
            }
          }
          if (postData.length === 0) {
            Message.warning('你未更改任何值!')
            return Promise.reject()
          }
          return Promise.resolve(postData)
        },
      });
    },
    columns() {
      const columns = []
      const {node} = this
      if (node.nodeType === 'Field') {
        columns.push(CompUtil.getTableColumnComponent(node))
      } else if (node.nodeType === 'Entry') {
        node.children.forEach(fieldNode => {
          columns.push(CompUtil.getTableColumnComponent(fieldNode))
        })
      } else if (node.nodeType === 'Table') {
        node.children[0].children.forEach(fieldNode => {
          columns.push(CompUtil.getTableColumnComponent(fieldNode))
        })
      }
      if(columns.length > 0) {
        columns.unshift(CompUtil.getTableIndexColumn())
      }
      return columns;
    }
  },
  data() {
    return {}
  }
}
</script>

<style scoped lang="scss">
.table-view {
  height: 100%;
}
</style>