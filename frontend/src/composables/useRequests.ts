import { ref } from "vue";
import { useRouter } from "vue-router";

export function useRequests() {
  const router = useRouter();
  const error = ref<string | null>(null);
  const loading = ref<boolean>(false);

  const postExpense = async (ammountDollars: number, reason: string, token: string): Promise<void> => {
    
    loading.value = true;
    error.value = null;

    try {

        const res = await fetch("/api/v1/requests/expense/create", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userId: token, ammountDollars: ammountDollars, reason: reason}),
      });

      console.log(await res.json())

      if (!res.ok) throw new Error("Invalid credentials");

    //   router.push('/dashboard')


    } catch (e: any) {
      error.value = e.message;
    } finally {
      loading.value = false;
    }
  };

  return { postExpense, error, loading };
}
