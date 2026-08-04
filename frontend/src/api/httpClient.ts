import type { ApiError } from "../types/transaction";

export async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let message = `Request failed with status ${response.status}`;
    try {
      const body: ApiError = await response.json();
      message = body.message ?? message;
    } catch {
      // response body wasn't JSON (or was empty) — fall back to the generic message
    }
    throw new Error(message);
  }
  // 204 No Content etc. would have no body to parse
  const text = await response.text();
  return text ? (JSON.parse(text) as T) : (undefined as T);
}
