<template>
  <div class="mib-type-ops padding-10">
    <el-select class="select" v-model="mibType" @change="handleChange" placeholder="请选择MIB类型">
      <el-option v-for="type in mibTypes" :key="type" :label="type" :value="type"></el-option>
    </el-select>&nbsp;
    <el-button type="primary" @click="uploadMib">上传Mib文件</el-button>
  </div>
</template>

<script>
import http from '@/http'

export default {
  name: "MibTypeOps",
  props: {
    value: String
  },
  data() {
    return {
      mibType: this.value,
      mibTypes: []
    }
  },
  mounted() {
    this.initTypes();
  },
  methods: {
    initTypes() {
      http.get('/mib/types').then(res => {
        this.mibTypes = res.data;
      })
    },
    uploadMib() {
      // TODO
    },
    handleChange() {
      this.$emit('input', this.mibType);
    }
  }
}
</script>

<style scoped lang="scss">
.mib-type-ops {
  display: flex;
  flex-direction: row;
  align-items: center;

  .select {
    width: 160px;
  }
}
</style>