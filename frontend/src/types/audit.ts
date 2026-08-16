export interface AuditEvent {
  id: string;
  operation: string;
  entityType: string;
  entityId: string;
  accountId: string;
  oldStatus: string | null;
  newStatus: string | null;
  timestamp: string; // ISO instant
  details: string;
}
