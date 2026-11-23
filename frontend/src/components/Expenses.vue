<script setup lang="ts">
import { onMounted, ref } from 'vue';
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
  allExpenses
} = useExpenses();


const refresh = async () => {

  await Promise.all([
    getPersonalExpenses(props.currentUser.token),
    getExpensesDecisions(props.currentUser.token),
    getAllExpenses(props.currentUser.token)
  ]);
}

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

const onAction = async (expense: number, decision: Status) => {

  console.log(expense)
  const saved = [...expensesToDecide.value]
  expensesToDecide.value = expensesToDecide.value.filter((num) => num.id !== expense)

  await decideExpense(currentUser.value!!.token, expense, decision)

  if (error.value !== null) {
    expensesToDecide.value = saved
  }
}

const onAllExpenses = async (expense: number) => {

  await vetoExpense(currentUser.value!!.token, expense)
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

interface PendingAction {
  expId: number;
  decision: Status;
  type: number
}

const pendingAction = ref<PendingAction | null>(null)

const cancelAction = () => {
  pendingAction.value = null
}

const onDecision = (index: number, decision: Status, type: number) => {

  let expId = 0

  if (type === 1) {
    console.log("Sunt 1")
    expId = expensesToDecide.value[index].id

  } else if (type === 2) {
    expId = allExpenses.value[index].id
  }

  pendingAction.value = { expId, decision, type }
}

const handleSentAction = () => {
  if (pendingAction.value?.type === 1) {
    onAction(pendingAction.value.expId, pendingAction.value.decision)
  } else {
    onAllExpenses(pendingAction.value!!.expId)
  }

  pendingAction.value = null
}

onMounted(async () => {
  await refresh()
})
</script>

<template>

  <div class="flex items-center justify-between">
    <BaseButton @click="handleShowButton" :text="buttonText" />

    <BaseButton @click="refresh" text="Refresh" />

  </div>
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

  <BaseExtenedTable @action="onDecision" v-if="activeTab === 'toDecide'" :data="expensesToDecide" />

  <BaseExtenedTable v-if="activeTab === 'pastToDecide'" :data="expensesDecided" :extra="true" />

  <BaseExtenedTable @action="onDecision" v-if="activeTab === 'allExpenses'" :data="allExpenses" :extra="true" />


  <Teleport to="body">
    <Transition enter-active-class="transition duration-150 ease-out" enter-from-class="opacity-0"
      enter-to-class="opacity-100" leave-active-class="transition duration-100 ease-in" leave-from-class="opacity-100"
      leave-to-class="opacity-0">
      <div v-if="pendingAction"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4"
        @click.self="cancelAction">
        <div class="w-full max-w-sm rounded-xl border border-slate-800 bg-nuxtBlack p-6 shadow-2xl">
          <h3 class="text-base font-semibold text-white">
            Confirm {{ pendingAction.decision }}
          </h3>

          <p class="mt-2 text-sm text-slate-400">
            Are you sure you want to mark the expense as {{ pendingAction.decision }}
          </p>

          <div class="mt-6 flex justify-end gap-3">
            <button type="button" @click="cancelAction"
              class="px-3.5 py-2 text-sm rounded-lg text-slate-400 hover:text-white transition cursor-pointer">
              Cancel
            </button>

            <BaseButton @click="handleSentAction" :text="`Yes`" />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>