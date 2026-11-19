import { ref } from "vue";
import type { Payment, Status } from "../types";

export const usePayments = () => {
  const error = ref<string | null>(null);
  const loading = ref<boolean>(false);
  const personalPayments = ref<Payment[]>([]);
  const paymentsToDecide = ref<Payment[]>([]);
  const selectedFile = ref<File | null>(null);
  const isUploading = ref<boolean>(false);

  const handleFileChange = (event: Event) => {
    const target = event.target as HTMLInputElement;
    if (target.files && target.files.length > 0) {
      selectedFile.value = target.files[0];
    }
  };

  const postPayment = async (token: string): Promise<void> => {
    if (!selectedFile.value) {
      alert("Please select a file first");
      return;
    }

    isUploading.value = true;

    const formData = new FormData();
    formData.append("file", selectedFile.value);
    formData.append("reason", "Reason e plm");

    console.log(token);
    try {
      const res = await fetch("/api/v1/requests/payments/upload", {
        method: "POST",
        headers: { Authorization: `Bearer ${token}` },
        body: formData,
      });

      selectedFile.value = null;
      const body = await res.json();
      console.log(body);

      if (!res.ok) throw new Error("Invalid credentials");
    } catch (e: any) {
      error.value = e.message;
    } finally {
      loading.value = false;
    }
  };

  const getPersonalPayments = async (token: string): Promise<void> => {
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

      personalPayments.value = body;

      if (!res.ok) throw new Error("Invalid credentials");
    } catch (e: any) {
      error.value = e.message;
    } finally {
      loading.value = false;
    }
  };

  const getPaymentsToDecide = async (token: string): Promise<void> => {
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
      paymentsToDecide.value = body;

      if (!res.ok) throw new Error("Invalid credentials");
    } catch (e: any) {
      error.value = e.message;
    } finally {
      loading.value = false;
    }
  };

  const decidePayment = async (
    token: string,
    exp: Payment,
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
    postPayment,
    error,
    decidePayment,
    loading,
    getPersonalPayments,
    getPaymentsToDecide,
    handleFileChange,
    isUploading,
    selectedFile,
    personalPayments,
    paymentsToDecide,
  };
};
