import { Profile, User } from 'app/core/generated/circabc';
export interface PermDef {
  authority: User | Profile;
  permission: string;
}

export interface AuthConfig {
  authKey: string;
  authValue: PermDef;
}
