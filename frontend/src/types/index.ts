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
    name: string,
    surrname: string,
    username: string,
    role: Role
}