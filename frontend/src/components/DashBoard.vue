<script setup lang="ts">

import Expenses from './Expenses.vue'
import Payments from './Payments.vue'
import Admin from './Admin.vue'
import { useRoles } from '../composables/useRoles.ts'
import { currentUser } from '../composables/useAuth'

const renderLogic = useRoles(currentUser.value!.role)


</script>

<template>

    <div v-if="currentUser">
        <!-- Expenses -->
        <Expenses :current-user="currentUser" :extra-options="renderLogic.renderExpenseExtra" />

        <!-- Payments -->
        <Payments v-if="renderLogic.renderPayments" :current-user="currentUser!!"
            :extra-options="renderLogic.renderPaymentsExtra" />

        <!-- Admin Options -->
        <Admin v-if="renderLogic.renderAdmin" :current-user="currentUser!!" :extra-options="false" />
    </div>

</template>