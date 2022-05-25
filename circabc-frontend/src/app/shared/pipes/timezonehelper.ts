export type TimeZoneLookupKeys = keyof typeof TimeZoneHelper.timeZoneLookup;
export class TimeZoneHelper {
  public static timeZoneLookup = {
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
}
