import { Role } from "./role";

export interface loginResponse{
      token : String,
      userId : number,
     allRoles : Role[]

}