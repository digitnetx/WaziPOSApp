
"use client";

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Receipt, ShieldCheck, Printer } from 'lucide-react';

export default function Home() {
  const router = useRouter();

  useEffect(() => {
    // In a real app, check auth state here
    // For now, we allow access to dashboard
  }, []);

  return (
    <div className="min-h-svh bg-background flex flex-col items-center justify-center p-4">
      <div className="w-full max-w-md space-y-8 text-center">
        <div className="flex flex-col items-center gap-2">
          <div className="bg-primary p-4 rounded-2xl shadow-lg">
            <Receipt className="h-10 w-10 text-primary-foreground" />
          </div>
          <h1 className="text-4xl font-headline font-extrabold tracking-tight text-primary">Wazi POS</h1>
          <p className="text-muted-foreground">Government Receipt Generation System</p>
        </div>

        <Card className="border-accent/20 shadow-xl overflow-hidden">
          <CardHeader className="bg-primary/5 pb-8">
            <CardTitle>Staff Login</CardTitle>
            <CardDescription>Enter your credentials to access the POS terminal</CardDescription>
          </CardHeader>
          <CardContent className="pt-6 space-y-4">
            <div className="space-y-2 text-left">
              <label className="text-sm font-medium">Email Address</label>
              <input 
                type="email" 
                defaultValue="admin@wazi.gov.tz" 
                className="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-primary/20" 
              />
            </div>
            <div className="space-y-2 text-left">
              <label className="text-sm font-medium">Password</label>
              <input 
                type="password" 
                defaultValue="********" 
                className="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-primary/20" 
              />
            </div>
            <Button className="w-full h-12 text-lg font-bold" onClick={() => router.push('/dashboard')}>
              Access Terminal
            </Button>
          </CardContent>
        </Card>

        <div className="grid grid-cols-2 gap-4 text-xs text-muted-foreground pt-8 border-t border-accent/20">
          <div className="flex flex-col items-center gap-1">
            <ShieldCheck className="h-4 w-4" />
            Secure Authentication
          </div>
          <div className="flex flex-col items-center gap-1">
            <Printer className="h-4 w-4" />
            Thermal Print Ready
          </div>
        </div>
      </div>
    </div>
  );
}
