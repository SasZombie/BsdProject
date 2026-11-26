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

  const allPayments = ref<Payment[]>([]);
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
      const res = await fetch("/api/v1/requests/payments/veto", {
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

      const target = allPayments.value.find((elem) => elem.id === exp);
      target && (target.status = "VETOED");
    } catch (e: any) {
      error.value = e.message;
    } finally {
      loading.value = false;
    }
  };

  const postPayment = async (token: string): Promise<void> => {
    if (!selectedFile.value) {
      return;
    }

    isUploading.value = true;

    const formData = new FormData();
    formData.append("file", selectedFile.value);
    formData.append("reason", "Reason e plm");

    try {
      const res = await fetch("/api/v1/requests/payments/upload", {
        method: "POST",
        headers: { Authorization: `Bearer ${token}` },
        body: formData,
      });

      if (!res.ok) {
        const errorData = await res.json();

        const errorMessage =
          errorData.message ||
          errorData.detail ||
          errorData.error ||
          "Request failed";

        throw new Error(errorMessage);
      }

      selectedFile.value = null;
      const body: Payment = await res.json();

      activePersonalPayments.value.push(body);
    } catch (e: any) {
      error.value = e.message;
    } finally {
      loading.value = false;
      isUploading.value = false;
    }
  };

  const getPersonalPayments = async (token: string): Promise<void> => {
    loading.value = true;
    error.value = null;

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
            updatedAt: item.updatedAt,
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

    try {
      const res = await fetch("/api/v1/requests/payments/all", {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });

      if (!res.ok) throw new Error("Invalid credentials");

      const body: Payment[] = await res.json();
      allPayments.value = body;
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
    comment?: string,
  ): Promise<void> => {
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
          comment: comment ? comment : "",
        }),
      });

      const body: PaymentDecided = await res.json();

      paymentsDecided.value.push(body);

      if (!res.ok) throw new Error("Invalid credentials");
    } catch (e: any) {
      error.value = e.message;
    } finally {
      loading.value = false;
    }
  };

  const downloadPayment = async (token: string, pay: number): Promise<void> => {
    try {
      const res = await fetch(`/api/v1/requests/payments/download/${pay}`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });

      if (!res.ok) throw new Error("Invalid combinations or permisions");

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
