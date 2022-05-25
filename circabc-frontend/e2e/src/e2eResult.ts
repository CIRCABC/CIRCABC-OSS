export class E2eResult {
  result: boolean;
  comment: string;
  returnData = '';

  constructor(result: boolean, comment: string, returnValue: string) {
    this.result = result;
    this.comment = comment;
    this.returnData = returnValue;
  }
}
