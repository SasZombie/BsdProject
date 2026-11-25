import { ref } from "vue";
import { useRouter } from "vue-router";
import type { User } from "../types";

export const currentUser = ref<User | null>(null);

export const useAuth = () => {
  const router = useRouter();
  const error = ref<string | null>(null);
  const loading = ref<boolean>(false);

  //AdminUsername 1
  const login = async (username: string, pass: string): Promise<void> => {
    try {
      const res = await fetch("/api/v1/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password: pass }),
      });

      if (!res.ok) throw new Error("Invalid credentials");

      const body = await res.json();
      currentUser.value = body;

      router.push("/dashboard");
    } catch (e: any) {
      error.value = e.message;
    } finally {
      loading.value = false;
    }
  };

  const refreshToken = async (token: string): Promise<void> => {
    try {
      const res = await fetch("/api/v1/auth/refreshToken", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });

      if (!res.ok) throw new Error("Invalid credentials");

      const body = await res.text()
      currentUser.value!!.token = body 

      router.push("/dashboard");
    } catch (e: any) {
      error.value = e.message;
    } finally {
      loading.value = false;
    }
  };

  const logOut = async () => {
    //TODO: Api call to backend

    router.push("/");
  };

  return { login, logOut, refreshToken, error, loading };
};
