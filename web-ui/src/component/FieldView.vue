<template>
  <div class="field-view">
    <el-input v-model="index" placeholder="输入index值" clearable required style="width: 130px;"></el-input>&nbsp;
    <component :is="component.component" v-bind="component.props"
               v-model="value" style="width: 400px;"></component>&nbsp;

    <el-button type="danger" :disabled="component.props.disabled" @click="doSet">Set</el-button>
    <el-button type="primary" plain @click="doGet">Get</el-button>
  </div>
</template>

<script>
import http from "@/http";
import * as CompUtil from "@/node_component";

export default {
  name: "FieldView",
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
      value: null,
      index: null
    }
  },
  computed: {
    component() {
      return CompUtil.getAttrOrFieldComponent(this.node)
    }
  },
  methods: {
    doGet() {
      if (!this.index) {
        this.$message.error("index值不能为空")
        return
      }
      const nodeName = this.node.name + "." + this.index
      http.get(`${this.type}/getSingleData?ip=${this.ip}&nodeName=${nodeName}`).then(res => {
        this.value = res.data
      })
    },
    doSet() {
      const nodeName = this.node.name + "." + this.index
      http.post(`${this.type}/setSingleData?ip=${this.ip}`, {
        name: nodeName,
        value: this.value
      }).then(res => {
        this.$message.success(res.msg)
      })
    }
  }
}
</script>

<style scoped lang="scss">
.field-view {
  display: flex;
}
</style>