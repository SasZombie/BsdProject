export type Role = 
    'EMPLOYEE'|
    'ADMIN'|
    'MANAGER'|
    'CEO'|
    'CFO'|
    'BOARD_DIRECTOR'|
    'ACCOUNTANT';


export interface User{
    token: string,
    role: Role
}

export interface BackEndUser
{
    id: string,
    name: string,
    surname: string,
    username: string,
    reportsTo: string,
    role: Role,
}