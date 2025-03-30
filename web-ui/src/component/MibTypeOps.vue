<template>
  <div class="mib-type-ops padding-10">
    <el-select class="select" size="small" clearable v-model="mibType" @change="handleChange" placeholder="请选择MIB类型">
      <el-option v-for="type in mibTypes" :key="type" :label="type" :value="type"></el-option>
    </el-select>&nbsp;
    <el-button type="primary" size="small" @click="toOpsMib">Mib文件维护</el-button>
  </div>
</template>

<script>
import http from '@/http'
import MibFileManager from './MibFileManager.vue'
import {util} from 'fast-crud-ui'

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
  watch: {
    value(newV) {
      this.mibType = newV
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
    toOpsMib() {
      util.openDialog({
        component: MibFileManager,
        dialogProps: {
          title: 'Mib文件管理',
          width: '80%',
          beforeClose: (done) => {
            this.initTypes()
            done()
          }
        }
      })
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