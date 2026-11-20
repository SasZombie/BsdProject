<!-- src/components/BaseInput.vue -->
<script setup lang="ts">
const model = defineModel<string | number>()

defineProps<{
  label?: string
  placeholder?: string
  type?: 'text' | 'number' | 'password' | 'email'
  error?: string | null
}>()
</script>

<template>
  <div class="flex flex-col gap-1.5 w-full">
    <label v-if="label" class="text-xs font-semibold uppercase tracking-wider text-slate-400">
      {{ label }}
    </label>

    <input
      v-model="model"
      :type="type || 'text'"
      :placeholder="placeholder"
      :class="[
        'w-full px-3.5 py-2 text-sm rounded-lg transition outline-none',
        'bg-slate-950 text-slate-100 placeholder-slate-500',
        'border focus:ring-2',

        error
          ? 'border-rose-500/80 focus:border-rose-500 focus:ring-rose-500/20'
          : 'border-slate-800 focus:border-indigo-500 focus:ring-indigo-500/20'
      ]"
    />

    <span v-if="error" class="text-xs text-rose-400 font-medium">
      {{ error }}
    </span>
  </div>
</template>