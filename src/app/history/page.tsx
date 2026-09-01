
"use client";

import React, { useState } from 'react';
import { Navbar } from '@/components/layout/Navbar';
import { Receipt } from '@/lib/types';
import { formatCurrency } from '@/app/lib/utils';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Card } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Search, Printer, FileDown, Eye, Loader2 } from 'lucide-react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { ThermalReceipt } from '@/components/receipt/ThermalReceipt';
import { useFirestore, useCollection, useMemoFirebase } from '@/firebase';
import { collection, query, orderBy } from 'firebase/firestore';

export default function HistoryPage() {
  const [searchQuery, setSearchQuery] = useState('');
  const firestore = useFirestore();

  const historyQuery = useMemoFirebase(() => {
    if (!firestore) return null;
    return query(collection(firestore, 'receipts'), orderBy('printedAt', 'desc'));
  }, [firestore]);

  const { data: history, loading } = useCollection<Receipt>(historyQuery);

  const filtered = (history || []).filter(r => 
    r.customerName.toLowerCase().includes(searchQuery.toLowerCase()) || 
    r.controlNumber.includes(searchQuery) ||
    r.customerPhone.includes(searchQuery)
  );

  return (
    <div className="min-h-svh bg-background">
      <Navbar />
      <div className="container mx-auto px-4 py-6 md:py-8">
        <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center gap-4 mb-6 md:mb-8">
          <div className="space-y-1">
            <h1 className="text-2xl md:text-3xl font-headline font-bold">Transaction History</h1>
            <p className="text-sm md:text-base text-muted-foreground">Comprehensive log of all issued government bills.</p>
          </div>
          <div className="flex flex-col sm:flex-row w-full lg:w-auto gap-2">
            <div className="relative flex-1 sm:w-80">
              <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
              <Input 
                placeholder="Search name, phone or control number..." 
                className="pl-10 h-10"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            <Button variant="outline" className="gap-2 h-10">
              <FileDown className="w-4 h-4" />
              Export CSV
            </Button>
          </div>
        </div>

        <Card className="border-accent/20 overflow-hidden">
          {loading ? (
            <div className="flex items-center justify-center py-20">
              <Loader2 className="w-8 h-8 animate-spin text-primary/40" />
            </div>
          ) : (
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead className="whitespace-nowrap">Control Number</TableHead>
                    <TableHead className="whitespace-nowrap">Payer</TableHead>
                    <TableHead className="whitespace-nowrap">Visitor Type</TableHead>
                    <TableHead className="whitespace-nowrap">People</TableHead>
                    <TableHead className="whitespace-nowrap">Amount</TableHead>
                    <TableHead className="whitespace-nowrap">Date Issued</TableHead>
                    <TableHead className="text-right whitespace-nowrap">Action</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filtered.length > 0 ? (
                    filtered.map((r) => (
                      <TableRow key={r.id}>
                        <TableCell className="font-code font-bold text-primary text-xs md:text-sm whitespace-nowrap">{r.controlNumber}</TableCell>
                        <TableCell className="whitespace-nowrap">
                          <div className="flex flex-col">
                            <span className="font-medium text-xs md:text-sm">{r.customerName}</span>
                            <span className="text-[10px] md:text-xs text-muted-foreground">{r.customerPhone}</span>
                          </div>
                        </TableCell>
                        <TableCell className="text-[10px] md:text-xs whitespace-nowrap">{r.visitorType}</TableCell>
                        <TableCell className="text-xs md:text-sm whitespace-nowrap">{r.numPeople}</TableCell>
                        <TableCell className="font-semibold text-xs md:text-sm whitespace-nowrap">{formatCurrency(r.amount)}</TableCell>
                        <TableCell className="text-[10px] md:text-xs text-muted-foreground whitespace-nowrap">{r.printedAt}</TableCell>
                        <TableCell className="text-right whitespace-nowrap">
                          <Dialog>
                            <DialogTrigger asChild>
                              <Button variant="ghost" size="sm" className="gap-1.5 h-8">
                                <Eye className="w-4 h-4" />
                                <span className="hidden sm:inline">Preview</span>
                              </Button>
                            </DialogTrigger>
                            <DialogContent className="max-w-[95vw] sm:max-w-fit overflow-y-auto max-h-[90vh]">
                              <DialogHeader>
                                <DialogTitle>Thermal Receipt Preview</DialogTitle>
                              </DialogHeader>
                              <div className="p-4 bg-muted/50 rounded-lg flex justify-center overflow-hidden">
                                <div className="scale-90 sm:scale-100 origin-top">
                                  <ThermalReceipt receipt={r} />
                                </div>
                              </div>
                              <div className="flex gap-2 justify-end mt-4">
                                <Button variant="outline" className="gap-2" onClick={() => window.print()}>
                                  <Printer className="w-4 h-4" />
                                  Print
                                </Button>
                              </div>
                            </DialogContent>
                          </Dialog>
                        </TableCell>
                      </TableRow>
                    ))
                  ) : (
                    <TableRow>
                      <TableCell colSpan={7} className="text-center py-20 text-muted-foreground">
                        No matching receipts found in archives.
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </div>
          )}
        </Card>
      </div>
    </div>
  );
}
