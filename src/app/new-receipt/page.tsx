
"use client";

import React, { useState } from 'react';
import { Navbar } from '@/components/layout/Navbar';
import { ReceiptForm } from '@/components/receipt/ReceiptForm';
import { ThermalReceipt } from '@/components/receipt/ThermalReceipt';
import { Receipt } from '@/lib/types';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { 
  Printer, 
  Download, 
  ArrowLeft, 
  CheckCircle2, 
  FileText
} from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { useFirestore } from '@/firebase';
import { collection, addDoc } from 'firebase/firestore';
import { errorEmitter } from '@/firebase/error-emitter';
import { printReceiptOnSunmi } from '@/lib/sunmi-bridge';
import { FirestorePermissionError } from '@/firebase/errors';
import { 
  Tabs, 
  TabsContent, 
  TabsList, 
  TabsTrigger 
} from "@/components/ui/tabs";

export default function NewReceiptPage() {
  const [currentReceipt, setCurrentReceipt] = useState<Receipt | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [paperSize, setPaperSize] = useState<'58mm' | '80mm'>('58mm');
  const { toast } = useToast();
  const firestore = useFirestore();

  const handleReceiptGenerated = async (receipt: Receipt) => {
    if (!firestore) return;
    
    // Instant UI feedback
    setCurrentReceipt(receipt);
    setIsSubmitting(false);
    
    const receiptsRef = collection(firestore, 'receipts');
    
    addDoc(receiptsRef, receipt)
      .catch(async (error) => {
        const permissionError = new FirestorePermissionError({
          path: receiptsRef.path,
          operation: 'create',
          requestResourceData: receipt,
        });
        errorEmitter.emit('permission-error', permissionError);
      });

    toast({
      title: "Bill Issued",
      description: `Control Number: ${receipt.controlNumber} generated.`,
    });
  };

  const handlePrint = () => {
    if (currentReceipt) {
      const printedBySunmi = printReceiptOnSunmi(currentReceipt);
      if (printedBySunmi) {
        toast({
          title: "Receipt sent to SUNMI printer",
          description: "The government bill was sent to the built-in 58mm printer.",
        });
        return;
      }
    }

    // Browser fallback for desktop/non-SUNMI environments.
    window.print();
  };

  const reset = () => {
    setCurrentReceipt(null);
  };

  return (
    <div className="min-h-svh bg-background pb-10">
      <Navbar />
      <div className="container mx-auto px-4 py-4 md:py-8">
        {!currentReceipt ? (
          <div className="max-w-4xl mx-auto">
            <div className="mb-4 md:mb-6">
              <h1 className="text-xl md:text-3xl font-headline font-bold text-primary">Issue Government Bill</h1>
            </div>
            <Card className="border-accent/20">
              <CardContent className="p-4 md:p-6">
                <ReceiptForm onSubmit={handleReceiptGenerated} isSubmitting={isSubmitting} />
              </CardContent>
            </Card>
          </div>
        ) : (
          <div className="max-w-6xl mx-auto space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
            <div className="text-center space-y-2">
              <CheckCircle2 className="w-10 h-10 md:w-16 md:h-16 text-primary mx-auto" />
              <h1 className="text-xl md:text-3xl font-headline font-bold">Payment Order Ready</h1>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 lg:gap-8 items-start">
              <div className="lg:col-span-5 space-y-4 md:space-y-6">
                <Card className="shadow-lg border-primary/10">
                  <CardHeader className="p-4 md:p-6">
                    <CardTitle className="text-base md:text-lg flex items-center gap-2">
                      <FileText className="w-5 h-5" />
                      Actions
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="p-4 md:p-6 pt-0 space-y-4">
                    <Button onClick={handlePrint} className="w-full gap-2 h-14 text-lg font-bold shadow-xl">
                      <Printer className="w-5 h-5" />
                      Print Receipt
                    </Button>
                    <div className="grid grid-cols-2 gap-2">
                      <Button variant="outline" className="gap-2 h-11">
                        <Download className="w-4 h-4" />
                        Download
                      </Button>
                      <Button variant="ghost" onClick={reset} className="gap-2 h-11">
                        <ArrowLeft className="w-4 h-4" />
                        New Entry
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              </div>

              <div className="lg:col-span-7 space-y-4">
                <div className="flex items-center justify-between mb-2">
                  <h3 className="text-xs font-bold uppercase tracking-wider text-muted-foreground">Scanned Receipt Preview</h3>
                  <Tabs value={paperSize} onValueChange={(v: any) => setPaperSize(v)}>
                    <TabsList className="h-8 bg-muted/50">
                      <TabsTrigger value="58mm" className="text-[10px] h-6">58mm</TabsTrigger>
                      <TabsTrigger value="80mm" className="text-[10px] h-6">80mm</TabsTrigger>
                    </TabsList>
                  </Tabs>
                </div>
                
                {/* Preview Container simulating a scanned document view */}
                <div className="relative bg-neutral-200 dark:bg-neutral-800 rounded-xl p-4 md:p-12 overflow-hidden flex justify-center border shadow-inner min-h-[600px]">
                  <div className="absolute inset-0 opacity-10 pointer-events-none bg-[radial-gradient(#000_1px,transparent_1px)] [background-size:30px_30px]" />
                  
                  <div id="thermal-receipt-print-area" className="relative z-10 scale-90 sm:scale-100 origin-top shadow-[0_20px_50px_rgba(0,0,0,0.2)]">
                    <ThermalReceipt 
                      receipt={currentReceipt} 
                      paperWidth={paperSize}
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
