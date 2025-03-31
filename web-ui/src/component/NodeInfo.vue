<template>
  <div class="node-info">
    <el-descriptions :column="column" size="small" :border="true" label-class-name="label" content-class-name="content">
      <el-descriptions-item label="OID">{{node.oid}}</el-descriptions-item>
      <el-descriptions-item label="节点名">{{node.name}}</el-descriptions-item>
      <el-descriptions-item label="父OID">{{node.parentOid}}</el-descriptions-item>
      <el-descriptions-item label="节点类型">{{node.nodeType}}</el-descriptions-item>
      <el-descriptions-item label="可写">
        <el-tag :type="node.canWrite ? 'success' : 'danger'">{{node.canWrite ? '是' : '否'}}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="读写状态">{{node.accessDesc}}</el-descriptions-item>
<!--      <el-descriptions-item label="状态描述">{{node.statusDesc}}</el-descriptions-item>-->
      <el-descriptions-item label="值类型">{{node.valueType}}</el-descriptions-item>
      <el-descriptions-item label="值约束" v-if="node.valueConstraintDesc">{{node.valueConstraintDesc}}</el-descriptions-item>
      <el-descriptions-item label="简述" v-if="node.briefDescription">{{node.briefDescription}}</el-descriptions-item>
      <el-descriptions-item label="详述">{{node.description}}</el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<script>
export default {
  name: "NodeInfo",
  props: {
    node: {
      type: Object,
      required: true
    },
    column: {
      type: Number,
      default: () => 1
    }
  }
}
</script>

<style scoped lang="scss">
.node-info {
  ::v-deep {
    .label {
      width: 80px;
    }

    .content {
      min-width: 200px;
    }
  }
}
</style>