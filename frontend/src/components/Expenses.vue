<script setup lang="ts">
import { ref } from 'vue';
import { useExpenses } from '../composables/useExpenses';
import type { Status, Tab, UserProps } from '../types';
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
  vetoExpense,
  error,
  loading,
  getPersonalExpenses,
  getExpensesDecisions,
  activePersonalExpenses,
  pastPersonalExpenses,
  expensesToDecide,
  expensesDecided,
  getAllExpenses,
  allExpenses,
} = useExpenses();


getPersonalExpenses(props.currentUser.token);
getExpensesDecisions(props.currentUser.token);
getAllExpenses(props.currentUser.token)

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
  },
  {
    id: 'allExpenses',
    label: 'All expenses done',
    show: true
  }
].filter((elem) => elem.show === true)

const activeTab = ref('activePersonal')


const onAction = async (index: number, decision: Status) => {

  const saved = [...expensesToDecide.value]
  const expense = expensesToDecide.value[index]
  expensesToDecide.value.splice(index, 1)

  console.log(expense.id)
  await decideExpense(currentUser.value!!.token, expense.id, decision)


  if (error.value !== null) {
    expensesToDecide.value = saved
  }
}

const onAllExpenses = async (index: number, status: Status) => {
  status;
  const expense = allExpenses.value[index]

  console.log(expense.id)

  await vetoExpense(currentUser.value!!.token, expense.id)
}

const showButton = ref(false)
const buttonText = ref("Add New Expense")

const handleShowButton = () => {
  showButton.value = !showButton.value

  if (showButton.value) {
    buttonText.value = "Close New Expense"
  }
  else {
    buttonText.value = "Add New Expense"
  }


}


</script>

<template>

  <BaseButton @click="handleShowButton" :text="buttonText" />

  <div v-show="showButton">
    <form @submit.prevent="postExpense(ammountDollars, reason, currentUser!!.token)"
      class="flex flex-wrap items-center gap-2.5 max-w-2xl">
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
  </div>


  <BaseNavigation v-model:active-tab="activeTab" :tabs="currentTabs" />

  <div class="h-px w-full bg-gradient-to-r from-transparent via-nuxtGreen/50 to-transparent"></div>

  <BasePlainTable v-if="activeTab === 'activePersonal'" :data="activePersonalExpenses" />

  <BasePlainTable v-if="activeTab === 'pastPersonal'" :data="pastPersonalExpenses" />

  <BaseExtenedTable @action="onAction" v-if="activeTab === 'toDecide'" :data="expensesToDecide" />

  <BaseExtenedTable v-if="activeTab === 'pastToDecide'" :data="expensesDecided" :extra="true" />

  <BaseExtenedTable @action="onAllExpenses" v-if="activeTab === 'allExpenses'" :data="allExpenses" :extra="true" />

</template>