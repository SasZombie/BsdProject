import { ref } from "vue";
import { useRouter } from "vue-router";

export function useAuth() {
  const router = useRouter();
  const error = ref<string | null>(null);
  const loading = ref<boolean>(false);

//AdminUsername 1
  const login = async (username: string, pass: string): Promise<void> => {
   
    if(username === null && pass === null)
    {
        username = "AdminUsername";
        pass = "1";
    }
    loading.value = true;
    error.value = null;

    try {

        const res = await fetch("/api/v1/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password: pass }),
      });

      console.log(await res.json())

      if (!res.ok) throw new Error("Invalid credentials");

      router.push('/dashboard')


    } catch (e: any) {
      error.value = e.message;
    } finally {
      loading.value = false;
    }
  };

  return { login, error, loading };
}
