<template>
  <div class="app-container">
     
      <el-tag>设置jar包依赖, 以及下载相关sdk</el-tag>
      <br><br>

      <el-upload
            :data="data"
            class="upload-demo"
            ref="uploadjar"
            action="/jrc/appendClassPath"
            :on-success="handleJarSuccess"
            :on-preview="handlePreview"
            :on-remove="handleRemove"
            :auto-upload="false">
            <el-button slot="trigger" size="small" type="primary">选取依赖Jar</el-button>
            <el-button style="margin-left: 10px;" size="small" type="success" @click="submitJarUpload">上传依赖Jar</el-button>
            <el-button style="margin-left: 10px;" size="small" type="success" @click="downloadFile('/static/scripts.zip')">下载环境SDK</el-button>
      </el-upload>

  </div>
</template>

<script>
import { httpPost } from "@/api/http_util";

export default {
  data() {
    return {
      data: {
        table: "",
        skipFields: "",
        jarList: []
      }
    };
  },
  created() {
   
  },
  methods: {
      handleRemove(file, fileList) {
        console.log(file, fileList);
      },
      handlePreview(file) {
        console.log(file);
      },
      submitJarUpload() {
        this.$refs.uploadjar.submit();
      },
      handleJarSuccess(response, file, fileList) {
         this.$notify({
              title: 'Jar上传完成',
              type: 'success',
              message: response.data,
              duration: 5000
            });
      },
      downloadFile(url){
        window.open(url);
      }
    }
  };
</script>

<style scoped>
.line {
  text-align: center;
}
</style>

