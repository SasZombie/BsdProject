<script setup lang="ts">
import { ref } from 'vue'
import { useAuth } from '../composables/useAuth'

const username = ref('AdminUsername')
const password = ref('1')

const { login, error, loading } = useAuth()
</script>

<template>
  <form @submit.prevent="login(username, password)">
    <input v-model="username" type="text" placeholder="Username"  />
    <input v-model="password" type="password" placeholder="Password" />
    <button :disabled="loading" type="submit">Log In</button>
    <p v-if="error">{{ error }}</p>
  </form>
</template>