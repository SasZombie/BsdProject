<script setup lang="ts">
import { useAdmin } from '../composables/useAdmin';
import type { UserProps } from '../types';
import UserRowForm from '../components/UserRowForm.vue'


const props = defineProps<UserProps>()

const { name, errorCreation, surname, username, password, selectedRole, reportsTo, roles, addUser, allUsers, getAllUsers, errorUpdate, updateUser } = useAdmin();

getAllUsers(props.currentUser)


</script>

<template>
    <div>
        <form @submit.prevent="addUser(currentUser!)">
            <input v-model="name" type="text" placeholder="Name" />
            <input v-model="surname" type="text" placeholder="Surname" />
            <input v-model="username" type="text" placeholder="Username" autocomplete="off" />
            <select v-model="reportsTo">
                <option :value="null">None</option>
                <option v-for="manager in allUsers" :key="manager.id" :value="manager.username">
                    {{ manager.name }} {{ manager.surname }} ({{ manager.username }})

                </option>
            </select>

            <input v-model="password" type="password" placeholder="Password" autocomplete="new-password" />

            <select v-model="selectedRole">
                <option v-for="role in roles" :key="role" :value="role">
                    {{ role }}
                </option>
            </select>

            <button type="submit">Create New User</button>
            <p v-if="errorCreation">{{ errorCreation }}</p>
        </form>

        <div v-if="allUsers && allUsers.length > 0">
            <UserRowForm v-for="user in allUsers" :key="user.id" :user="user" :roles="roles" :all-users="allUsers"
                @update="updateUser(currentUser!, $event)" />
            <p v-if="errorUpdate">{{ errorUpdate }}</p>
        </div>
        <!-- <p v-else-if="loading">Loading...</p> -->
    </div>

</template>