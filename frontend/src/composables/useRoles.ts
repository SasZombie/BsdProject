import type { Role } from "../types"

// export type Role =
//   | "EMPLOYEE"
//   | "ADMIN"
//   | "MANAGER"
//   | "CEO"
//   | "CFO"
//   | "BOARD_DIRECTOR"
//   | "ACCOUNTANT";

export const useRoles = (userRole: Role) =>
({

    renderExpenseExtra: ['MANAGER', 'CEO', 'CFO', 'BOARD_DIRECTOR'].includes(userRole),
    renderPayments: ['ACCOUNTANT', 'MANAGER', 'CEO', 'CFO', 'BOARD_DIRECTOR'].includes(userRole),
    renderPaymentsExtra: ['MANAGER', 'CEO', 'CFO', 'BOARD_DIRECTOR'].includes(userRole),
    renderAdmin: 'ADMIN' === userRole,
    // const renderExpense: boolean = false

})