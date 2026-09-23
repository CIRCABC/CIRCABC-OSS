/**
 * Union type of the valid GMT time-zone identifier keys accepted by the
 * {@link timeZoneLookup} map (for example `'GMT'`, `'GMT-5'`, `'GMT+2'`).
 *
 * Derived directly from the keys of {@link timeZoneLookup} so the two stay in
 * sync, and used to constrain lookups to known time zones at compile time.
 */
export type TimeZoneLookupKeys = keyof typeof timeZoneLookup;

/**
 * Maps human-readable GMT time-zone labels to their canonical UTC offset
 * strings in `±HH:mm` format.
 *
 * Keys range from `'GMT-12'` through `'GMT+11'`, with the base `'GMT'` key
 * resolving to `'+00:00'`. The resulting offset strings are suitable for
 * building ISO-8601 timestamps or configuring date/time components that expect
 * an explicit UTC offset.
 */
export const timeZoneLookup = {
  'GMT-12': '-12:00',
  'GMT-11': '-11:00',
  'GMT-10': '-10:00',
  'GMT-9': '-09:00',
  'GMT-8': '-08:00',
  'GMT-7': '-07:00',
  'GMT-6': '-06:00',
  'GMT-5': '-05:00',
  'GMT-4': '-04:00',
  'GMT-3': '-03:00',
  'GMT-2': '-02:00',
  'GMT-1': '-01:00',
  GMT: '+00:00',
  'GMT+1': '+01:00',
  'GMT+2': '+02:00',
  'GMT+3': '+03:00',
  'GMT+4': '+04:00',
  'GMT+5': '+05:00',
  'GMT+6': '+06:00',
  'GMT+7': '+07:00',
  'GMT+8': '+08:00',
  'GMT+9': '+09:00',
  'GMT+10': '+10:00',
  'GMT+11': '+11:00',
};
