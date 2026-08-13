export type AccountStatus = "ACTIVE" | "CLOSED";

export interface Account {
  id: string;
  accountHolderName: string;
  status: AccountStatus;
  createdDate: string; // YYYY-MM-DD
  balance: number;
}

export interface CreateAccountRequest {
  accountHolderName: string;
}

export interface UpdateAccountRequest {
  accountHolderName?: string;
  status?: AccountStatus;
}

export interface AccountBalance {
  accountId: string;
  balance: number;
}
