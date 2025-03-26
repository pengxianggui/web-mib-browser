<template>
  <div class="container">
    <div class="head">
      <mib-type-ops v-model="type"></mib-type-ops>
      <connection-ops v-model="ip"></connection-ops>
    </div>
    <div class="body">
      <mib-tree class="tree" :type="type"
                @nodeClick="nodeClick" @nodeGet="get" @nodeSet="set" @tableView="tableView"></mib-tree>
      <div class="main">
        <el-tabs v-model="activeTableName" type="card">
          <el-tab-pane :key="selectNode.name" :label="selectNode.name" :name="selectNode.name" v-if="selectNode">
            <node-component-previewer :type="type" :node="selectNode" :ip="ip"></node-component-previewer>
          </el-tab-pane>
          <el-tab-pane v-for="panel in tabPanels" :key="panel.key" :label="panel.label" :name="panel.name">
            <node-component-previewer :ref="panel.name" :type="type"
                                      :node="panel.node"
                                      :ip="panel.ip"
                                      :operate="panel.operate"></node-component-previewer>
          </el-tab-pane>
        </el-tabs>
        <log-console :logs="logs"></log-console>
      </div>
    </div>
  </div>
</template>

<script>
import MibTypeOps from "@/component/MibTypeOps.vue";
import ConnectionOps from "@/component/ConnectionOps.vue";
import MibTree from "@/component/MibTree.vue";
import NodeComponentPreviewer from "@/component/NodeComponentPreviewer.vue";
import LogConsole from "@/component/LogConsole.vue";

export default {
  name: "App",
  components: {MibTypeOps, ConnectionOps, MibTree, NodeComponentPreviewer, LogConsole},
  data() {
    return {
      type: null, // mib类型
      ip: null, // 连接
      selectNode: null,
      /**
       * 格式:
       * {
       *   key: '',
       *   label: '',
       *   name: '',
       *   ip: '',
       *   node: {
       *     name: '',
       *     oid: '',
       *     ...
       *   },
       *   operate: 'get|set|tableView'
       * }
       */
      activeTableName: null,
      tabPanels: [],
      logs: []
    }
  },
  methods: {
    nodeClick(nodeData) {
      this.selectNode = nodeData
      this.activeTableName = this.selectNode.name
    },
    get(nodeData) {
      // TODO
      console.log(nodeData)
    },
    set(nodeData) {
      // TODO
      console.log(nodeData)
    },
    tableView(nodeData) {
      // TODO
      console.log(nodeData)
    },
    addToPanel(panel) {
      if (panel.operate && panel.connection.ip) {
        this.$refs[panel.name].execute(panel.operate)
      }
    }
  }
}
</script>

<style scoped lang="scss">
$headHeight: 60px;

.container {
  height: 100%;
  display: flex;
  flex-direction: column;

  & > .head {
    height: $headHeight;
    line-height: $headHeight;
    display: flex;
    justify-content: space-between;
    border-bottom: 1px solid #d5d5d5;

    //& > * {
      //flex: 1;
    //}
  }

  .body {
    flex: 1;
    display: flex;
    height: calc(100% - $headHeight);

    .tree {
      width: 340px;
      height: 100%;
      border-right: 1px solid #d5d5d5;
    }

    .main {
      flex: 1;
    }
  }
}
</style>