<template>
  <div>
  <el-card class="box-card">
    <div slot="header" class="clearfix">
      <span style="margin-left: 700px;font-size: 1.9rem;font-weight:bold">API参考文档</span>
      <el-button style="float: right; padding: 3px 0;font-size: 1.3rem;font-weight:bold" type="text">下载PRIVOY</el-button>
    </div>
    <div>
      <vue-markdown class="markdown-body">{{msg}}</vue-markdown>
    </div>
  </el-card>
    <el-footer style="text-align: center;font-size: 0.5rem;margin-top: 90px">
      Copyright© 2022 PRIVOY All Rights Reserved
    </el-footer>
  </div>
</template>

<script>
import README from '../../README.md'
import VueMarkdown from 'vue-markdown'
import 'github-markdown-css'
export default {
  name: 'Help',
  components: {
    // eslint-disable-next-line no-undef
    VueMarkdown
  },
  data () {
    return {
      msg: README
    }
  }
}
</script>

<style scoped>
.markdown-body {
  padding: 20px;
  min-width: 300px;
  max-width: 1500px;
  font-size: 20px;
  h2 {
    font-size: 18px;
    margin: 1em 0 15px;
    padding-top: 0.8em;
    padding-bottom: 0.8em;
  }
  h3 {
    font-size: 14px;
    margin: 22px 0 16px;
  }
  h4 {
    font-size: 13px;
    margin: 20px 0 16px;
  }
  h5 {
    font-size: 15px;
    margin: 16px 0 16px;
    font-weight: 700;
  }
  p {
    font-size: 15px;
    line-height: 24px;
    color: #666666;
    margin-top: 0px;
    margin: 8px 0;
    margin: 14px 0 14px;
  }
  pre {
    background-color: #eee;
    margin-bottom: 8px;
    margin-top: 8px;
    margin: 12px 0 12px;
  }
  blockquote {
    margin-bottom: 8px;
    margin-top: 8px;
    margin: 14px 0 14px;
    background-color: #eee;
    padding: 16px 16px;
  }
  tr {
    background-color: #f5f5f5;
  }
  code {
    background-color: #eee;
  }
  ul, ol, li {
    list-style: unset;
    font-size: 12px;
    line-height: 20px;
    color: #666666;
    margin-top: 0px;
    margin: 8px 0;
  }
  blockquote {
    border-color: #48b6e2;
  }
  table {
    display: table;
    width: 100%;
    max-width: 100%;
    margin-bottom: 20px;
  }
  }
</style>
