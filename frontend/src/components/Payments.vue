<!-- <script setup lang="ts">
import { usePayments } from '../composables/usePayments';
import type { UserProps } from '../types';


const props = defineProps<UserProps>()

const { postPayment, handleFileChange, isUploading, selectedFile, decidePayment, getPersonalPayments, getPaymentsToDecide, personalPayments, downloadPayment, paymentsToDecide } = usePayments()


getPaymentsToDecide(props.currentUser.token)


</script>

<template>
    <form @submit.prevent="postPayment(currentUser!!.token)">

        <div>
            <label>Select File:</label>
            <input type="file" @change="handleFileChange" />
        </div>

        <button type="submit" :disabled="isUploading || !selectedFile">
            {{ isUploading ? 'Uploading...' : 'Upload Receipt' }}
        </button>
    </form>

    <p>Payments to Decide </p>
    <div v-if="paymentsToDecide && paymentsToDecide.length > 0">
        <div v-for="payment in paymentsToDecide" :key="payment.id">
            <p>{{ payment.reason }} ${{ payment.status }}</p>


            <button type="button" @click="downloadPayment(currentUser!.token, payment)">
                Download
            </button>

            <button type="button" @click="decidePayment(currentUser!.token, payment, 'APPROVED')">
                Yes
            </button>

            <button type="button" @click="decidePayment(currentUser!.token, payment, 'REJECTED')">
                No
            </button>
        </div>
    </div>

</template> -->

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
import { usePayments } from '../composables/usePayments.ts';

const props = defineProps<UserProps>()


const reason = ref('I want money cuz I can');
const ammountDollars = ref(67);


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
  getAllPayments

} = usePayments()


const refresh = async () => {

  await Promise.all([
    getPersonalPayments(props.currentUser.token),
    getPaymentsToDecide(props.currentUser.token),
    getAllPayments(props.currentUser.token)
  ]);
}

const currentTabs: Tab[] = [
  {
    id: 'activePersonal',
    label: 'Active Personal Payments',
    show: true
  },
  {
    id: 'pastPersonal',
    label: 'Past Personal Payments',
    show: true
  },
  {
    id: 'toDecide',
    label: 'Active Payments to Decide',
    show: true
  },
  {
    id: 'pastToDecide',
    label: 'Past Payments Decided',
    show: true
  },
  {
    id: 'allPayments',
    label: 'All payments done',
    show: true
  }
].filter((elem) => elem.show === true)


const activeTab = ref('activePersonal')

const onAction = async (expense: number, decision: Status) => {

  console.log(expense)
  const saved = [...paymentsToDecide.value]
  paymentsToDecide.value = paymentsToDecide.value.filter((num) => num.id !== expense)

  await decidePayment(currentUser.value!!.token, expense, decision)

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

  console.log(index)
  if (type === 1) {
    console.log("Sunt 1")
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

  <BasePlainTable v-if="activeTab === 'activePersonal'" :data="activePersonalPayments" />

  <BasePlainTable v-if="activeTab === 'pastPersonal'" :data="pastPersonalPayments" /> 

  <BaseExtenedTable @action="onDecision" v-if="activeTab === 'toDecide'" :data="paymentsToDecide" />

  <BaseExtenedTable v-if="activeTab === 'pastToDecide'" :data="paymentsDecided" :extra="true" /> 

  <!-- <BaseExtenedTable @action="onDecision" v-if="activeTab === 'allPayments'" :data="allExspenses" :extra="true" /> -->


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