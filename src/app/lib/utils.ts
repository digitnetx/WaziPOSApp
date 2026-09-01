
import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function generateControlNumber(): string {
  return Math.floor(200000000000 + Math.random() * 799999999999).toString();
}

export function generateTransactionId(): string {
  return 'TXN-' + Math.random().toString(36).substring(2, 9).toUpperCase();
}

export function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('en-TZ', {
    style: 'currency',
    currency: 'TZS',
    minimumFractionDigits: 0,
  }).format(amount);
}
