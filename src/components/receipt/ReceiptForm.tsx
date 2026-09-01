
"use client";

import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { format } from 'date-fns';
import { Loader2, Sparkles, Save } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import { Receipt } from '@/lib/types';
import { generateControlNumber, generateTransactionId } from '@/app/lib/utils';
import { autoGenerateReceiptInstructions } from '@/ai/flows/auto-generate-receipt-instructions';

const formSchema = z.object({
  customerName: z.string().min(2, "Name is required"),
  customerPhone: z.string().min(10, "Valid phone number is required"),
  visitorType: z.enum(['International Tourist', 'Local Resident', 'School Group', 'EAC Resident']),
  numPeople: z.coerce.number().min(1, "At least 1 person"),
  amount: z.coerce.number().min(0, "Amount must be positive"),
  currency: z.enum(['TZS', 'USD']),
  paymentOption: z.enum(['Exact', 'Partial']),
  posCenterName: z.string().min(2, "POS Center name is required"),
  notes: z.string(),
  staffName: z.string().min(2, "Staff name is required"),
  billItem: z.string().min(2, "Bill Item name is required"),
});

interface ReceiptFormProps { onSubmit: (receipt: Receipt) => void; isSubmitting?: boolean; }

export const ReceiptForm: React.FC<ReceiptFormProps> = ({ onSubmit, isSubmitting }) => {
  const [isGeneratingAI, setIsGeneratingAI] = useState(false);
  const [language, setLanguage] = useState<'English' | 'Swahili'>('English');
  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      customerName: 'Sajid', customerPhone: '0000000000', visitorType: 'International Tourist',
      numPeople: 1, amount: 10000, currency: 'TZS', paymentOption: 'Exact',
      posCenterName: 'CHANGU BAWE MINERAL CONSERVATION AREA (CHABAMCA)', notes: '',
      staffName: 'Mwanaid Khamis', billItem: 'Entrance fees per day/person',
    },
  });
  const visitorType = form.watch('visitorType');
  const posCenterName = form.watch('posCenterName');
  const handleGenerateInstructions = async () => {
    setIsGeneratingAI(true);
    try {
      const result = await autoGenerateReceiptInstructions({ visitorType, posCenterName, language });
      form.setValue('notes', result.instructions);
    } catch (error) { console.error("AI Generation failed", error); }
    finally { setIsGeneratingAI(false); }
  };
  const handleFormSubmit = (values: z.infer<typeof formSchema>) => {
    const now = new Date();
    const expiry = new Date(now);
    expiry.setDate(expiry.getDate() + 1);
    const receipt = {
      ...values, id: crypto.randomUUID(), controlNumber: generateControlNumber(), transactionId: generateTransactionId(),
      printedAt: format(now, 'yyyy-MM-dd HH:mm:ss'), expiryDate: format(expiry, 'yyyy-MM-dd HH:mm:ss'), printedBy: values.staffName,
    } as unknown as Receipt;
    onSubmit(receipt);
  };
  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(handleFormSubmit)} className="space-y-4 md:space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <FormField control={form.control} name="customerName" render={({ field }) => (<FormItem className="md:col-span-2"><FormLabel className="text-xs md:text-sm">Payer Name</FormLabel><FormControl><Input placeholder="Enter payer name" {...field} className="h-9 md:h-10 text-sm" /></FormControl><FormMessage /></FormItem>)} />
          <FormField control={form.control} name="customerPhone" render={({ field }) => (<FormItem><FormLabel className="text-xs md:text-sm">Payer Phone</FormLabel><FormControl><Input placeholder="e.g. 0000000000" {...field} className="h-9 md:h-10 text-sm" /></FormControl><FormMessage /></FormItem>)} />
          <FormField control={form.control} name="billItem" render={({ field }) => (<FormItem><FormLabel className="text-xs md:text-sm">Bill Item</FormLabel><FormControl><Input placeholder="e.g. Entrance fees..." {...field} className="h-9 md:h-10 text-sm" /></FormControl><FormMessage /></FormItem>)} />
          <FormField control={form.control} name="visitorType" render={({ field }) => (<FormItem><FormLabel className="text-xs md:text-sm">Visitor Category</FormLabel><Select onValueChange={field.onChange} defaultValue={field.value}><FormControl><SelectTrigger className="h-9 md:h-10 text-sm"><SelectValue placeholder="Select type" /></SelectTrigger></FormControl><SelectContent><SelectItem value="International Tourist">International Tourist</SelectItem><SelectItem value="Local Resident">Local Resident</SelectItem><SelectItem value="School Group">School Group</SelectItem><SelectItem value="EAC Resident">EAC Resident</SelectItem></SelectContent></Select><FormMessage /></FormItem>)} />
          <div className="grid grid-cols-2 gap-4">
            <FormField control={form.control} name="numPeople" render={({ field }) => (<FormItem><FormLabel className="text-xs md:text-sm">Quantity</FormLabel><FormControl><Input type="number" {...field} className="h-9 md:h-10 text-sm" /></FormControl><FormMessage /></FormItem>)} />
            <FormField control={form.control} name="paymentOption" render={({ field }) => (<FormItem><FormLabel className="text-xs md:text-sm">Pay Option</FormLabel><Select onValueChange={field.onChange} defaultValue={field.value}><FormControl><SelectTrigger className="h-9 md:h-10 text-sm"><SelectValue /></SelectTrigger></FormControl><SelectContent><SelectItem value="Exact">Exact</SelectItem><SelectItem value="Partial">Partial</SelectItem></SelectContent></Select><FormMessage /></FormItem>)} />
          </div>
          <FormField control={form.control} name="staffName" render={({ field }) => (<FormItem><FormLabel className="text-xs md:text-sm">Issued By</FormLabel><FormControl><Input placeholder="Staff name" {...field} className="h-9 md:h-10 text-sm" /></FormControl><FormMessage /></FormItem>)} />
          <FormField control={form.control} name="posCenterName" render={({ field }) => (<FormItem><FormLabel className="text-xs md:text-sm">POS Center</FormLabel><FormControl><Input placeholder="Center name" {...field} className="h-9 md:h-10 text-sm" /></FormControl><FormMessage /></FormItem>)} />
          <FormField control={form.control} name="amount" render={({ field }) => (<FormItem><FormLabel className="text-xs md:text-sm font-bold">Total Amount</FormLabel><FormControl><Input type="number" step="0.01" {...field} className="h-10 md:h-12 text-lg font-bold border-primary/30" /></FormControl><FormMessage /></FormItem>)} />
          <FormField control={form.control} name="currency" render={({ field }) => (<FormItem><FormLabel className="text-xs md:text-sm font-bold">Currency</FormLabel><Select onValueChange={field.onChange} defaultValue={field.value}><FormControl><SelectTrigger className="h-10 md:h-12 text-sm font-bold"><SelectValue placeholder="Select currency" /></SelectTrigger></FormControl><SelectContent><SelectItem value="TZS">TZS — Tanzanian Shilling</SelectItem><SelectItem value="USD">USD — US Dollar</SelectItem></SelectContent></Select><FormMessage /></FormItem>)} />
        </div>
        <div className="space-y-4 pt-4 border-t">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3"><FormLabel className="flex items-center gap-2 text-xs md:text-sm">Additional Notes (AI Generated)<Sparkles className="w-3.5 h-3.5 text-primary" /></FormLabel><div className="flex w-full sm:w-auto gap-2"><Select value={language} onValueChange={(v: any) => setLanguage(v)}><SelectTrigger className="flex-1 sm:w-[100px] h-8 text-[11px]"><SelectValue /></SelectTrigger><SelectContent><SelectItem value="English">English</SelectItem><SelectItem value="Swahili">Swahili</SelectItem></SelectContent></Select><Button type="button" variant="outline" size="sm" className="h-8 px-2 shrink-0 text-[11px] gap-1.5" onClick={handleGenerateInstructions} disabled={isGeneratingAI}>{isGeneratingAI ? <Loader2 className="w-3 h-3 animate-spin" /> : <Sparkles className="w-3 h-3" />}Generate</Button></div></div>
          <FormField control={form.control} name="notes" render={({ field }) => (<FormItem><FormControl><Textarea placeholder="Transaction details or special instructions..." className="min-h-[70px] text-xs md:text-sm" {...field} /></FormControl><FormMessage /></FormItem>)} />
        </div>
        <div className="pt-2"><Button type="submit" className="w-full gap-2 text-base md:text-lg h-12 md:h-14 font-bold" disabled={isSubmitting}>{isSubmitting ? <Loader2 className="w-5 h-5 animate-spin" /> : <><Save className="w-5 h-5" />Generate Government Bill</>}</Button></div>
      </form>
    </Form>
  );
};
