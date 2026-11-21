<script setup lang="ts">
import { ref } from 'vue';
import { useExpenses } from '../composables/useExpenses';
import type { Expense, Status, Tab, UserProps } from '../types';
import BaseInput from './BaseInput.vue';
import BaseButton from './BaseButton.vue';
import BaseNavigation from './BaseNavigation.vue';
import BasePlainTable from './BasePlainTable.vue';
import BaseExtenedTable from './BaseExtenedTable.vue';
import { currentUser } from '../composables/useAuth.ts';

const props = defineProps<UserProps>()


const reason = ref('I want money cuz I can');
const ammountDollars = ref(67);

const {
    postExpense,
    decideExpense,
    error,
    loading,
    getPersonalExpenses,
    getExpensesToDecide,
    personalExpenses,
    expensesToDecide,
    extenedExpenses
} = useExpenses();


getPersonalExpenses(props.currentUser.token);
getExpensesToDecide(props.currentUser.token);

const currentTabs: Tab[] = [
    {
        id: 'activePersonal',
        label: 'Active Personal Expenses',
        show: true
    },
    {
        id: 'pastPersonal',
        label: 'Past Personal Expenses',
        show: true
    },
    {
        id: 'toDecide',
        label: 'Active Expenses to Decide',
        show: true
    },
    {
        id: 'pastToDecide',
        label: 'Past expenses Decided',
        show: true
    }
].filter((elem) => elem.show === true)

const activeTab = ref('activePersonal')


const onApprove = (index: number, decision: Status) => {

  const expense = extenedExpenses.value[index]
  decideExpense(currentUser.value!!.token, expense.expense, decision)
}

</script>

<template>

<form @submit.prevent="postExpense(ammountDollars, reason, currentUser!!.token)" 
  class="flex flex-wrap items-center gap-2.5 max-w-2xl"
>
<div class="px-1 py-2">
    <h3 class="text-base font-semibold text-white tracking-wide">
      New Expense
    </h3>
 
  </div>
  <div class="flex-1 min-w-[180px]">
    <BaseInput v-model="reason" type="text" placeholder="Reason" />
  </div>

  <div class="w-33">
    <BaseInput v-model="ammountDollars" type="number" placeholder="Amount ($)" />
  </div>

  <div class="">
    <BaseButton text="Submit Request" :disabled="loading" type="submit" />
  </div>

  <p v-if="error" class="w-full text-xs text-red-400 mt-1">{{ error }}</p>
</form>

    <BaseNavigation v-model:active-tab="activeTab" :tabs="currentTabs" />

    <div class="h-px w-full bg-gradient-to-r from-transparent via-nuxtGreen/50 to-transparent"></div>

    <BasePlainTable v-if="activeTab === 'activePersonal'" :data="personalExpenses"/>
    
    <BasePlainTable v-if="activeTab === 'pastPersonal'" :data="personalExpenses"/>

    <BaseExtenedTable @action="onApprove" v-if="activeTab === 'toDecide'" :data="expensesToDecide"/>
    <BaseExtenedTable v-if="activeTab === 'pastToDecide'" :data="personalExpenses"/>

</template>