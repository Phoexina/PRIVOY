import Vue from 'vue'
import Router from 'vue-router'
import Index from '../components/Index'
import Home from '../components/Home'
import About from '../components/About'
import Demo from '../components/Demo'
import Help from '../components/Help'
import Search from '../components/Search'
import Benefits from '../components/Benefits'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      redirect: '/Index/Home'
    },
    {
      path: '/Index',
      name: 'Index',
      component: Index,
      children: [
        {
          path: 'Home',
          name: 'Home',
          component: Home
        }, {
          path: 'About',
          name: 'About',
          component: About
        }, {
          path: 'Demo',
          name: 'Demo',
          component: Demo
        }, {
          path: 'Help',
          name: 'Help',
          component: Help
        }, {
          path: 'Benefits',
          name: 'Benefits',
          component: Benefits
        }, {
          path: 'Search',
          name: 'Search',
          component: Search
        }
      ]
    }
  ]
})
