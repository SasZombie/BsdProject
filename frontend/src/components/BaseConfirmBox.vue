<script setup lang="ts">

import type { BoxProps } from '../types';
import BaseButton from './BaseButton.vue';
import BaseInput from './BaseInput.vue';


defineProps<{
    pendingAction: BoxProps | null
}>()

const emit = defineEmits<{
    (e: 'cancel'): void
    (e: 'submit'): void
}>()

</script>

<template>
    <Teleport to="body">
        <Transition enter-active-class="transition duration-150 ease-out" enter-from-class="opacity-0"
            enter-to-class="opacity-100" leave-active-class="transition duration-100 ease-in"
            leave-from-class="opacity-100" leave-to-class="opacity-0">
            <div v-if="pendingAction"
                class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4"
                @click.self="emit('cancel')">
                <div class="w-full max-w-sm rounded-xl border border-nuxtGreen bg-nuxtBlack p-6 shadow-2xl">
                    <h3 class="text-base font-semibold text-white">
                        Confirm {{ pendingAction.decision }}
                    </h3>

                    <p class="mt-2 text-sm text-slate-400">
                        Are you sure you want to mark it as {{ pendingAction.decision }}
                    </p>

                    <BaseInput v-model="pendingAction.comment" placeholder="Comment(optional)" type="text" />

                    <div class="mt-6 flex justify-end gap-3">
                        <BaseButton type="button" @click="emit('cancel')" text="Cancel"/>

                        <BaseButton @click="emit('submit')" :text="`Yes`" />
                    </div>
                </div>
            </div>
        </Transition>
    </Teleport>
</template>