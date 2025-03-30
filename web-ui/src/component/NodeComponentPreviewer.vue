<template>
  <div class="node-component-previewer">
      <table-view :type="type" :ip="ip" :node="node" :operate="operate"
                  v-if="node.nodeType === 'Table' || node.nodeType === 'Entry' || (node.nodeType === 'Field' && operate === 'tableView')"></table-view>
      <field-view :type="type" :ip="ip" :node="node" v-else-if="node.nodeType === 'Field'"></field-view>
      <attr-view :type="type" :ip="ip" :node="node" :operate="operate" v-else-if="node.nodeType === 'Attr'"></attr-view>
      <node-info class="padding-5" :node="node" :column="2" v-else></node-info>
<!--    </div>-->
  </div>
</template>

<script>
import TableView from "@/component/TableView.vue";
import NodeInfo from "@/component/NodeInfo.vue";
import FieldView from "@/component/FieldView.vue";
import AttrView from "@/component/AttrView.vue";

export default {
  name: "NodeComponentPreviewer",
  components: {NodeInfo, TableView, FieldView, AttrView},
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
  }
}
</script>

<style scoped lang="scss">
.node-component-previewer {
  padding: 0 15px;
  height: 100%;
}
</style>