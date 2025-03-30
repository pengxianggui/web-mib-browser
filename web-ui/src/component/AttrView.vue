<template>
  <div class="attr-view">
    <component :is="component.component" v-bind="component.props"
               v-model="value" style="width: 400px;"></component>&nbsp;

    <el-button type="danger" :disabled="component.props.disabled || value === null" @click="doSet">Set</el-button>
    <el-button type="primary" plain @click="doGet">Get</el-button>
  </div>
</template>

<script>
import http from "@/http";
import * as CompUtil from "@/node_component";

export default {
  name: "AttrView",
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
  data() {
    return {
      value: null
    }
  },
  computed: {
    component() {
      return CompUtil.getAttrOrFieldComponent(this.node)
    }
  },
  mounted() {
    if (this.operate === "get") {
      this.doGet()
    }
  },
  methods: {
    doGet() {
      http.get(`${this.type}/getSingleData?ip=${this.ip}&nodeName=${this.node.name}`).then(res => {
        this.value = res.data
      })
    },
    doSet() {
      http.post(`${this.type}/setSingleData?ip=${this.ip}`, {
        name: this.node.name,
        value: this.value
      }).then(res => {
        this.$message.success(res.msg)
      })
    }
  }
}
</script>

<style scoped lang="scss">

</style>