export const ActionResult = {
  SUCCEED: 1,
  CANCELED: 0,
  FAILED: -1,
} as const;

export type ActionResult = (typeof ActionResult)[keyof typeof ActionResult];
