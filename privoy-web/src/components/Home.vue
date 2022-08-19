<template>
  <div>
<!--    <el-main>Main</el-main>-->
<!--    <el-footer>Footer</el-footer>-->
    <div class="left" style="width: 48%;float: left;text-align: right;margin-top: 50px">
      <img src="../assets/lock2.svg">
    </div>
    <div class="right" style="width: 48%;float: right;text-align: center">
      PRIVOY<br>
      一个基于密码学<br>
      流数据的隐私管理第三方<br>
      管理平台
    </div>
    <el-row style="text-align: center">
      <el-button type="success" size="large" round style="font-size: 1.8rem;letter-spacing: .05em" @click="$router.push('/Index/Benefits')">WHY PRIVOY</el-button>
      <el-button type="primary" size="large" round style="font-size: 1.8rem;letter-spacing: .05em" @click="$router.push('/Index/Help')">START PRIVOY</el-button>
    </el-row>
    <el-footer style="text-align: center;font-size: 0.5rem;margin-top: 90px">
      Copyright© 2022 PRIVOY All Rights Reserved
    </el-footer>
  </div>
</template>

<script>
export default {
  name: 'Home',
  data () {
    return {}
  }
}
</script>

<style scoped>
.right{
  font-size: 3.2rem;
  padding: 0;
  margin: 0;
  border-bottom: 0;
  font-weight: 300;
  line-height: 1.45;
  margin-block-start: 0.83em;
  margin-block-end: 0.83em;
  margin-inline-start: 0px;
  margin-inline-end: 0px;
  margin-top: 90px;
  display: block;
}
</style>
