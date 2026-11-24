import { ref } from "vue";
import type {
  ActivePayment,
  DecisionPayments,
  PastPayment,
  Payment,
  PaymentDecided,
  PaymentToDecide,
  Status,
} from "../types";

export const usePayments = () => {
  const error = ref<string | null>(null);
  const loading = ref<boolean>(false);

  const allPayments = ref<Payment[]>([])
  const activePersonalPayments = ref<ActivePayment[]>([]);
  const pastPersonalPayments = ref<PastPayment[]>([]);

  const paymentsToDecide = ref<PaymentToDecide[]>([]);
  const paymentsDecided = ref<PaymentDecided[]>([]);
  const selectedFile = ref<File | null>(null);
  const isUploading = ref<boolean>(false);

  const handleFileChange = (event: Event) => {
    const target = event.target as HTMLInputElement;
    if (target.files && target.files.length > 0) {
      selectedFile.value = target.files[0];
    }
  };

  const vetoPayment = async (token: string, exp: number): Promise<void> => {
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

      console.log("AM trecut");
      // const target = allPa.value.find((elem) => elem.id === exp);
      // target && (target.status = "VETOED");
    } catch (e: any) {
      error.value = e.message;
    } finally {
      loading.value = false;
    }
  };

  const postPayment = async (token: string): Promise<void> => {
    if (!selectedFile.value) {
      alert("Please select a file first");
      return;
    }

    isUploading.value = true;

    console.log("AAA");

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

      if (!res.ok) throw new Error("Invalid credentials");

      selectedFile.value = null;
      const body: Payment = await res.json();
      console.log(body);

      activePersonalPayments.value.push(body);
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
      const res = await fetch("/api/v1/requests/payments/personal", {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });

      if (!res.ok) throw new Error("Invalid credentials");

      const body: Payment[] = await res.json();
      console.log(body);
      const temp1: ActivePayment[] = [];
      const temp2: PastPayment[] = [];

      for (const e of body) {
        if (e.status === "PENDING") {
          temp1.push({
            id: e.id,
            reason: e.reason,
            createdAt: e.createdAt,
            updatedAt: e.updatedAt,
            needsApprovalBy: e.needsApprovalBy,
            approvedBy: e.approvedBy,
          });
        } else {
          temp2.push({
            id: e.id,
            reason: e.reason,
            createdAt: e.createdAt,
            updatedAt: e.updatedAt,
            status: e.status,
            approvedBy: e.approvedBy,
            rejectedBy: e.rejectedBy,
          });
        }
      }

      activePersonalPayments.value = temp1;
      pastPersonalPayments.value = temp2;
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
      const res = await fetch("/api/v1/requests/payments/toDecide", {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });

      if (!res.ok) throw new Error("Invalid credentials");

      const body: DecisionPayments[] = await res.json();

      const temp1: PaymentToDecide[] = [];
      const temp2: PaymentDecided[] = [];

      for (const item of body) {
        if (item.status === "PENDING") {
          temp1.push({
            id: item.id,
            createdAt: item.createdAt,
            reason: item.reason,
            userName: item.userName,
            updatedAt: item.updatedAt
          });
        } else {
          temp2.push({
            id: item.id,
            createdAt: item.createdAt,
            updatedAt: item.updatedAt,
            reason: item.reason,
            status: item.status,
            personalStatus: item.personalStatus,
            userName: item.userName,
          });
        }
      }

      paymentsToDecide.value = temp1;
      paymentsDecided.value = temp2;

    } catch (e: any) {
      error.value = e.message;
    } finally {
      loading.value = false;
    }
  };

  const getAllPayments = async (token: string): Promise<void> => {
    loading.value = true;
    error.value = null;

    console.log(token);
    try {
      const res = await fetch("/api/v1/requests/payments/allPayments", {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });

      if (!res.ok) throw new Error("Invalid credentials");

      const body = await res.json();
      console.log("Payments to decide: ", body);
      paymentsToDecide.value = body;
    } catch (e: any) {
      error.value = e.message;
    } finally {
      loading.value = false;
    }
  };

  const decidePayment = async (
    token: string,
    pay: number,
    decision: Status,
  ): Promise<void> => {
    console.log(token);
    try {
      const res = await fetch("/api/v1/requests/payments/decision", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          expenseId: pay,
          decision: decision,
          comment: "Bla Bla Bla",
        }),
      });

      const body: PaymentDecided = await res.json();
      console.log(body);

      paymentsDecided.value.push(body)

      if (!res.ok) throw new Error("Invalid credentials");
    } catch (e: any) {
      error.value = e.message;
    } finally {
      loading.value = false;
    }
  };

  const downloadPayment = async (
    token: string,
    pay: Payment,
  ): Promise<void> => {
    console.log(token);
    try {
      const res = await fetch(`/api/v1/requests/payments/download/${pay.id}`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });

      if (!res.ok) throw new Error("Invalid credentials");

      const blob = await res.blob();

      const downloadUrl = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = downloadUrl;
      link.download = "resource";
      document.body.appendChild(link);
      link.click();

      link.remove();
      window.URL.revokeObjectURL(downloadUrl);
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
    activePersonalPayments,
    paymentsToDecide,
    pastPersonalPayments,
    paymentsDecided,
    allPayments,
    downloadPayment,
    vetoPayment,
    getAllPayments,
  };
};
