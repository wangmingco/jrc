<template>
  <div class="app-container">
    <el-tag>Java脚本执行器. 上传Java文件进行编译</el-tag>
    <br><br>

    <el-form label-width="120px">

      <el-form-item >
        <el-select v-model="selectedSourceType" placeholder="请选择源码操作类型">
          <el-option
              v-for="item in sourceTypes"
              :key="item"
              :label="item"
              :value="item"
          />
        </el-select>
        <br>

        <el-upload
            v-show="selectedSourceType == '上传Java文件'"
            :data="data"
            class="upload-demo"
            ref="upload"
            action="/jrc/compileFile"
            :on-success="handleSuccess"
            :on-preview="handlePreview"
            :on-remove="handleRemove"
            :file-list="fileList"
            :auto-upload="false">
            <el-button slot="trigger" size="small" type="primary">选取Java文件</el-button>
          </el-upload>

          <el-input
            v-show="selectedSourceType == '编辑Java源码'"
            type="textarea"
            :autosize="{ minRows: 2, maxRows: 300}"
            placeholder="上传的Java代码"
            v-model="javasource">
          </el-input>

        </el-form-item>
    
      <el-form-item>
          <el-button style="margin-left: 10px;" size="small" type="success" @click="submitUpload">上传源码</el-button>
      </el-form-item>
    
       <el-form-item >  
          <el-select v-model="selectMethod" placeholder="请选择要执行的方法">
            <el-option
                v-for="item in javaMethods"
                :key="item"
                :label="item"
                :value="item"
            >
            </el-option>
          </el-select>

          <el-button style="margin-left: 10px;" size="small" type="success" @click="invokeMethod">执行函数</el-button>
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
            placeholder="反编译后Java源码"
            v-model="javacontent">
          </el-input>
        </el-form-item>

     </el-form>     
  </div>
</template>

<script>
import { httpPost } from "@/api/http_util";

export default {
  
  data() {
    return {
      sourceTypes: ['编辑Java源码', '上传Java文件'],
      selectedSourceType: '编辑Java源码',
      javaMethods: [],
      selectMethod: null,
      key: "",
      data: {
        table: "",
        skipFields: ""
      },
      javasource: "",
      result: "",
      javacontent: "",
      fileList: []
    };
  },
  created() {
   
  },
  methods: {
     submitUpload() {
      this.result = ""
      this.javacontent = ""
      
      if(this.selectedSourceType == '上传Java文件') {
          this.$refs.upload.submit();
       } else {
         let query =  {
            javasource : this.javasource
          }
          httpPost(query, '/jrc/compileSource').then(response=> {
            
            this.$notify({
              title: '执行完成',
              type: 'success',
              message: "",
              duration: 5000
            });

            console.info("/jrc/compileSource 应答结果: ", response.data)
            
            this.javacontent = response.data.javacontent;
            this.javaMethods = response.data.methods;
            this.key = response.data.key;

            console.info("/jrc/compileSource method 内容: ", this.javaMethods)

            if(this.javaMethods.length > 0) {
              this.selectMethod = this.javaMethods[0]
            }

          }).catch(err => {
            this.$message.error("/jrc/compileSource request error  " + err)
            console.error("/jrc/execute    " + err)
          })
       }
        
      },
      handleRemove(file, fileList) {
        console.log(file, fileList);
      },
      handlePreview(file) {
        console.log(file);
      },
      handleSuccess(response, file, fileList) {
         this.javacontent = response.data.javacontent;
         this.javaMethods = response.data.methods
         this.selectMethod = this.javaMethods[0]
         this.key = response.data.key;
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
  text-align: left;
}
</style>

