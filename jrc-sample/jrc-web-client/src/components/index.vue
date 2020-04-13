<template>
  <div class="app-container">
    <el-row>
      <el-col :span="3"> <el-tag>Java脚本执行器.</el-tag></el-col>
    </el-row>

     <el-row>
      <el-col :span="4"> 
        <el-select v-model="selectedSourceType" placeholder="请选择源码操作类型">
          <el-option v-for="item in sourceTypes" :key="item" :label="item" :value="item" />
        </el-select>
      </el-col>
      
      <el-col :span="18"> 
          <el-input
            v-show="selectedSourceType == '编辑Java源码'"
            type="textarea"
            :autosize="{ minRows: 1, maxRows: 300}"
            placeholder="上传的Java代码"
            v-model="javasource">
          </el-input>
      </el-col>

      <el-col :span="2"> 
        <el-upload
            v-show="selectedSourceType == '上传Java文件'"
            :data="data"
            class="upload-demo"
            ref="upload"
            action="/jrc/uploadJavaFile"
            :on-success="handleSuccess"
            :on-preview="handlePreview"
            :on-remove="handleRemove"
            :file-list="fileList"
            :auto-upload="false">
            <el-button slot="trigger" size="small" type="primary">选取Java文件</el-button>
          </el-upload>
      </el-col>
      
      <el-col :span="2"> 
        <el-upload
              v-show="selectedSourceType == '上传Class文件'"
              :data="data"
              class="upload-demo"
              ref="upload"
              action="/jrc/uploadClassFile"
              :on-success="handleSuccess"
              :on-preview="handlePreview"
              :on-remove="handleRemove"
              :file-list="fileList"
              :auto-upload="false">
              <el-button slot="trigger" size="small" type="primary">选取Class文件</el-button>
        </el-upload>
            
      </el-col>
    </el-row>

    <el-row>
      <el-col :span="2"> 
        <el-button style="margin-left: 10px;" size="small" type="success" @click="submitUpload">上传源码</el-button>
      </el-col>

      <el-col :span="2"> 
        <el-button style="margin-left: 10px;" size="small" type="success" @click="jarUploadDialogVisible = true">上传依赖Jar</el-button>
      </el-col>
    </el-row>

    <el-row>
      <el-col :span="4"> 
        <el-select v-model="selectMethod" placeholder="请选择要执行的方法">
            <el-option v-for="item in javaMethods" :key="item" :label="item" :value="item" >
            </el-option>
          </el-select>
      </el-col>

      <el-col :span="2"> 
          <el-button style="margin-left: 10px;" size="small" type="success" @click="invokeMethod">执行函数</el-button>
      </el-col>
    </el-row>
      
    <el-row>
      <el-col :span="18"> 
        <el-input
            type="textarea"
            :autosize="{ minRows: 1, maxRows: 300}"
            placeholder="调用方法执行结果"
            v-model="result">
          </el-input>
      </el-col>
    </el-row>

    <el-row>
      <el-col :span="18"> 
          <el-input
            type="textarea"
            :autosize="{ minRows: 1, maxRows: 300}"
            placeholder="反编译后Java源码"
            v-model="javacontent">
          </el-input>  
      </el-col>
    </el-row>

    <el-dialog
        title="jar包上传"
        :visible.sync="jarUploadDialogVisible"
        width="30%">

        <el-upload
            :data="data"
            class="upload-demo"
            ref="uploadjar"
            action="/jrc/uploadJarFile"
            :on-success="handleJarSuccess"
            :on-preview="handlePreview"
            :on-remove="handleRemove"
            :auto-upload="false">
            <el-button slot="trigger" size="small" type="primary">选取依赖Jar</el-button>
            <el-button style="margin-left: 10px;" size="small" type="success" @click="submitJarUpload">上传依赖Jar</el-button>
            <el-button style="margin-left: 10px;" size="small" type="success" @click="downloadFile('/static/scripts.zip')">下载环境SDK</el-button>
      </el-upload>

    </el-dialog>
  
  </div>
</template>

<script>
import { httpPost } from "@/api/http_util";

export default {
  
  data() {
    return {
      jarUploadDialogVisible: false,
      sourceTypes: ['编辑Java源码', '上传Java文件', '上传Class文件'],
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
      
      if(this.selectedSourceType == '上传Java文件' ||
          this.selectedSourceType == '上传Class文件') {
          this.$refs.upload.submit();
       } else {
         let query =  {
            javasource : this.javasource
          }
          httpPost(query, '/jrc/uploadJavaSource').then(response=> {
            
            this.$notify({
              title: '执行完成',
              type: 'success',
              message: "",
              duration: 5000
            });

            console.info("/jrc/uploadJavaSource 应答结果: ", response.data)
            
            this.javacontent = response.data.javacontent;
            this.javaMethods = response.data.methods;
            this.key = response.data.key;

            console.info("/jrc/uploadJavaSource method 内容: ", this.javaMethods)

            if(this.javaMethods.length > 0) {
              this.selectMethod = this.javaMethods[0]
            }

          }).catch(err => {
            this.$message.error("/jrc/uploadJavaSource request error  " + err)
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
          httpPost(query, '/jrc/executeMethod').then(res=> {
            
            this.$notify({
              title: '执行完成',
              type: 'success',
              message: "方法: " + this.selectMethod,
              duration: 5000
            });

            this.result = res.data  

          }).catch(err => {
            this.$message.error("/jrc/executeMethod request error  " + err)
            console.error("/jrc/executeMethod    " + err)
          })
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
  text-align: left;
}
 .el-row {
    margin-bottom: 20px;
    &:last-child {
      margin-bottom: 0;
    }
  }
  .el-col {
    border-radius: 4px;
  }
  .bg-purple-dark {
    background: #99a9bf;
  }
  .bg-purple {
    background: #d3dce6;
  }
  .bg-purple-light {
    background: #e5e9f2;
  }
  .grid-content {
    border-radius: 4px;
    min-height: 36px;
  }
  .row-bg {
    padding: 10px 0;
    background-color: #f9fafc;
  }
</style>

