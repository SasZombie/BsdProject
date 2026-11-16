<script setup lang="ts">
import { ref, watch } from 'vue'
import type { Role, BackEndUser } from '../types'

const props = defineProps<{
  user: BackEndUser
  roles: Role[]
}>()

const emit = defineEmits<{
  (e: 'update', updatedUser: BackEndUser): void
}>()

const form = ref<BackEndUser>({ ...props.user })

watch(() => props.user, (newUser) => {
  form.value = { ...newUser }
}, { deep: true })

const submitForm = () => {
  emit('update', form.value)
}
</script>

<template>
  <form @submit.prevent="submitForm">
    <p> {{form.name}}  {{form.surname}} {{form.username}}

    <input v-model="form.reportsTo" type="text" placeholder="Reports To" />

    <select v-model="form.role">
      <option v-for="role in roles" :key="role" :value="role">
        {{ role }}
      </option>
    </select>

    <button type="submit">Update User</button>
  </p>
  </form>
</template>