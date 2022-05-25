import { DistributionMail } from 'app/core/generated/circabc';

export interface SelectablePagedDistributionMails {
  data: SelectableDistributionMail[];
  total: number;
}

export interface SelectableDistributionMail extends DistributionMail {
  selected: boolean;
}
