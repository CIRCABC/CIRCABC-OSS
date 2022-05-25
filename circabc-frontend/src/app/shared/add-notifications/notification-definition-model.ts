import { Profile, User } from 'app/core/generated/circabc';
export interface NotifDef {
  authority: User | Profile;
  notifications: string;
}

export interface AuthConfig {
  authKey: string;
  authValue: NotifDef;
}
