import { ref } from "vue";
import type { Expense, Status } from "../types";

export const useRequests = () => {
  const error = ref<string | null>(null);
  const loading = ref<boolean>(false);
  const personalExpenses = ref<Expense[]>([]);
  const expensesToDecide = ref<Expense[]>([]);

  const postExpense = async (
    ammountDollars: number,
    reason: string,
    token: string,
  ): Promise<void> => {
    loading.value = true;
    error.value = null;

    console.log(token);
    try {
      const res = await fetch("/api/v1/requests/expense/create", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          userId: token,
          ammountDollars: ammountDollars,
          reason: reason,
        }),
      });

      const body = await res.json();
      console.log(body);

      if (!res.ok) throw new Error("Invalid credentials");
    } catch (e: any) {
      error.value = e.message;
    } finally {
      loading.value = false;
    }
  };

  const getPersonalExpenses = async (token: string): Promise<void> => {
    loading.value = true;
    error.value = null;

    console.log(token);
    try {
      const res = await fetch("/api/v1/requests/expense/personal", {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });

      const body = await res.json();
      console.log(body);

      personalExpenses.value = body;

      if (!res.ok) throw new Error("Invalid credentials");
    } catch (e: any) {
      error.value = e.message;
    } finally {
      loading.value = false;
    }
  };

  const getExpensesToDecide = async (token: string): Promise<void> => {
    loading.value = true;
    error.value = null;

    console.log(token);
    try {
      const res = await fetch("/api/v1/requests/expense/toDecide", {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });

      const body = await res.json();
      console.log(body);
      expensesToDecide.value = body;

      if (!res.ok) throw new Error("Invalid credentials");
    } catch (e: any) {
      error.value = e.message;
    } finally {
      loading.value = false;
    }
  };

  const decideExpense = async (
    token: string,
    exp: Expense,
    decision: Status,
  ): Promise<void> => {
    console.log(token);
    try {
      const res = await fetch("/api/v1/requests/expense/decision", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          expenseId: exp.id,
          decision: decision,
          comment: "Bla Bla Bla",
        }),
      });

      const body = await res.json();
      console.log(body);
      // expensesToDecide.value = body;

      if (!res.ok) throw new Error("Invalid credentials");
    } catch (e: any) {
      error.value = e.message;
    } finally {
      loading.value = false;
    }
  };

  return {
    postExpense,
    error,
    decideExpense,
    loading,
    getPersonalExpenses,
    getExpensesToDecide,
    personalExpenses,
    expensesToDecide,
  };
};
