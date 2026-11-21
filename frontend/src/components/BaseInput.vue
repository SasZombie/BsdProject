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
    <label v-if="label" class="text-xs font-semibold uppercase tracking-wider text-white">
      {{ label }}
    </label>

    <input
      v-model="model"
      :type="type || 'text'"
      :placeholder="placeholder"
      :class="[
        'w-full px-3.5 py-2 text-sm rounded-lg transition outline-none',
        'bg-nuxtBlack placeholder-nuxtGreen/60 caret-nuxtGreen',
        'border focus:ring-1',

        error
          ? 'border-rose-500/80 focus:border-rose-500 focus:ring-rose-500/20'
          : 'border-nuxtGreen/20 focus:border-nuxtGreen/40 focus:ring-nuxtGreen'
      ]"
    />

    <span v-if="error" class="text-xs text-rose-400 font-medium">
      {{ error }}
    </span>
  </div>
</template>