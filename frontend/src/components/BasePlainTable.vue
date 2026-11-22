<script setup lang="ts" generic="T extends Record<string, any>">
import { computed } from 'vue'

const props = defineProps<{
    data: T[]
}>()

const headers = computed<string[]>(() => {
    if (!props.data || props.data.length === 0) return []
    return Object.keys(props.data[0])
})

const formatHeader = (key: string): string => {
    return key
        .replace(/([A-Z])/g, ' $1')
        .replace(/^./, (str) => str.toUpperCase())
        .trim()
}

const formatCellValue = (val: any): string => {
    if (val === null || val === undefined) return '—'

    if (typeof val === 'string' && /^\d{4}-\d{2}-\d{2}T/.test(val)) {
        const date = new Date(val)
        if (!isNaN(date.getTime())) {
            return new Intl.DateTimeFormat('en-RO', {
                year: 'numeric',
                month: 'short',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
                hour12: false,
            }).format(date)
        }
    }

    return String(val)
}

</script>

<template>
    <div class="w-full overflow-hidden rounded-xl border border-slate-800 bg-nuxtBlack shadow-xl">
        <div class="overflow-x-auto">
            <table class="w-full text-left text-sm text-slate-300">

                <thead class="border-b border-slate-800 bg-slate-900/60 text-xs uppercase tracking-wider text-white">
                    <tr>
                        <th v-for="header in headers" :key="header" scope="col"
                            class="px-6 py-3.5 font-medium capitalize">
                            {{ formatHeader(header) }}
                        </th>
                    </tr>
                </thead>

                <tbody class="divide-y divide-slate-800/60">
                    <tr v-for="row in data" :key="row.id ?? row.reason" class="hover:transition-colors hover:bg-slate-800/30">
                        <td v-for="header in headers" :key="header"
                            class="whitespace-nowrap px-6 py-4 font-mono text-nuxtGreen">
                            {{ formatCellValue(row[header]) ?? '-' }}
                        </td>
                    </tr>

                    <tr v-if="data.length === 0">
                        <td class="px-6 py-8 text-center text-white">
                            No personal expenses pending
                        </td>
                    </tr>
                </tbody>

            </table>
        </div>
    </div>
</template>