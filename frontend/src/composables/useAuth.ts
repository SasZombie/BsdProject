import { ref } from "vue";
import { useRouter } from "vue-router";
import type { User } from "../types";

const currentUser = ref<User | null>(null);

export const useAuth = () => {
  const router = useRouter();
  const error = ref<string | null>(null);
  const loading = ref<boolean>(false);

  //AdminUsername 1
  const login = async (username: string, pass: string): Promise<void> => {
    loading.value = true;
    error.value = null;

    try {
      const res = await fetch("/api/v1/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password: pass }),
      });

      const body = await res.json();
      console.log(body);

      if (!res.ok) throw new Error("Invalid credentials");

      currentUser.value = body;

      router.push("/dashboard");
    } catch (e: any) {
      error.value = e.message;
    } finally {
      loading.value = false;
    }
  };

  return { login, currentUser, error, loading };
};
