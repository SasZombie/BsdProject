import { ref } from "vue";
import type {
  ActiveExpense,
  Expense,
  ExpenseForm,
  ExpensesDecided,
  ExtenedExpense,
  PastExpsnse,
  Status,
} from "../types";
import { currentUser } from "./useAuth";

export const useExpenses = () => {
  const error = ref<string | null>(null);
  const loading = ref<boolean>(false);
  const activePersonalExpenses = ref<ActiveExpense[]>([]);
  const pastPersonalExpenses = ref<PastExpsnse[]>([]);
  const extenedExpenses = ref<ExtenedExpense[]>([]);

  const expensesToDecide = ref<ExpenseForm[]>([]);
  const expensesDecided = ref<ExpensesDecided[]>([]);

  const allExpenses = ref<Expense[]>([]);

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
      if (!res.ok) throw new Error("Invalid credentials");

      const body: Expense = await res.json();
      body.postedBy = currentUser.value!!.name

      activePersonalExpenses.value.push(body);
      allExpenses.value.push(body);
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

      if (!res.ok) throw new Error("Invalid credentials");
      const body: Expense[] = await res.json();
      console.log(body);

      for (const e of body) {
        if (e.status === "PENDING") {
          activePersonalExpenses.value.push({
            id: e.id,
            reason: e.reason,
            createdAt: e.createdAt,
            updatedAt: e.updatedAt,
            ammountOfDollars: e.ammountOfDollars,
            needsApprovalBy: e.needsApprovalBy,
            approvedBy: e.approvedBy,
          });
        } else {
          pastPersonalExpenses.value.push({
            id: e.id,
            reason: e.reason,
            createdAt: e.createdAt,
            updatedAt: e.updatedAt,
            ammountOfDollars: e.ammountOfDollars,
            status: e.status,
            approvedBy: e.approvedBy,
            rejectedBy: e.rejectedBy,
          });
        }
      }
    } catch (e: any) {
      error.value = e.message;
    } finally {
      loading.value = false;
    }
  };

  const getExpensesDecisions = async (token: string): Promise<void> => {
    loading.value = true;
    error.value = null;

    console.log(token);
    try {
      const res = await fetch("/api/v1/requests/expense/decisions", {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });

      if (!res.ok) throw new Error("Invalid credentials");

      const body: ExpensesDecided[] = await res.json();
      console.log("Am rpimti", body);

      for (const item of body) {
        if (item.status === "PENDING") {
          expensesToDecide.value.push({
            id: item.id,
            createdAt: item.createdAt,
            reason: item.reason,
            status: item.status,
            ammountOfDollars: item.ammountOfDollars,
            userName: item.userName,
          });
        } else {
          expensesDecided.value.push({
            id: item.id,
            createdAt: item.createdAt,
            reason: item.reason,
            status: item.status,
            personalStatus: item.personalStatus,
            ammountOfDollars: item.ammountOfDollars,
            userName: item.userName,
          });
        }
      }
    } catch (e: any) {
      error.value = e.message;
    } finally {
      loading.value = false;
    }
  };

  const getAllExpenses = async (token: string): Promise<void> => {
    loading.value = true;
    error.value = null;

    console.log(token);
    try {
      const res = await fetch("/api/v1/requests/expense/all", {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });

      if (!res.ok) throw new Error("Invalid credentials");
      extenedExpenses.value = await res.json();
      console.log(extenedExpenses.value);

      for (const item of extenedExpenses.value) {
        allExpenses.value.push({
          id: item.expense.id,
          createdAt: item.expense.createdAt,
          updatedAt: item.expense.updatedAt,
          reason: item.expense.reason,
          status: item.expense.status,
          ammountOfDollars: item.expense.ammountOfDollars,
          postedBy: `${item.user.name} ${item.user.surname}`,

          needsApprovalBy: item.expense.needsApprovalBy,
          approvedBy: item.expense.approvedBy,
          rejectedBy: item.expense.rejectedBy,
        });
      }
    } catch (e: any) {
      error.value = e.message;
    } finally {
      loading.value = false;
    }
  };

  const decideExpense = async (
    token: string,
    exp: number,
    decision: Status,
  ): Promise<void> => {
    try {
      const res = await fetch("/api/v1/requests/expense/decision", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          expenseId: exp,
          decision: decision,
          comment: "Bla Bla Bla",
        }),
      });

      if (!res.ok) throw new Error("Invalid credentials");

      const body: ExpensesDecided = await res.json();
      expensesDecided.value.push(body);
    } catch (e: any) {
      error.value = e.message;
    } finally {
      loading.value = false;
    }
  };

  const vetoExpense = async (token: string, exp: number): Promise<void> => {
    try {
      const res = await fetch("/api/v1/requests/expense/veto", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          expenseId: exp,
          comment: "Bla Bla Bla",
        }),
      });

      if (!res.ok) throw new Error("Invalid credentials");

      console.log("AM trecut")
      const target = allExpenses.value.find((elem) => elem.id === exp);
      target && (target.status = "VETOED");

    } catch (e: any) {
      error.value = e.message;
    } finally {
      loading.value = false;
    }
  };

  return {
    postExpense,
    vetoExpense,
    error,
    decideExpense,
    loading,
    getPersonalExpenses,
    getExpensesDecisions,
    activePersonalExpenses,
    pastPersonalExpenses,
    expensesToDecide,
    expensesDecided,
    allExpenses,
    getAllExpenses,
    extenedExpenses,
  };
};
