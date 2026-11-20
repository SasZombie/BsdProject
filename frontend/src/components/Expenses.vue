<script setup lang="ts">
import { ref } from 'vue';
import { useExpenses } from '../composables/useExpenses';
import type { UserProps } from '../types';

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
    expensesToDecide
} = useExpenses();


getPersonalExpenses(props.currentUser.token);
getExpensesToDecide(props.currentUser.token);
</script>

<template>
    <form @submit.prevent="postExpense(ammountDollars, reason, currentUser.token)">
        <input v-model="reason" type="text" placeholder="reason" />
        <input v-model="ammountDollars" type="text" placeholder="amount" />
        <button :disabled="loading" type="submit">Submit Request</button>
        <p v-if="error">{{ error }}</p>
    </form>

    <div v-if="personalExpenses && personalExpenses.length > 0">
        <div v-for="value in personalExpenses" :key="value.id">
            <p>{{ value.reason }} {{ value.ammountOfDollars }}</p>
        </div>
    </div>

    <div v-if="extraOptions">

        <p>Expenses to Decide</p>
        
        <div v-if="expensesToDecide && expensesToDecide.length > 0">
            <div v-for="expense in expensesToDecide" :key="expense.id">
                <p>{{ expense.reason }} ${{ expense.ammountOfDollars }}</p>
                
                <button type="button" @click="decideExpense(currentUser.token, expense, 'APPROVED')">
                    Yes
                </button>
                
                <button type="button" @click="decideExpense(currentUser.token, expense, 'REJECTED')">
                    No
                </button>
            </div>
        </div>
    </div>
</template>