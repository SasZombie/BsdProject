<script setup lang="ts">

import { onMounted, ref } from 'vue'
import { useRequests } from '../composables/useRequests'
import { useAuth } from '../composables/useAuth'
import { useAdmin } from '../composables/useAdmin'
import router from '../router'

import UserRowForm from '../components/UserRowForm.vue'

const reason = ref('I want money cuz I can')
const ammountDollars = ref(67)

const { postExpense, error, loading } = useRequests()
const { currentUser } = useAuth()

console.log(currentUser.value)

const { name, errorCreation, surname, username, password, selectedRole, reportsTo, roles, addUser, allUsers, getAllUsers, errorUpdate, updateUser } = useAdmin();

onMounted(() => {
    onMounted(() => {
        if (!currentUser.value) {
            router.replace('/')
        }
    });

    getAllUsers(currentUser.value!)
})


</script>

<template>


    <form @submit.prevent="postExpense(ammountDollars, reason, '1')">
        <input v-model="reason" type="text" placeholder="reason" />
        <input v-model="ammountDollars" type="text" placeholder="ammount" />
        <button :disabled="loading" type="submit">Submit Request</button>
        <p v-if="error">{{ error }}</p>
    </form>

    <!-- Admin Options -->
    <div>

        <form @submit.prevent="addUser(currentUser!)">
            <input v-model="name" type="text" placeholder="Name" />
            <input v-model="surname" type="text" placeholder="Surname" />
            <input v-model="username" type="text" placeholder="Username" />
            <input v-model="reportsTo" type="text" placeholder="ReportsTo" />
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
            <UserRowForm v-for="user in allUsers" :key="user.id" :user="user" :roles="roles" @update="updateUser(currentUser!!, $event)" />
            <p v-if="errorUpdate">{{ errorUpdate }}</p>
        </div>
        <p v-else-if="loading">Loading...</p>
    </div>

</template>