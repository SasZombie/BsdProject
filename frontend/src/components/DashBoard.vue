<script setup lang="ts">

import { onMounted, ref } from 'vue'
import { useExpenses } from '../composables/useExpenses.ts'
import { usePayments } from '../composables/usePayments.ts'
import { useAuth } from '../composables/useAuth'
import { useAdmin } from '../composables/useAdmin'
import router from '../router'

import UserRowForm from '../components/UserRowForm.vue'


const reason = ref('I want money cuz I can')
const ammountDollars = ref(67)

const { postExpense, decideExpense, error, loading, getPersonalExpenses, getExpensesToDecide, personalExpenses, expensesToDecide } = useExpenses()
const { postPayment, handleFileChange, isUploading, selectedFile, decidePayment, getPersonalPayments, getPaymentsToDecide, personalPayments, downloadPayment, paymentsToDecide } = usePayments()
const { currentUser } = useAuth()

console.log(currentUser.value)

const { name, errorCreation, surname, username, password, selectedRole, reportsTo, roles, addUser, allUsers, getAllUsers, errorUpdate, updateUser } = useAdmin();

onMounted(() => {

    if (!currentUser.value) {
        router.replace('/')
    }


    getAllUsers(currentUser.value!)
    getPersonalExpenses(currentUser.value!.token)
    getExpensesToDecide(currentUser.value!.token)
    getPaymentsToDecide(currentUser.value!.token)


})


</script>

<template>

    <!-- Expenses -->

    <form @submit.prevent="postExpense(ammountDollars, reason, currentUser!.token)">
        <input v-model="reason" type="text" placeholder="reason" />
        <input v-model="ammountDollars" type="text" placeholder="ammount" />
        <button :disabled="loading" type="submit">Submit Request</button>
        <p v-if="error">{{ error }}</p>
    </form>

    <div v-if="personalExpenses && personalExpenses.length > 0">
        <div v-for="value in personalExpenses">
            <p>{{ value.reason }} {{ value.ammountOfDollars }} </p>
        </div>
    </div>

    <p>Expenses to Decide </p>
    <div v-if="expensesToDecide && expensesToDecide.length > 0">
        <div v-for="expense in expensesToDecide" :key="expense.id">
            <p>{{ expense.reason }} ${{ expense.ammountOfDollars }}</p>

            <button type="button" @click="decideExpense(currentUser!.token, expense, 'APPROVED')">
                Yes
            </button>

            <button type="button" @click="decideExpense(currentUser!.token, expense, 'REJECTED')">
                No
            </button>
        </div>
    </div>

    <!-- Payments -->


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
    


    <!-- Admin Options -->
    <div>

        <form @submit.prevent="addUser(currentUser!)">
            <input v-model="name" type="text" placeholder="Name" />
            <input v-model="surname" type="text" placeholder="Surname" />
            <input v-model="username" type="text" placeholder="Username" />
            <select v-model="reportsTo">
                <option :value="null">None</option>
                <option v-for="manager in allUsers" :key="manager.id" :value="manager.username">
                    {{ manager.name }} {{ manager.surname }} ({{ manager.username }})

                </option>
            </select>

            <input v-model="password" type="password" placeholder="Password" />

            <select v-model="selectedRole">
                <option v-for="role in roles" :key="role" :value="role">
                    {{ role }}
                </option>
            </select>

            <button :disabled="loading" type="submit">Create New User</button>
            <p v-if="errorCreation">{{ errorCreation }}</p>
        </form>

        <div v-if="allUsers && allUsers.length > 0">
            <UserRowForm v-for="user in allUsers" :key="user.id" :user="user" :roles="roles" :all-users="allUsers"
                @update="updateUser(currentUser!, $event)" />
            <p v-if="errorUpdate">{{ errorUpdate }}</p>
        </div>
        <p v-else-if="loading">Loading...</p>
    </div>

</template>