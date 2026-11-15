<script setup lang="ts">

import { ref } from 'vue'
import { useRequests } from '../composables/useRequests'

const reason = ref('I want money cuz I can')
const ammountDollars = ref(67)

const { postExpense, error, loading } = useRequests()

</script>

<template>


  <form @submit.prevent="postExpense(ammountDollars, reason, '1')">
    <input v-model="reason" type="text" placeholder="reason"  />
    <input v-model="ammountDollars" type="text" placeholder="ammount" />
    <button :disabled="loading" type="submit">Submit Request</button>
    <p v-if="error">{{ error }}</p>
  </form>


</template>