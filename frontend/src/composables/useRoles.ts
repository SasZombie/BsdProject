import type { Role } from "../types"

export const useRoles = (userRole: Role) =>
({

    renderExpenseExtra: ['MANAGER', 'CEO', 'CFO', 'BOARD_DIRECTOR'].includes(userRole),
    renderPayments: ['ACCOUNTANT', 'MANAGER', 'CEO', 'CFO', 'BOARD_DIRECTOR'].includes(userRole),
    renderPaymentsExtra: ['MANAGER', 'CEO', 'CFO', 'BOARD_DIRECTOR'].includes(userRole),
    renderAdmin: 'ADMIN' === userRole,
    renderAll: ['CEO', 'CFO', 'BOARD_DIRECTOR'].includes(userRole)
    // const renderExpense: boolean = false

})