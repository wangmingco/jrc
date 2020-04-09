<template>
  <div class="app-container">
   
    <div class="tab-container">
      <el-tabs v-model="activeName" style="margin-top:15px;" type="border-card">
        <el-tab-pane label="Java文件" name="javaFile">
          <javaFile></javaFile>
        </el-tab-pane>
        
         <el-tab-pane label="Class文件" name="classFile">
          <classFile></classFile>
        </el-tab-pane>

         <el-tab-pane label="环境设置" name="env">
          <env></env>
        </el-tab-pane>
        
      </el-tabs>

    </div>
  </div>
</template>

<script>
import classFile from './class_file'
import javaFile from './java_file'
import env from './env'

export default {
  components: { classFile, javaFile, env},
  filters: {
    statusFilter(status) {
      const statusMap = {
        published: 'success',
        draft: 'gray',
        deleted: 'danger'
      }
      return statusMap[status]
    }
  },
  data() {
    return {
      activeName: 'javaFile'
    }
  },
  created() {
    
  },
  methods: {
  
  }
}
</script>
