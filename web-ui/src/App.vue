<template>
  <div class="container">
    <transition name="fade">
      <mib-tree ref="mibTree" class="tree" :type="type" @nodeClick="nodeClick"
                @execute-operate="executeMibNodeOperate" v-show="treeVisible"></mib-tree>
    </transition>
    <div class="body">
      <div class="head">&nbsp;
        <el-button icon="el-icon-s-fold" size="small" @click="() => treeVisible = !treeVisible"></el-button>
        <mib-type-ops v-model="type"></mib-type-ops>
        <span class="flex"></span>
        <el-button type="text" icon="el-icon-third-terminal-box-fill" style="color: black;"
                   @click="openTerminal(ip)"></el-button>&nbsp;
        <connection-ops :ws="socketService" @open-terminal="openTerminal" v-model="ip"></connection-ops>
      </div>
      <el-tabs class="main" type="card" editable :addable="false" v-model="activeTableName" @edit="handleTabsEdit">
        <el-tab-pane :key="previewPanel.key" :label="previewPanel.label" :name="previewPanel.name" :closable="false">
          <node-component-previewer :type="type" :node="selectNode" :ip="ip" v-if="selectNode"/>
        </el-tab-pane>
        <el-tab-pane v-for="panel in tabPanels" :key="panel.key" :label="panel.label" :name="panel.name">
          <component :is="panel.component" :ref="panel.key" v-bind="panel.props"></component>
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
import WebSshTerm from "@/component/WebSshTerm.vue";
import SocketService from "@/websocket";

export default {
  name: "App",
  components: {MibTypeOps, ConnectionOps, MibTree, NodeComponentPreviewer, WebSshTerm},
  data() {
    return {
      treeVisible: true,
      socketService: null,
      type: null, // mib类型
      ip: null, // 连接
      selectNode: null,
      activeTableName: null,
      tabPanels: [], // 格式: {key: '', label: '', name: '', component: '', props: {...}}
      logs: []
    }
  },
  computed: {
    previewPanel() {
      const name = this.selectNode ? this.selectNode.name : '点击mib节点预览'
      return {key: name, label: name, name: name}
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

    this.socketService = new SocketService(`ws://${window.location.host}/socket`)
    this.socketService.connect()
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
      this.selectNode = nodeData;
      if (nodeData != null) {
        this.activeTableName = nodeData ? nodeData.name : null;
      }
      this.updateQueryParam("selectNodeName", this.activeTableName)
    },
    executeMibNodeOperate(nodeData, operate) {
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
          component: 'node-component-previewer',
          props: {
            ip: this.ip,
            type: this.type,
            node: nodeData,
            operate: operate
          }
        }
        this.tabPanels.push(panel)
      }
      this.activeTableName = key
    },
    handleTabsEdit(targetName, action) {
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

      this.tabPanels = tabs.filter(tab => tab.name !== targetName);
      this.activeTableName = this.tabPanels.length === 0 ? this.previewPanel.name : activeName;
    },
    openTerminal(ip) {
      if (!ip) {
        return;
      }
      const timestamp = new Date().getTime()
      const key = `SSH-${ip}-${timestamp}`
      this.tabPanels.push({
        key: key,
        name: key,
        label: `SSH-${ip}`,
        component: 'web-ssh-term',
        props: {
          ip: ip,
          id: key
        }
      })
      this.activeTableName = key
    }
  },
  beforeDestroy() {
    if (this.socketService) {
      this.socketService.disconnect()
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
    overflow: hidden;
    //height: calc(100% - $headHeight);

    & > .head {
      height: $headHeight;
      line-height: $headHeight;
      display: flex;
      align-items: center;
      border-bottom: 1px solid #d5d5d5;
      background-color: aliceblue;
      & > .flex {
        flex: 1;
      }
    }

    .main {
      flex: 1;

      ::v-deep {
        .el-tabs__new-tab {
          display: none;
        }

        .el-tabs__content {
          height: calc(100% - 45px);

          & > .el-tab-pane {
            height: 100%;
          }
        }
        .el-tabs__header {
          margin-bottom: 0;
        }
      }
    }
  }
}

.fade-enter-active, .fade-leave-active {
  transition: width .25s ease, opacity .25s ease;
}
.fade-enter, .fade-leave-to {
  opacity: 0;
  width: 0 !important;
}

</style>