import { ref } from "vue";

export function useRequests() {
  const error = ref<string | null>(null);
  const loading = ref<boolean>(false);

  const postExpense = async (ammountDollars: number, reason: string, token: string): Promise<void> => {
    
    loading.value = true;
    error.value = null;

    console.log(token)
    try {

        const res = await fetch("/api/v1/requests/expense/create", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userId: token, ammountDollars: ammountDollars, reason: reason}),
      });

        const body = await res.json()
        console.log(body)

      if (!res.ok) throw new Error("Invalid credentials");
    } catch (e: any) {
      error.value = e.message;
    } finally {
      loading.value = false;
    }
  };

  const getExpense = async (token: string): Promise<void> => {
    
    loading.value = true;
    error.value = null;

    console.log(token)
    try {

        const res = await fetch("/api/v1/requests/expense/all", {
        method: "GET",
        headers: { "Content-Type": "application/json",  Authorization: `Bearer ${token}` },
      });

        const body = await res.json()
        console.log(body)

      if (!res.ok) throw new Error("Invalid credentials");
    } catch (e: any) {
      error.value = e.message;
    } finally {
      loading.value = false;
    }
  };


  return { postExpense, error, loading, getExpense };
}
