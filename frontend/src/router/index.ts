import { createRouter, createWebHistory } from 'vue-router'
import LogIn from '../components/LogIn.vue' 
import DashBoard from '../components/DashBoard.vue' 

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'login',
      component: LogIn
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: DashBoard
    }

  ]
})

export default router