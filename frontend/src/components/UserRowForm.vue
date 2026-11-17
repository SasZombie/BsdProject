<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import type { Role, BackEndUser } from '../types'

const props = defineProps<{
  user: BackEndUser
  roles: Role[]
  allUsers: BackEndUser[]
}>()

const emit = defineEmits<{
  (e: 'update', updatedUser: BackEndUser): void
}>()

const form = ref<BackEndUser>({ ...props.user })

watch(
  () => props.user,
  (newUser) => {
    form.value = { ...newUser }
  },
  { deep: true }
)

const availableManagers = computed(() => {
  return props.allUsers.filter((u) => u.id !== props.user.id)
})

const submitForm = () => {
  emit('update', form.value)
}
</script>

<template>
  <form @submit.prevent="submitForm">
    <span>{{ form.name }} {{ form.surname }} ({{ form.username }})</span>

    <select v-model="form.reportsTo">
      <option :value="null">None</option>
      <option 
        v-for="manager in availableManagers" 
        :key="manager.id" 
        :value="manager.username"
      >
        {{ manager.name }} {{ manager.surname }} ({{ manager.username }})
      </option>
    </select>

    <select v-model="form.role">
      <option v-for="role in roles" :key="role" :value="role">
        {{ role }}
      </option>
    </select>

    <button type="submit">Update User</button>
  </form>
</template>