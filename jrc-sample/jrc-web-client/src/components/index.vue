<template>
  <div class="app-container">
    <el-row>
      <el-col :span="3">
        <el-tag>Java脚本执行器.</el-tag>
        <el-tag type="danger">不支持方法重写.</el-tag>
      </el-col>
    </el-row>

    <el-row>
      <el-col :span="2">
        <el-select v-model="selectedSourceType" placeholder="请选择源码操作类型">
          <el-option v-for="item in sourceTypes" :key="item" :label="item" :value="item"/>
        </el-select>
      </el-col>

      <el-col :span="2">
        <el-button style="margin-left: 10px;" size="small" type="success" @click="jarUploadDialogVisible = true">
          上传依赖Jar
        </el-button>
      </el-col>

      <el-col :span="2">
        <el-button style="margin-left: 10px;" size="small" type="success" @click="jarDownloadDialogVisible = true">
          从仓库下载依赖Jar
        </el-button>
      </el-col>

    </el-row>

    <el-row>
      <el-col :span="18">
        <el-input
            v-show="selectedSourceType == '编辑Java源码'"
            type="textarea"
            :autosize="{ minRows: 5, maxRows: 300}"
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
      <el-col :span="9">
        <el-select v-model="selectClass" placeholder="请选择要执行类" @change='changeClass' style="width:100%">
          <el-option v-for="item in classes" :key="item" :label="item" :value="item">
          </el-option>
        </el-select>
      </el-col>
      <el-col :span="3">
        <el-select v-model="selectVersion" placeholder="请选择要执行的版本" @change='changeVersion'>
          <el-option v-for="item in versions" :key="item" :label="item" :value="item">
          </el-option>
        </el-select>
      </el-col>
      <el-col :span="2.7">
        <el-select v-model="selectMethod" placeholder="请选择要执行的方法">
          <el-option v-for="item in javaMethods" :key="item" :label="item" :value="item">
          </el-option>
        </el-select>
      </el-col>

      <el-col :span="2">
        <el-button style="margin-left: 10px;" size="small" type="success" @click="submitUpload">上传源码</el-button>
      </el-col>

      <el-col :span="1">
        <el-button size="small" type="success" @click="executeMethod">执行函数</el-button>
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

    <el-dialog
        title="从仓库下载依赖Jar"
        :visible.sync="jarDownloadDialogVisible"
        width="70%">

        <el-row>
          <el-col :span="15">
             <el-input
                type="text"
                placeholder="搜索关键字"
                v-model="searchJarKeyword">
            </el-input>
          </el-col>

          <el-col :span="2">
             <el-button style="margin-left: 10px;" size="small" type="success" @click="searchJar">搜索</el-button>
          </el-col>
        </el-row>
       		 		
        <el-table
          :data="jarList"
          border
          style="width: 100%">
          <el-table-column
            fixed
            prop="g"
            label="GroupId">
          </el-table-column>
          
          <el-table-column
            prop="a"
            label="ArtifactId">
          </el-table-column>

          <el-table-column
            prop="latestVersion"
            label="Latest Version">
          </el-table-column>

          <el-table-column
            prop="timestamp"
            label="Updated">
          </el-table-column>
          
          <el-table-column
            fixed="right"
            label="操作"
            width="100">
            <template slot-scope="scope">
              <el-button @click="downloadJar(scope.row)" type="text" size="small">下载</el-button>
            </template>
          </el-table-column>
        </el-table>
    </el-dialog>
  
  </div>
</template>

<script>
import {httpDataPost, httpParamPost} from "@/api/http_util";

