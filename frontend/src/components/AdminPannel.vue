<script setup lang="ts">
import { useAdmin } from '../composables/useAdmin';
import type { Tab, UserProps } from '../types';
import UserRowForm from '../components/UserRowForm.vue'
import BaseInput from './BaseInput.vue';
import BaseNavigation from './BaseNavigation.vue';
import BaseButton from './BaseButton.vue';
import { computed, nextTick, onMounted, ref } from 'vue';
import { currentUser } from '../composables/useAuth.ts';



const props = defineProps<UserProps>()

const { name, errorCreation, surname, username, password, selectedRole, reportsTo, roles, addUser, getNextLog, allUsers, getAllUsers, errorUpdate, updateUser, logs, haltOperations, resumeOperations }
    = useAdmin();


const currentTabs: Tab[] = [
    {
        id: 'users',
        label: 'Users',
        show: true
    },
    {
        id: 'logs',
        label: 'Logs',
        show: true
    }
].filter((elem) => elem.show === true)

const activeTab = ref('users')

onMounted(async () => {
    await refresh()
})

const refresh = async () => {
    getAllUsers(props.currentUser)
    getNextLog(props.currentUser)
}

const logViewerRef = ref<HTMLElement | null>(null);

const scrollToBottom = () => {
    nextTick(() => {
        if (logViewerRef.value) {
            logViewerRef.value.scrollTop = logViewerRef.value.scrollHeight;
        }
    });
};

const copyLogs = async () => {
    await navigator.clipboard.writeText(rawLogContent.value);
};
const rawLogContent = computed(() => {
    return Array.isArray(logs.value) ? logs.value.join('\n') : logs.value;
});

const operationsFunctional = ref(true)

const operationsText = ref('Halt Operations')

const handleButtonChange = () => {
    if (operationsFunctional.value) {
        haltOperations(currentUser.value!!)
        operationsText.value = 'Resume Operations'
    }
    else {
        resumeOperations(currentUser.value!!)
        operationsText.value = 'Halt Operations'
    }
    operationsFunctional.value = !operationsFunctional.value
}

</script>

<template>
    <div class="flex items-center justify-between">
        <BaseNavigation v-model:active-tab="activeTab" :tabs="currentTabs" />

        <div class="flex items-center gap-2">

            <BaseButton @click="handleButtonChange" :text="operationsText" />

            <BaseButton @click="refresh" text="Refresh" />
        </div>
    </div>
    <div v-if="activeTab === 'users'">

        <div class="w-full">
            <form @submit.prevent="addUser(currentUser!)" class="flex flex-wrap items-center gap-2.5">
                <div class="flex-1 min-w-[130px]">
                    <BaseInput v-model="name" type="text" placeholder="First Name" class="w-full" />
                </div>

                <div class="flex-1 min-w-[130px]">
                    <BaseInput v-model="surname" type="text" placeholder="Last Name" class="w-full" />
                </div>

                <div class="flex-1 min-w-[140px]">
                    <BaseInput v-model="username" type="text" placeholder="Username" autocomplete="off"
                        class="w-full" />
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

        <div class="py-3 w-full flex flex-col gap-2.5" v-if="allUsers && allUsers.length > 0">
            <UserRowForm v-for="user in allUsers" :key="user.id" :user="user" :roles="roles" :all-users="allUsers"
                @update="updateUser(currentUser!, $event)" />
            <p v-if="errorUpdate">{{ errorUpdate }}</p>
        </div>
    </div>

    <div v-if="activeTab === 'logs'"
        class="w-full rounded-xl border border-nuxtGreen/50 bg-nuxtBlack shadow-2xl overflow-hidden">
        <div
            class="flex items-center justify-between border-b border-nuxtGreen/50 bg-nuxtBlack px-4 py-2.5 backdrop-blur-sm">
            <div class="flex items-center gap-2">
                <span class="ml-2 font-mono text-xs font-medium text-zinc-400">system-output.log</span>
            </div>

            <div class="flex items-center gap-2">
                <BaseButton @click="copyLogs" type="button" text="Copy All" />
                <BaseButton @click="scrollToBottom" type="button" text="Scroll to Bottom" />
            </div>
        </div>

        <div ref="logViewerRef"
            class="max-h-[550px] overflow-y-auto p-4 font-mono text-xs leading-relaxed selection:bg-emerald-900/60 selection:text-emerald-200">
            <pre
                class="font-mono text-zinc-300 whitespace-pre-wrap break-all select-text"><code>{{ rawLogContent }}</code></pre>
        </div>
    </div>
</template>