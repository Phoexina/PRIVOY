<template>
  <div>
    <el-container>
      <el-header>
        <el-menu
          :default-active="activeIndex"
          class="el-menu-demo"
          mode="horizontal"
          @select="handleSelect"
          background-color="white"
          text-color="black"
          router>
          <el-menu-item index="/Index/Home">PRIVOY</el-menu-item>
          <el-submenu index="2">
            <template slot="title">学习使用</template>
<!--            <el-menu-item index="/Index/Benefits">平台优势</el-menu-item>-->
            <el-menu-item index="/Index/Help">使用文档</el-menu-item>
            <el-menu-item index="/Index/Demo">参考案例</el-menu-item>
          </el-submenu>
          <el-menu-item index="/Index/Search">服务查询</el-menu-item>
          <el-menu-item index="/Index/About">关于我们</el-menu-item>
        </el-menu>
      </el-header>
      <el-main>
        <router-view></router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script>
export default {
  name: 'Index',
  data () {
    return {
      activeIndex: '1'
    }
  },
  methods: {
    handleSelect (key, keyPath) {
      console.log(key, keyPath)
    }
  }
}
</script>

<style scoped>
  @font-face {
    font-family: "PingFang SC"
  }
</style>
