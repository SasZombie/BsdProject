<script setup lang="ts">
import { onMounted, ref } from 'vue';
import type { BoxProps, Status, Tab, UserProps } from '../types';
import BaseInput from './BaseInput.vue';
import BaseButton from './BaseButton.vue';
import BaseNavigation from './BaseNavigation.vue';
import BasePlainTable from './BasePlainTable.vue';
import BaseExtenedTable from './BaseExtenedTable.vue';
import BaseConfirmBox from './BaseConfirmBox.vue';
import { currentUser } from '../composables/useAuth.ts';
import { usePayments } from '../composables/usePayments.ts';
import { useRoles } from '../composables/useRoles.ts';

const props = defineProps<UserProps>()

const {
  getPersonalPayments,
  getPaymentsToDecide,
  activePersonalPayments,
  postPayment,
  handleFileChange, isUploading, selectedFile,
  paymentsToDecide,
  decidePayment,
  pastPersonalPayments,
  paymentsDecided,
  vetoPayment,
  error,
  allPayments,
  downloadPayment,
  getAllPayments

} = usePayments()


const renderLogic = useRoles(currentUser.value!.role)


const currentTabs: Tab[] = [
  {
    id: 'activePersonal',
    label: 'Active Personal Payments',
    show: true,
    refresh: () => getPersonalPayments(props.currentUser.token)
  },
  {
    id: 'pastPersonal',
    label: 'Past Personal Payments',
    show: true
  },
  {
    id: 'toDecide',
    label: 'Active Payments to Decide',
    show: renderLogic.renderPaymentsExtra,
    refresh: () => getPaymentsToDecide(props.currentUser.token)
  },
  {
    id: 'pastToDecide',
    label: 'Past Payments Decided',
    show: renderLogic.renderPaymentsExtra
  },
  {
    id: 'allPayments',
    label: 'All payments done',
    show: renderLogic.renderAll,
    refresh: () => getAllPayments(props.currentUser.token)
  }
].filter((elem) => elem.show === true)

const refresh = async () => {

  const refreshPromises = currentTabs
    .map((tab) => tab.refresh?.())
    .filter(Boolean)

  await Promise.all(refreshPromises)
}

const activeTab = ref('activePersonal')

const onAction = async (expense: number, decision: Status) => {

  const saved = [...paymentsToDecide.value]
  paymentsToDecide.value = paymentsToDecide.value.filter((num) => num.id !== expense)

  await decidePayment(currentUser.value!!.token, expense, decision, pendingAction.value?.comment)

  if (error.value !== null) {
    paymentsToDecide.value = saved
  }
}

const onAllExpenses = async (expense: number) => {

  await vetoPayment(currentUser.value!!.token, expense)
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

const pendingAction = ref<BoxProps | null>(null)

const cancelAction = () => {
  pendingAction.value = null
}

const onDecision = (index: number, decision: Status, type: number) => {

  let expId = 0

  if (type === 1) {
    expId = paymentsToDecide.value[index].id

  } else if (type === 2) {
    expId = allPayments.value[index].id
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

const handleDownload = async (item: { id: number }) => {

  downloadPayment(currentUser.value!!.token, item.id)
}

</script>

<template>

  <div class="flex items-center justify-between">
    <BaseButton @click="handleShowButton" :text="buttonText" />

    <BaseButton @click="refresh" text="Refresh" />

  </div>
  <div v-show="showButton">
    <form @submit.prevent="postPayment(currentUser!!.token)">
      <div class="px-1 py-2">
        <h3 class="text-base font-semibold text-white tracking-wide mb-3">
          New Payment
        </h3>

        <div class="flex items-center gap-3">
          <div class="w-64">
            <BaseInput type="file" @change="handleFileChange" />
          </div>

          <BaseButton text="Upload Receipt" type="submit" :disabled="isUploading || !selectedFile" />
        </div>

        <p v-if="error" class="w-full text-xs text-red-400 mt-2">{{ error }}</p>
      </div>
    </form>
  </div>

  <BaseNavigation v-model:active-tab="activeTab" :tabs="currentTabs" />

  <div class="h-px w-full bg-gradient-to-r from-transparent via-nuxtGreen/50 to-transparent"></div>

  <BasePlainTable @download="handleDownload" :download="true" v-if="activeTab === 'activePersonal'"
    :data="activePersonalPayments" />

  <BasePlainTable @download="handleDownload" :download="true" v-if="activeTab === 'pastPersonal'"
    :data="pastPersonalPayments" />

  <BaseExtenedTable @download="handleDownload" :download="true" @action="onDecision" v-if="activeTab === 'toDecide'"
    :data="paymentsToDecide" />

  <BaseExtenedTable @download="handleDownload" :download="true" v-if="activeTab === 'pastToDecide'"
    :data="paymentsDecided" :extra="true" />

  <BaseExtenedTable @download="handleDownload" :download="true" @action="onDecision" v-if="activeTab === 'allPayments'"
    :data="allPayments" :extra="true" />

  <BaseConfirmBox @submit="handleSentAction" @cancel="cancelAction" :pendingAction="pendingAction" />

</template>