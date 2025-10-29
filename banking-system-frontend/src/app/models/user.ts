import { Account } from "./account";
import { Role } from "./role";



export interface User {
  userId: number;
  username: string;
  email: string;
  password: string;
  phone: string;
  status: 'ACTIVE' | 'INACTIVE';
  roles?: Role[];
  accounts : Account[];
}
