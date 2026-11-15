import { ref } from "vue";
import type { Role } from "../types";

export const useAdmin = () => {
  const errorCreation = ref<string | null>(null);
  const name = ref("");
  const surname = ref("");
  const username = ref("");
  const password = ref("");
  const selectedRole = ref<Role>("EMPLOYEE");

  const roles: Role[] = [
    "EMPLOYEE",
    "ADMIN",
    "MANAGER",
    "CEO",
    "CFO",
    "BOARD_DIRECTOR",
    "ACCOUNTANT",
  ];

  const addUser = async (): Promise<void> => {
    errorCreation.value = null;

    console.log("start");
    try {
      const res = await fetch("/api/v1/users/create", {
        method: "POST",
        headers: { "Content-Type": "application/json" },

        body: JSON.stringify({
          name: name.value,
          surrname: surname.value,
          username: username.value,
          password: password.value,
          role: selectedRole.value,
        }),
      });

      const body = await res.json();
      console.log(body);

      if (!res.ok) throw new Error("Erorrr");
    } catch (e: any) {
      errorCreation.value = e.message;
    }
  };

  return {
    name,
    errorCreation,
    surname,
    username,
    password,
    selectedRole,
    roles,
    addUser,
  };
};
