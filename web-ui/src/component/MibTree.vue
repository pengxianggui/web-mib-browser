<template>
  <div class="mib-tree padding-5">
    <el-tree class="tree"
             :data="treeData"
             :props="treeConfig"
             :default-expand-all="false"
             :filter-node-method="filterNode"
             :highlight-current="true"
             @node-click="nodeClick"
             ref="tree">
      <template #default="{ node, data }">
        <div :title="data.toolTipText">
          <i :class="getIconClass(node, data)"></i>&nbsp;
          <span>{{ data.name }}</span>
        </div>
      </template>
    </el-tree>

<!--    <node-info class="node-info el-card" :node="selectNode" v-if="selectNode.name"></node-info>-->
  </div>
</template>

<script>
// import NodeInfo from "@/component/NodeInfo.vue";
import $http from '@/http'

export default {
  name: "MibTree",
  // components: { NodeInfo },
  props: {
    type: String
  },
  data() {
    return {
      treeData: [],
      treeConfig: {
        label: 'name',
        children: 'children'
      },
      selectNode: {}
    }
  },
  watch: {
    'type': function (newV) {
      this.getTreeData(newV)
    }
  },
  methods: {
    getTreeData(type) {
      if (!type) {
        return;
      }
      $http.get(`/mib/${type}/tree`).then(res => {
        this.treeData = [res.data]
      })
    },
    filterNode(value, data) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    },
    getIconClass(node, data) {
      console.log('node', node)
      console.log('data', data)
      const {nodeType} = data
      if (nodeType === 'Dir') {
        return 'el-icon-folder'
      } else if (nodeType === 'Table') {
        return 'el-icon-s-grid'
      } else if (nodeType === 'Entry') {
        return 'el-icon-document'
      } else if (nodeType === 'Field') {
        return 'el-icon-edit'
      } else if (nodeType === 'Attr') {
        return 'el-icon-edit'
      }
    },
    nodeClick(nodeData/*, node*/) {
      this.selectNode = nodeData
      this.$emit('nodeClick', nodeData)
    }
  }
}
</script>

<style scoped lang="scss">
.mib-tree {
  display: flex;
  flex-direction: column;

  .tree {
    flex: 1;
    overflow: auto;
  }

  .node-info {
    height: 280px;
    min-height: 280px;
    padding: 10px;
    overflow-y: auto;
  }
}
</style>