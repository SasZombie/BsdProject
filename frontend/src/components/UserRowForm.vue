<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import type { Role, BackEndUser } from '../types'
import BaseButton from './BaseButton.vue';

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
  <form @submit.prevent="submitForm" class="flex items-center gap-3 w-full py-2 px-3 rounded-lg border border-nuxtGreen/30 bg-nuxtGreen/10">
    <div class="flex-1 min-w-[200px] truncate">
      <span class="text-sm font-medium text-white">
        {{ form.name }} {{ form.surname }}
      </span>
    </div>

    <div class="relative w-52 shrink-0">
      <select 
        v-model="form.reportsTo"
        class="w-full appearance-none rounded-lg border border-nuxtGreen/20 bg-nuxtBlack/90 py-1.5 pl-3 pr-8 text-sm text-white focus:border-nuxtGreen focus:outline-none focus:ring-1 focus:ring-nuxtGreen transition-colors truncate"
      >
        <option :value="null">Reports to: None</option>
        <option v-for="manager in availableManagers" :key="manager.id" :value="manager.username">
          {{ manager.name }} {{ manager.surname }} ({{ manager.username }})
        </option>
      </select>
      <div class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2.5 text-zinc-400">
        <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
        </svg>
      </div>
    </div>

    <div class="relative w-40 shrink-0">
      <select 
        v-model="form.role"
        class="w-full appearance-none rounded-lg border border-nuxtGreen/20 bg-nuxtBlack/90 py-1.5 pl-3 pr-8 text-sm text-white focus:border-nuxtGreen focus:outline-none focus:ring-1 focus:ring-nuxtGreen transition-colors"
      >
        <option v-for="role in roles" :key="role" :value="role">
          {{ role }}
        </option>
      </select>
      <div class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2.5 text-zinc-400">
        <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
        </svg>
      </div>
    </div>

    <div class="shrink-0">
      <BaseButton text="Update" type="submit" class="px-3 py-1.5 text-sm" />
    </div>
  </form>
</template>