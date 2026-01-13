export interface User {
  id: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  twoFactorEnabled: boolean;
}

export namespace AuthResponse {
  export interface UserDto {
    id: number;
    username: string;
    email: string;
    firstName?: string;
    lastName?: string;
    twoFactorEnabled: boolean;
  }
}
