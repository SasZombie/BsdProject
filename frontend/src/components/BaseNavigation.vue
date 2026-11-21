<script setup lang="ts">
import type { Tab } from '../types';
const props = defineProps<{
    tabs: Tab[]
}>()

const activeTab = defineModel<string>('activeTab', { required: true })

</script>

<template>

    <div class="flex items-center gap-2 border-nuxtGreen/30 pb-3">
        <button v-for="tab in tabs" :key="tab.id" type="button" @click="activeTab = tab.id" :class="[
            'relative px-4 py-2 text-sm font-medium rounded-lg transition-all duration-200 outline-none cursor-pointer',
            activeTab === tab.id
                ? 'bg-nuxtGreen/10 text-nuxtGreen border border-nuxtGreen/40 shadow-[0_0_12px_rgba(0,220,130,0.2)]'
                : 'text-white hover:text-white hover:bg-nuxtGreen/10 border border-nuxtGreen/30']">
            {{ tab.label }}
        </button>
    </div>

</template>