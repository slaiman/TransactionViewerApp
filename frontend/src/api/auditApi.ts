import type { AuditEvent } from "../types/audit";
import { handleResponse } from "./httpClient";

export async function fetchAuditEvents(): Promise<AuditEvent[]> {
  const response = await fetch("/api/audit");
  return handleResponse<AuditEvent[]>(response);
}
