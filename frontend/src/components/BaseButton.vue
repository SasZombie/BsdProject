<script setup lang="ts">
withDefaults(
  defineProps<{
    text: string
    type?: 'button' | 'submit' | 'reset'
  }>(),
  {
    type: 'submit'
  }
)
</script>

<template>
  <button 
    :type="type" 
    :class="[
      'px-3.5 py-2 text-sm rounded-lg transition outline-none cursor-pointer',
      'bg-nuxtBlack placeholder-nuxtGreen/30',
      'border border-nuxtGreen/30 hover:bg-nuxtGreen/10 focus:ring-1 focus:ring-nuxtGreen'
    ]"
  >
    {{ text }}
  </button>
</template>