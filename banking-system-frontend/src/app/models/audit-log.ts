export interface AuditLog {
  id: number;
  action: string;
  performedBy: string;
  details?: string;
  timestamp?: string; // ISO string
  userId:number;
}
