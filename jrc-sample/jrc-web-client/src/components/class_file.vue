<template>
  <div class="app-container">
     
     <el-form :label-position="left" label-width="120px">
       
        <el-form-item>
            <el-tag>Java class脚本执行器. 选择class文件上传到服务器, 服务器会对齐进行解析反编译.
               同时会将public, 无参方法返回, 选择其中一个方法进行调用执行.
               支持静态方法和实例方法</el-tag>
        </el-form-item>

       <el-form-item label="class文件上传">
          <el-upload
            :data="data"
            class="upload-demo"
            ref="upload"
            action="/jrc/decompile"
            :on-success="handleSuccess"
            :on-preview="handlePreview"
            :on-remove="handleRemove"
            :file-list="fileList"
            :auto-upload="false">
            <el-button slot="trigger" size="small" type="primary">选取Class文件</el-button>
            <el-button style="margin-left: 10px;" size="small" type="success" @click="submitUpload">上传</el-button>
            <el-button style="margin-left: 10px;" size="small" type="success" @click="invokeMethod">执行函数</el-button>
          </el-upload>
       </el-form-item>

        <el-form-item label="执行方法的名称">  
          <el-select v-model="selectMethod" placeholder="请选择要执行的方法">
            <el-option
                v-for="item in methods"
                :key="item"
                :label="item"
                :value="item"
            >
            </el-option>
          </el-select>
        </el-form-item>

      <el-form-item>
          <el-input
            type="textarea"
            :autosize="{ minRows: 2, maxRows: 300}"
            placeholder="调用方法执行结果"
            v-model="result">
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-input
            type="textarea"
            :autosize="{ minRows: 2, maxRows: 300}"
            placeholder="反编译的Java代码"
            v-model="javacontent">
          </el-input>
        </el-form-item>

     </el-form>     
  </div>
</template>

<script>
import { httpPost } from "@/api/http_util";

export default {
  filters: {
    statusFilter(status) {
      const statusMap = {
        published: "success",
        draft: "gray",
        deleted: "danger"
      };
      return statusMap[status];
    }
  },
  data() {
    return {
      methods: [],
      selectMethod: null,
      key: "",
      fileList: [],
      data: {
        table: "",
        skipFields: ""
      },
      result: "",
      javacontent: ""
    };
  },
  created() {
   
  },
  methods: {
     submitUpload() {
        this.$refs.upload.submit();
      },
      handleRemove(file, fileList) {
        console.log(file, fileList);
      },
      handlePreview(file) {
        console.log(file);
      },
      handleSuccess(response, file, fileList) {
         this.javacontent = response.data.javacontent;
         this.methods = response.data.methods
         this.selectMethod = this.methods[0]
         this.key = response.data.key;
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
      invokeMethod() {
         let query =  {
            method : this.selectMethod,
            key: this.key
          }
          httpPost(query, '/jrc/execute').then(res=> {
            
            this.$notify({
              title: '执行完成',
              type: 'success',
              message: "方法: " + this.selectMethod,
              duration: 5000
            });
            this.result = res.data  

          }).catch(err => {
            this.$message.error("/jrc/execute request error  " + err)
            console.error("/jrc/execute    " + err)
          })
      }
    }
  };
</script>

<style scoped>
.line {
  text-align: center;
}
</style>

