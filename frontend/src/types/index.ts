export type Role =
  | "EMPLOYEE"
  | "ADMIN"
  | "MANAGER"
  | "CEO"
  | "CFO"
  | "BOARD_DIRECTOR"
  | "ACCOUNTANT";

export type Status =
  | "PENDING"
  | "APPROVED"
  | "REJECTED"
  | "VETOED"


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
  id: number,
  reason: String;
  createdAt: Date;
  updatedAt: Date;
  status: Status;
  ammountOfDollars: number;
}
