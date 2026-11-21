<script setup lang="ts">
import { ref } from 'vue'
import { useAuth } from '../composables/useAuth'
import BaseInput from './BaseInput.vue'
import BaseButton from './BaseButton.vue'

const username = ref('AdminUsername')
const password = ref('1')

const { login, error } = useAuth()

const nodes = [
  { pos: '5%', size: 'w-3 h-3' },
  { pos: '15%', size: 'w-5 h-5' },
  { pos: '25%', size: 'w-8 h-8' },
  { pos: '35%', size: 'w-12 h-12' },

  { pos: '65%', size: 'w-12 h-12' },
  { pos: '75%', size: 'w-8 h-8' },
  { pos: '85%', size: 'w-5 h-5' },
  { pos: '95%', size: 'w-3 h-3' },
]
</script>

<template>

  <div class="absolute top-1/4 left-0 h-px w-full bg-gradient-to-r from-transparent via-nuxtGreen/40 to-transparent">

    <div
      class="absolute left-[12%] -top-3 h-6 w-6 -translate-x-1/2 rounded-full border border-nuxtGreen/40 bg-nuxtBlack/80 flex items-center justify-center">
      <div class="h-1.5 w-1.5 rounded-full bg-nuxtGreen"></div>
    </div>

    <div class="absolute right-[28%] -top-4 h-8 w-8 translate-x-1/2 flex items-center justify-center">

      <div class="absolute inset-0 rounded-full border border-dashed border-nuxtGreen/50"></div>

      <div class="h-4 w-4 rounded-full border border-nuxtGreen bg-nuxtBlack"></div>
      <div class="absolute h-1.5 w-1.5 rounded-full bg-nuxtGreen/80"></div>
    </div>

  </div>

  <div
    class="absolute top-1/2 left-0 h-px w-full bg-gradient-to-r from-transparent via-nuxtGreen to-transparent shadow-[0_0_15px_rgba(99,102,241,0.5)]">
    <div v-for="(node, index) in nodes" :key="index" :style="{ left: node.pos }" :class="[
      'absolute top-1/2 -translate-x-1/2 -translate-y-1/2',
      node.size
    ]">
      <span class="absolute inline-flex h-full w-full animate-ping rounded-full bg-nuxtGreen opacity-60"></span>
      <span class="absolute inline-flex h-full w-full rounded-full border border-nuxtGreen bg-nuxtBlack"></span>
    </div>
  </div>

  <div class="absolute top-3/4 left-0 h-px w-full bg-gradient-to-r from-transparent via-nuxtGreen/30 to-transparent">
    <div class="absolute left-[65%] -top-2 flex h-4 w-4 -translate-x-1/2 items-center justify-center">
      <div class="absolute h-4 w-px bg-nuxtGreen/60"></div>
      <div class="absolute h-2 w-2 rounded-full border border-nuxtGreen bg-nuxtBlack"></div>
    </div>

  </div>

  <div class="flex min-h-screen items-center justify-center p-4">
    <div
      class="relative w-full max-w-md rounded-xl border border-nuxtGreen p-8 shadow-2xl shadow-nuxtGreen/30 backdrop-blur-md">

      <div class="flex flex-col items-center space-y-4">

        <h2 class="w-full text-center text-xl font-bold tracking-tight text-white">
          Login
        </h2>

        <form @submit.prevent="login(username, password)" class="flex w-full flex-col gap-4">
          <BaseInput spellcheck="false" v-model="username" type="text" placeholder="Username" />
          <BaseInput v-model="password" type="password" placeholder="Password" />

          <p v-if="error" class="text-center text-xs text-red-400">
            {{ error }}
          </p>

          <BaseButton text="Sign In" />
        </form>

      </div>
    </div>
  </div>

</template>