<template>
  <div class="container">
    <mib-tree ref="mibTree" class="tree" :type="type" @nodeClick="nodeClick"
              @execute-operate="executeOperate"></mib-tree>
    <div class="body">
      <div class="head">
        <mib-type-ops v-model="type"></mib-type-ops>
        <connection-ops v-model="ip"></connection-ops>
      </div>
      <el-tabs class="main" type="card" editable :addable="false"
               v-model="activeTableName" @edit="handleTabsEdit">
        <el-tab-pane :key="selectNode.name" :label="selectNode.name" :name="selectNode.name"
                     :closable="false" v-if="selectNode">
          <node-component-previewer :type="type" :node="selectNode" :ip="ip"></node-component-previewer>
        </el-tab-pane>
        <el-tab-pane v-for="panel in tabPanels" :key="panel.key" :label="panel.label" :name="panel.name">
          <node-component-previewer :ref="panel.key" :type="type"
                                    :node="panel.node"
                                    :ip="panel.ip"
                                    :operate="panel.operate"></node-component-previewer>
        </el-tab-pane>
      </el-tabs>
      <!--        <log-console :logs="logs"></log-console>-->
    </div>
  </div>
</template>

<script>
import MibTypeOps from "@/component/MibTypeOps.vue";
import ConnectionOps from "@/component/ConnectionOps.vue";
import MibTree from "@/component/MibTree.vue";
import NodeComponentPreviewer from "@/component/NodeComponentPreviewer.vue";

export default {
  name: "App",
  components: {MibTypeOps, ConnectionOps, MibTree, NodeComponentPreviewer},
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
  watch: {
    type(newV) {
      this.updateQueryParam("type", newV)
    },
    ip(newV) {
      this.updateQueryParam("ip", newV)
    }
  },
  created() {
    this.$nextTick(() => {
      this.type = this.getQueryParam("type")
      this.ip = this.getQueryParam("ip")
      const selectNodeName = this.getQueryParam("selectNodeName");
      setTimeout(() => {
        this.$refs.mibTree.setActiveNode(selectNodeName)
      }, 2000)
    })
  },
  methods: {
    getQueryParam(name) {
      const urlParams = new URLSearchParams(window.location.search)
      return urlParams.get(name)
    },
    updateQueryParam(name, value) {
      const url = new URL(window.location)
      const params = new URLSearchParams(url.search)
      if (value) {
        params.set(name, value)
      } else {
        params.delete(name)
      }
      url.search = params.toString()
      window.history.pushState({}, '', url)
    },
    nodeClick(nodeData) {
      this.selectNode = nodeData
      this.activeTableName = this.selectNode.name
      this.updateQueryParam("selectNodeName", nodeData.name)
    },
    executeOperate(nodeData, operate) {
      if (!this.ip) {
        this.$message.error('请先选择设备')
        return
      }
      const key = this.ip + "-" + nodeData.name;
      if (this.tabPanels.find(panel => panel.key === key) === undefined) {
        const panel = {
          key: key,
          label: key,
          name: key,
          ip: this.ip,
          node: nodeData,
          operate: operate
        }
        this.tabPanels.push(panel)
      }
      this.activeTableName = key
      this.$nextTick(() => {
        const ref = Array.isArray(this.$refs[key]) ? this.$refs[key][0] : this.$refs[key]
        ref.execute(operate)
      })
    },
    handleTabsEdit(targetName, action) {
      console.log(targetName, action)
      if (action !== 'remove') {
        return;
      }
      let tabs = this.tabPanels;
      let activeName = this.activeTableName;
      if (activeName === targetName) {
        tabs.forEach((tab, index) => {
          if (tab.name === targetName) {
            let nextTab = tabs[index + 1] || tabs[index - 1];
            if (nextTab) {
              activeName = nextTab.name;
            }
          }
        });
      }

      this.activeTableName = activeName;
      this.tabPanels = tabs.filter(tab => tab.name !== targetName);
    }
  }
}
</script>

<style scoped lang="scss">
$headHeight: 40px;

.container {
  height: 100%;
  display: flex;
  flex-direction: row;

  & > .tree {
    width: 340px;
    height: 100%;
    border-right: 1px solid #d5d5d5;
    background-color: #e6f5ff;
    & * {
      background-color: transparent;
    }
  }

  .body {
    flex: 1;
    display: flex;
    flex-direction: column;
    height: 100%;
    //height: calc(100% - $headHeight);

    & > .head {
      height: $headHeight;
      line-height: $headHeight;
      display: flex;
      justify-content: space-between;
      border-bottom: 1px solid #d5d5d5;
      background-color: aliceblue;
    }

    .main {
      flex: 1;

      ::v-deep {
        .el-tabs__new-tab {
          display: none;
        }
        .el-tabs__content {
          height: calc(100% - 60px);

          & > .el-tab-pane {
            height: 100%;
          }
        }
      }
    }
  }
}
</style>