export default {
  
  data() {
    return {
      jarUploadDialogVisible: false,
      jarDownloadDialogVisible: false,
      sourceTypes: ['编辑Java源码', '上传Java文件', '上传Class文件'],
      selectedSourceType: '编辑Java源码',
      classInfoList: [],
      classes: [],
      versions: [],
      javaMethods: [],
      selectClass: null,
      selectVersion: null,
      selectMethod: null,
      data: {
        table: "",
        skipFields: ""
      },
      javasource: "",
      result: "",
      javacontent: "",
      fileList: [],
      searchJarKeyword: "",
      jarList: []
    };
  },
  mounted() {
    this.queryClassInfo();
  },
  methods: {
    submitUpload() {
      this.result = ""
      this.javacontent = ""

      if (this.selectedSourceType == '上传Java文件' ||
          this.selectedSourceType == '上传Class文件') {
        this.$refs.upload.submit();
      } else {
        httpDataPost(this.javasource, '/jrc/uploadJavaSource').then(response => {
            
            this.$notify({
              title: '执行完成',
              type: 'success',
              message: "",
              duration: 5000
            });

            this.queryClassInfo()
            this.decompile()
          }).catch(err => {
            this.$message.error("/jrc/uploadJavaSource request error  " + err)
          })
       }
        
      },
      queryClassInfo() {
        let query = {}
        httpParamPost(query, '/jrc/getClassVersionMethods').then(response => {
          this.classInfoList = response.data;
          console.info("getClassVersionMethods -> ", this.classInfoList)
          if (this.classInfoList.length > 0) {
            const firstClassInfo = this.classInfoList[0]
            this.selectClass = firstClassInfo.className
            this.selectVersion = firstClassInfo.versions[0].version
            this.selectMethod = firstClassInfo.versions[0].methodNames[0]

            console.info("selectClass ->", this.selectClass)
            console.info("selectVersion ->", this.selectVersion)
            console.info("selectMethod ->", this.selectMethod)

            this.changeReset()

          }
        }).catch(err => {
          this.$message.error("/jrc/getClassVersionMethods request error  " + err)
        })
      },
    changeReset() {
      this.classes = this.classInfoList.map(function (value) {
        return value.className
      })
      console.info("classes ->", this.classes)

      const selectedClassName = this.selectClass
      var classInfo = this.classInfoList.filter(function (ci) {
        return ci.className == selectedClassName
      })
      console.info("classInfo ->", classInfo)

      if (classInfo && classInfo.length > 0) {
        this.versions = classInfo[0].versions.map(function (versionInfo) {
          return versionInfo.version
        })
        console.info("versions ->", this.versions)
      } else {
        return
      }

      const selectedVersion = this.selectVersion
      var versionInfo = classInfo[0].versions.filter(function (versionInfo) {
        return versionInfo.version == selectedVersion
      })
      console.info("versionInfo ->", versionInfo)

      if (versionInfo && versionInfo.length > 0) {
        this.javaMethods = versionInfo[0].methodNames
        console.info("methods ->", this.javaMethods)
      }
    },
    changeClass() {
      this.changeReset()
      this.decompile()
    },
    changeVersion() {
      this.changeReset()
      this.decompile()
    },
    decompile() {
      if (!this.selectClass) {
        return
      }
      let query = {
        className: this.selectClass,
        version: this.selectVersion
      }
      console.info("decompile -> ", query)
      httpParamPost(query, '/jrc/decompile').then(response => {
        this.javacontent = response.data.source;
      }).catch(err => {
        this.$message.error("/jrc/decompile request error  " + err)
      })
      },
      handleRemove(file, fileList) {
        console.log(file, fileList);
      },
      handlePreview(file) {
        console.log(file);
      },
      handleSuccess(response, file, fileList) {
         this.$notify({
              title: '执行完成',
              type: 'success',
              message: "",
              duration: 5000
            });
            
            this.versions = response.data.versions;

            if(this.versions.length > 0) {
              this.selectVersion = this.versions[0]
            }

            this.queryClassInfo()
            this.decompile()
      },
    executeMethod() {
      let query = {
        method: this.selectMethod,
        className: this.selectClass,
        version: this.selectVersion
      }
      httpParamPost(query, '/jrc/executeMethod').then(res => {

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
      },
      searchJar() {
        let query =  {
            searchJarKeyword : this.searchJarKeyword
        }
        httpParamPost(query, '/jrc/searchJar').then(response=> {
          var responseData = JSON.parse(response.data);
          this.jarList = responseData.response.docs;
        }).catch(err => {
            this.$message.error("/jrc/searchJar request error  " + err)
        })
      },
      downloadJar(row) {
        httpDataPost(row, '/jrc/downloadJar').then(response=> {
          
        }).catch(err => {
            this.$message.error("/jrc/downloadJar request error  " + err)
        })
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

