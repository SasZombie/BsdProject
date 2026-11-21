<script setup lang="ts">

import Expenses from './Expenses.vue'
import Payments from './Payments.vue'
import Admin from './Admin.vue'
import { useRoles } from '../composables/useRoles.ts'
import { currentUser } from '../composables/useAuth'
import BaseNavigation from './BaseNavigation.vue'
import type { Tab } from '../types/index.ts'
import { ref } from 'vue'

const renderLogic = useRoles(currentUser.value!.role)

const currentTabs: Tab[] = [
    {
        id: 'expenses',
        label: 'Expenses',
        show: true
    },
    {
        id: 'payments',
        label: 'Payments',
        show: true
        // show: renderLogic.renderPayments
    },
    {
        id: 'admin',
        label: 'Admin',
        show: true
        // show: renderLogic.renderAdmin
    }
].filter((elem) => elem.show === true)

const activeTab = ref('expenses')


</script>

<template>

    <div
        class="pointer-events-none fixed top-0 -left-20 h-full w-72 bg-gradient-to-r from-nuxtGreen/10 to-transparent blur-2xl">
    </div>

    <div
        class="pointer-events-none fixed top-0 -right-20 h-full w-72 bg-gradient-to-l from-nuxtGreen/10 to-transparent blur-2xl">
    </div>

    <div class="px-4 py-4">
        <h3 class="text-2xl font-semibold text-white">Dashboard Overview</h3>
    </div>
    <div class="h-px w-full bg-gradient-to-r from-transparent via-nuxtGreen/50 to-transparent"></div>

    <div v-if="currentUser" class="w-full space-y-6 px-4 py-5">

        <!-- Expenses -->
        <BaseNavigation v-model:active-tab="activeTab" :tabs="currentTabs" />
        <div class="h-px w-full bg-gradient-to-r from-transparent via-nuxtGreen/50 to-transparent"></div>

        <KeepAlive>
            <Expenses v-if="activeTab === 'expenses'" :current-user="currentUser" :extra-options="true" />
        </KeepAlive>
        <!-- Payments -->

        <KeepAlive>
            <Payments v-if="activeTab === 'payments' && renderLogic.renderPayments" :current-user="currentUser!!"
                :extra-options="true" />
        </KeepAlive>

        <!-- Admin Options -->
        <KeepAlive>
            <Admin v-if="activeTab === 'admin' && renderLogic.renderAdmin" :current-user="currentUser!!"
                :extra-options="true" />
        </KeepAlive>

    </div>


</template>