<template>
  <div :style="conTop" style="height: 750px;width: 1580px;position: fixed;margin-top: 0px">
    <div id="chart" style="height: 300px;width: 500px;margin-top: 30px;margin-left: 360px;"></div>
    <el-card class="box-card" style="width: 1000px;margin-left: 280px;margin-top: 70px">
      <div slot="header" class="clearfix">
        <span>查询隐私使用情况</span>
      </div>
      <el-row style="margin-top: 20px">
        <el-input v-model="input" placeholder="请输入用户ID" style="width: 400px;margin-left: 220px"></el-input>
        <el-button type="primary" @click="disp()">查询</el-button>
      </el-row>
<!--      <div id="chart1" style="height: 300px;width: 500px;margin-top: 30px;margin-left: 270px;display: none"></div>-->
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'Search',
  data () {
    return {
      input: '',
      is_in: 'none',
      conTop: {
        // backgroundImage: 'url(' + require('../assets/search.jpeg') + ')',
        // backgroundRepeat: 'no-repeat',
        // backgroundSize: 'cover'
      }
    }
  },
  mounted () {
    this.drawChart()
  },
  methods: {
    disp () {
      if (this.input !== '' && this.input != null) {
        let block = document.getElementById('chart')
        block.style.display = ''
      } else {
        this.$message.error('请输入用户ID')
      }
    },
    drawChart () {
      // 基于准备好的dom，初始化echarts实例  这个和上面的main对应
      let myChart = this.$echarts.init(document.getElementById('chart'),null,{
        width: 800,
        height: 400
      })
      // 指定图表的配置项和数据
      // let option = {
      //   legend: {
      //     // Try 'horizontal'
      //     orient: 'vertical',
      //     right: 10,
      //     top: 'center'
      //   },
      //   title: {
      //     text: '使用tokens情况'
      //   },
      //   xAxis: {
      //     type: 'category',
      //     data: [ '6-12', '6-13', '6-14', '6-15', '6-16' ],
      //     name: '日期'
      //   },
      //   yAxis: {
      //     type: 'value',
      //     name: '使用数量'
      //   },
      //   series: [
      //     {
      //       data: [120, 200, 150, 200, 150],
      //       type: 'line',
      //       name: 'subject1'
      //     }, {
      //       data: [200, 150, 120, 100, 120],
      //       type: 'line',
      //       name: 'subject2'
      //     }, {
      //       data: [150, 190, 130, 220, 190],
      //       type: 'line',
      //       name: 'subject3'
      //     }, {
      //       data: [160, 105, 190, 100, 130],
      //       type: 'line',
      //       name: 'subject4'
      //     }
      //   ]
      // }
      let option = {
        legend: {
          // Try 'horizontal'
          orient: 'vertical',
          right: 10,
          top: 'center'
        },
        title: {
          text: this.input + '查询结果'
        },
        xAxis: {
          type: 'category',
          data: [ '6-12', '6-13', '6-14', '6-15', '6-16' ],
          name: '日期'
        },
        yAxis: {
          type: 'value',
          name: '使用数量'
        },
        series: [
          {
            data: [20, 30, 50, 80, 10],
            type: 'line',
            name: 'subject1'
          }, {
            data: [10, 50, 20, 10, 0],
            type: 'line',
            name: 'subject2'
          }, {
            data: [20, 10, 30, 20, 90],
            type: 'line',
            name: 'subject3'
          }, {
            data: [60, 5, 90, 0, 30],
            type: 'line',
            name: 'subject4'
          }
        ]
      }
      // 使用刚指定的配置项和数据显示图表。
      myChart.setOption(option)
    }
  }
}
</script>

<style scoped>

</style>
