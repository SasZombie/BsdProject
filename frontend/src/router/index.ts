import { createRouter, createWebHistory } from "vue-router";
import LogIn from "../components/LogIn.vue";
import DashBoard from "../components/DashBoard.vue";
import { currentUser } from '../composables/useAuth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/",
      redirect: "/login",
    },
    {
      path: "/login",
      name: "login",
      component: LogIn,
    },
    {
      path: "/dashboard",
      name: "dashboard",
      component: DashBoard,

      meta: { requiresAuth: true },
    },
  ],
});

router.beforeEach((to, _from) => {
  if (to.meta.requiresAuth) {
    if (!currentUser.value) {
      return "/" 
    }
  }

  if (to.path === '/' && currentUser.value) {
    return "/dashboard"
  }

})

export default router;
