export interface CardEntry {
  id: number;
  type: string;
  title: string;
  text: string;
  date: string;
  size: number;
  skin: string;
  height?: number;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  properties?: { [key: string]: any };
}
