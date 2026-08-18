
export type VisitorType = 'International Tourist' | 'Local Resident' | 'School Group' | 'EAC Resident';

export type PaymentOption = 'Exact' | 'Partial';

export interface Receipt {
  id: string;
  billItem: string;
  customerName: string;
  customerPhone: string;
  numPeople: number;
  amount: number;
  paymentOption: PaymentOption;
  expiryDate: string;
  controlNumber: string;
  posCenterName: string;
  printedBy: string;
  printedAt: string;
  notes: string;
  visitorType: VisitorType;
  transactionId: string;
}

export interface Stats {
  totalReceiptsToday: number;
  totalRevenueToday: number;
  totalVisitorsToday: number;
}
