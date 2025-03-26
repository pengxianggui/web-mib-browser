<template>
  <el-form ref="form" :model="connection" label-width="100px">
    <el-form-item label="IP" name="ip" required>
      <el-input v-model="connection.ip"/>
    </el-form-item>
    <el-form-item label="port" name="port" required>
      <el-input-number v-model="connection.port" :controls="false"/>
    </el-form-item>
    <el-form-item label="community" name="community">
      <el-input v-model="connection.community"/>
    </el-form-item>
    <el-form-item label="version" name="version">
      <el-input-number v-model="connection.version" :controls="false"/>
    </el-form-item>
  </el-form>
</template>

<script>
import {Message} from 'element-ui';
import http from "@/http";

export default {
  name: "ConnectionForm",
  props: {
    value: Object
  },
  data() {
    return {
      connection: {
        community: 'public',
        port: 161,
        version: 2,
        ...this.value
      }
    }
  },
  methods: {
    submit() {
      const {connection} = this
      return new Promise((resolve, reject) => {
        this.$refs['form'].validate().then(() => {
          http.post('/connection', connection).then(res => {
            resolve(res.data)
            Message.success('保存成功')
          }).catch((err) => {
            reject(err)
          })
        }).catch(() => {
          reject('请填写表单')
        })
      })
    }
  }
}
</script>

<style scoped lang="scss">

</style>