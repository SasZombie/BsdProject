<script setup lang="ts">
import { useAdmin } from '../composables/useAdmin';
import type { UserProps } from '../types';
import UserRowForm from '../components/UserRowForm.vue'
import BaseInput from './BaseInput.vue';
import BaseButton from './BaseButton.vue';

const props = defineProps<UserProps>()

const { name, errorCreation, surname, username, password, selectedRole, reportsTo, roles, addUser, allUsers, getAllUsers, errorUpdate, updateUser } = useAdmin();

getAllUsers(props.currentUser)


</script>

<template>
    <div class="w-full">
        <form @submit.prevent="addUser(currentUser!)" class="flex flex-wrap items-center gap-2.5">
            <div class="flex-1 min-w-[130px]">
                <BaseInput v-model="name" type="text" placeholder="First Name" class="w-full" />
            </div>

            <div class="flex-1 min-w-[130px]">
                <BaseInput v-model="surname" type="text" placeholder="Last Name" class="w-full" />
            </div>

            <div class="flex-1 min-w-[140px]">
                <BaseInput v-model="username" type="text" placeholder="Username" autocomplete="off" class="w-full" />
            </div>

            <div class="flex-1 min-w-[140px]">
                <BaseInput v-model="password" type="password" placeholder="Password" autocomplete="new-password"
                    class="w-full" />
            </div>

            <div class="relative w-40 shrink-0">
                <select v-model="selectedRole"
                    class="w-full appearance-none rounded-lg border border-nuxtGreen/20 bg-nuxtBlack/90 py-2 pl-3 pr-8 text-sm text-white focus:border-nuxtGreen focus:outline-none focus:ring-1 focus:ring-nuxtGreen transition-colors">
                    <option disabled value="">Select Role</option>
                    <option v-for="role in roles" :key="role" :value="role" class="bg-nuxtBlack">
                        {{ role }}
                    </option>
                </select>
                <div class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2.5 text-white">
                    <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                    </svg>
                </div>
            </div>

            <div class="relative w-48 shrink-0">
                <select v-model="reportsTo"
                    class="w-full appearance-none rounded-lg border border-nuxtGreen/20 bg-nuxtBlack/90 py-2 pl-3 pr-8 text-sm text-white focus:border-nuxtGreen focus:outline-none focus:ring-1 focus:ring-nuxtGreen transition-colors">
                    <option :value="null" class="bg-nuxtBlack">Reports To: None</option>
                    <option v-for="manager in allUsers" :key="manager.id" :value="manager.username"
                        class="bg-nuxtBlack">
                        {{ manager.name }} {{ manager.surname }}
                    </option>
                </select>

                <div class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2.5 text-white">
                    <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                    </svg>
                </div>
            </div>

            <div class="shrink-0">
                <BaseButton text="Add User" type="submit" class="whitespace-nowrap px-4 py-2" />
            </div>
        </form>

        <p v-if="errorCreation" class="mt-2 text-xs font-medium text-red-400 pl-1 flex items-center gap-1.5">
            <svg class="h-3.5 w-3.5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span>{{ errorCreation }}</span>
        </p>
    </div>

    <div class="w-full flex flex-col gap-2.5" v-if="allUsers && allUsers.length > 0">
        <UserRowForm v-for="user in allUsers" :key="user.id" :user="user" :roles="roles" :all-users="allUsers"
            @update="updateUser(currentUser!, $event)" />
        <p v-if="errorUpdate">{{ errorUpdate }}</p>
    </div>
</template>