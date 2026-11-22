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
