import { ref } from "vue";
import { type User, type Role, type BackEndUser } from "../types";

export const useAdmin = () => {
  const errorCreation = ref<string | null>(null);
  const errorUpdate = ref<string | null>(null);

  const name = ref("");
  const surname = ref("");
  const username = ref("");
  const password = ref("");
  const reportsTo = ref("None");
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

  const allUsers = ref<BackEndUser[]>([]);

  const getAllUsers = async (currentUser: User): Promise<void> => {
    try {
      const res = await fetch("/api/v1/users/all", {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${currentUser.token}`,
        },
      });

      const body = await res.json();
      allUsers.value = body;

      if (!res.ok) throw new Error("Cannot fetch users");
    } catch (e: any) {
      errorCreation.value = e.message;
    }
  };

  const updateUser = async (currentUser: User, updatedUser: BackEndUser) => {
    console.log(updatedUser.role);
    try {
      const res = await fetch(`/api/v1/users/update/${updatedUser.id}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${currentUser.token}`,
        },
        body: JSON.stringify({
          role: updatedUser.role,
          reportsTo: updatedUser.reportsTo,
        }),
      });

      if (!res.ok) throw new Error("Cannot fetch users");
      const savedUser = await res.json();

      const index = allUsers.value.findIndex((u) => u.id === updatedUser.id);
      if (index !== -1) {
        allUsers.value[index] = { ...allUsers.value[index], ...savedUser };
      }
    } catch (e: any) {
      errorCreation.value = e.message;
    }
  };

  const addUser = async (currentUser: User): Promise<void> => {
    if (currentUser === null) {
      errorCreation.value = "Trying to create with a null user";

      return;
    }
    errorCreation.value = null;

    console.log(reportsTo.value);
    try {
      const res = await fetch("/api/v1/users/create", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${currentUser.token}`,
        },

        
        body: JSON.stringify({
          token: currentUser.token,
          name: name.value,
          surname: surname.value,
          username: username.value,
          password: password.value,
          reportsTo: reportsTo.value,
          role: selectedRole.value,
        }),
      });

      const body = await res.json();
      console.log(body);

      if (!res.ok) throw new Error("Erorrr");

      name.value = ""
      username.value = ""
      surname.value = ""
      password.value = ""
      reportsTo.value = ""
      selectedRole.value = "EMPLOYEE"

      allUsers.value.push(body)

    } catch (e: any) {
      errorCreation.value = e.message;
    }
  };

  return {
    errorCreation,
    errorUpdate,
    name,
    surname,
    username,
    password,
    reportsTo,
    selectedRole,
    roles,
    allUsers,
    addUser,
    getAllUsers,
    updateUser,
  };
};
