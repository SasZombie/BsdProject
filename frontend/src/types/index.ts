export type Role =
  | "EMPLOYEE"
  | "ADMIN"
  | "MANAGER"
  | "CEO"
  | "CFO"
  | "BOARD_DIRECTOR"
  | "ACCOUNTANT";

export type Status = "PENDING" | "APPROVED" | "REJECTED" | "VETOED";

export interface User {
  token: string;
  role: Role;
  name: string;
}

export interface BackEndUser {
  id: string;
  name: string;
  surname: string;
  username: string;
  reportsTo: string;
  role: Role;
}

export interface Expense {
  id: number;
  reason: String;
  createdAt: Date;
  updatedAt: Date;
  status: Status;
  ammountOfDollars: number;
  needsApprovalBy: string[];
  approvedBy: string[];
  rejectedBy: string[];
  postedBy: string;
}

export interface ActiveExpense {
  id: number;
  reason: String;
  createdAt: Date;
  updatedAt: Date;
  ammountOfDollars: number;
  needsApprovalBy: string[];
  approvedBy: string[];
}

export interface PastExpsnse {
  id: number;
  reason: String;
  createdAt: Date;
  updatedAt: Date;
  status: Status;
  ammountOfDollars: number;
  approvedBy: string[];
  rejectedBy: string[];
}

export interface ExtenedExpense {
  expense: Expense;
  user: BackEndUser;
}

export interface ExpenseForm {
  id: number;
  reason: String;
  createdAt: Date;
  status: Status;
  ammountOfDollars: number;
  userName: string;
}

export interface ExpensesDecided {
  id: number;
  reason: String;
  createdAt: Date;
  status: Status;
  personalStatus: Status;
  ammountOfDollars: number;
  userName: string;
}

export interface Payment {
  id: number;
  reason: String;
  createdAt: Date;
  updatedAt: Date;
  status: Status;
  needsApprovalBy: string[];
  approvedBy: string[];
  rejectedBy: string[];
  userName: string
  
}

export interface DecisionPayments extends Payment
{
  personalStatus: Status;
}
export interface BasePayment {
  id: number;
  reason: String;
  createdAt: Date;
  updatedAt: Date;
}

export interface ActivePayment extends BasePayment {
  needsApprovalBy: string[];
  approvedBy: string[];
}

export interface PastPayment extends BasePayment {
  approvedBy: string[];
  rejectedBy: string[];
  status: Status;
}

export interface PaymentToDecide extends BasePayment
{
  userName: string
}

export interface PaymentDecided extends BasePayment
{
  userName: string
  status: Status
  personalStatus: Status
}

export interface Tab {
  id: string;
  label: string;
  show: boolean;
}

export interface UserProps {
  currentUser: User;
  extraOptions: boolean;
}
