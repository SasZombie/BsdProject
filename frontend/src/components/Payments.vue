<script setup lang="ts">
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

</template>