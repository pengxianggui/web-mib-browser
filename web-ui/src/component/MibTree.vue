<template>
  <div class="mib-tree padding-5">
    <div class="opr" v-if="type">
      <el-button type="text" class="el-icon-refresh" @click="getTreeData(type, true)"></el-button>
      <el-input class="search" v-model="filterText" size="small" clearable placeholder="过滤"></el-input>
      <el-button type="text" class="el-icon-arrow-down" @click="expandAll"></el-button>
      <el-button type="text" class="el-icon-arrow-left" @click="collapseAll"></el-button>
    </div>
    <el-tree class="tree"
             ref="tree"
             :data="treeData"
             :props="treeConfig"
             :default-expand-all="false"
             :filter-node-method="filterNode"
             :highlight-current="true"
             @node-contextmenu="rightButtonHandle"
             node-key="name"
             @node-click="nodeClick">
      <template #default="{ node, data }">
        <div :title="data.toolTipText">
          <i :class="getIconClass(node, data)"></i>&nbsp;
          <span>{{ data.name }}</span>
        </div>
      </template>
    </el-tree>
    <el-popover placement="right"
                width="150"
                trigger="manual"
                v-model="nodeMenu.visible"
                @mouseleave.native="() => nodeMenu.visible = false"
                class="right-menu">
      <ul>
        <li v-for="operate in nodeMenu.operates" :key="operate" @click="executeOperate(operate, nodeMenu.data)">
          {{ operate }}
        </li>
      </ul>
    </el-popover>

    <node-info class="node-info el-card" :node="selectNode" :column="1" v-if="selectNode"></node-info>
  </div>
</template>

<script>
import NodeInfo from "@/component/NodeInfo.vue";
import $http from '@/http'

export default {
  name: "MibTree",
  components: {NodeInfo},
  props: {
    type: String
  },
  data() {
    return {
      filterText: null,
      treeData: [],
      treeConfig: {
        label: 'name',
        children: 'children'
      },
      selectNode: null,
      nodeMenu: {
        visible: false,
        operates: [],
        data: null
      }
    }
  },
  watch: {
    type(newV) {
      if (!newV) {
        this.treeData = []
        this.nodeClick(null)
        return;
      }
      this.getTreeData(newV)
    },
    filterText(newV) {
      this.$refs.tree.filter(newV)
    }
  },
  methods: {
    getTreeData(type, invalidCache = false) {
      if (!type) {
        return;
      }
      $http.get(`/mib/${type}/tree?invalidCache=${invalidCache}`).then(res => {
        this.treeData = [res.data]
      })
    },
    filterNode(value, data) {
      if (!value) return true;
      return data.name.indexOf(value) !== -1;
    },
    getIconClass(node, data) {
      const {nodeType, canWrite} = data
      if (nodeType === 'Dir') {
        return 'el-icon-folder'
      } else if (nodeType === 'Table') {
        return 'el-icon-third-biaoge'
      } else if (nodeType === 'Entry') {
        return 'el-icon-document'
      } else if (nodeType === 'Field') {
        return canWrite ? 'el-icon-edit' : 'el-icon-view'
      } else if (nodeType === 'Attr') {
        return canWrite ? 'el-icon-edit' : 'el-icon-view'
      } else if (nodeType === 'Trip') {
        return 'el-icon-third-yigaojing'
      }
    },
    nodeClick(nodeData/*, node*/) {
      this.selectNode = nodeData
      this.$emit('nodeClick', nodeData)
    },
    setActiveNode(nodeName) {
      if (this.treeData.length === 0) {
        return;
      }
      this.$refs.tree.setCurrentKey(nodeName)
      const node = this.$refs.tree.getNode(nodeName)
      this.filterText = nodeName
      if (node) {
        this.nodeClick(node.data)
      }
      this.$nextTick(() => {
        this.filterText = null // 通过过滤+清除过滤，取巧实现展开定位
      })
    },
    // 收缩
    collapseAll() {
      const expandedNodes = this.$refs.tree.store.nodesMap
      for (let key in expandedNodes) {
        if (expandedNodes[key].expanded) {
          this.$refs.tree.store.nodesMap[key].expanded = false
        }
      }
    },
    // 展开
    expandAll() {
      const expand = (nodes) => {
        nodes.forEach(node => {
          this.$refs.tree.store.nodesMap[node.data.name].expanded = true
          if (node.childNodes && node.childNodes.length > 0) {
            expand(node.childNodes)
          }
        })
      }
      expand(this.$refs.tree.store.root.childNodes)
    },
    rightButtonHandle(MouseEvent, nodeData, Node, VueComponent) {
      console.log(MouseEvent, Node, VueComponent)
      const rightMenuEl = document.querySelector(".right-menu")
      rightMenuEl.style.left = event.offsetX + "px"
      rightMenuEl.style.top = event.clientY + "px"
      const {nodeType} = nodeData
      const popMenu = []
      if (nodeType === 'Table' || nodeType === 'Entry' || nodeType === 'Field') {
        popMenu.push('tableView')
      } else if (nodeType === 'Attr') {
        popMenu.push('get')
      }

      if (popMenu.length > 0) {
        this.nodeMenu.operates = popMenu
        this.nodeMenu.visible = true
        this.nodeMenu.data = nodeData;
      }
    },
    executeOperate(operate, nodeData) {
      this.$emit('execute-operate', nodeData, operate)
    }
  }
}
</script>

<style scoped lang="scss">
.mib-tree {
  display: flex;
  flex-direction: column;

  .opr {
    height: 40px;
    box-shadow: #d5d5d5 0px 2px 5px;
    z-index: 1;
    display: flex;
    align-items: center;
    background-color: #e6f5ff;

    & > .el-button {
      margin: 0 10px;
    }
  }

  .tree {
    flex: 1;
    overflow: auto;
  }

  .node-info {
    height: 200px;
    min-height: 200px;
    overflow-y: auto;
    box-shadow: #e6e6e6 0px -4px 9px;
    z-index: 1;
  }
}

.right-menu {
  position: absolute;

  ::v-deep {
    .el-popover {
      padding: 5px;
    }
  }

  & ul {
    list-style: none;
    margin: 0;
    padding: 0;

    li {
      padding: 5px 10px;
      cursor: pointer;

      &:hover {
        background-color: aliceblue;
      }
    }
  }
}
</style>