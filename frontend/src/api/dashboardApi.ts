import type { DashboardStats } from "../types/transaction";
import { handleResponse } from "./httpClient";

export async function fetchDashboardStats(): Promise<DashboardStats> {
  const response = await fetch("/api/dashboard");
  return handleResponse<DashboardStats>(response);
}
