
"use client";

import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { ReceiptText, Wallet, Users, TrendingUp } from 'lucide-react';
import { formatCurrency } from '@/app/lib/utils';
import { Stats } from '@/lib/types';

interface StatsOverviewProps {
  stats: Stats;
}

export const StatsOverview: React.FC<StatsOverviewProps> = ({ stats }) => {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
      <Card className="border-accent/20">
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">Daily Revenue</CardTitle>
          <Wallet className="h-4 w-4 text-primary opacity-70" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">{formatCurrency(stats.totalRevenueToday)}</div>
          <p className="text-xs text-muted-foreground mt-1">
            <TrendingUp className="inline w-3 h-3 mr-1 text-green-600" />
            +12% from yesterday
          </p>
        </CardContent>
      </Card>
      
      <Card className="border-accent/20">
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">Total Receipts</CardTitle>
          <ReceiptText className="h-4 w-4 text-primary opacity-70" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">{stats.totalReceiptsToday}</div>
          <p className="text-xs text-muted-foreground mt-1">Issued today</p>
        </CardContent>
      </Card>

      <Card className="border-accent/20">
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">Total Visitors</CardTitle>
          <Users className="h-4 w-4 text-primary opacity-70" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">{stats.totalVisitorsToday}</div>
          <p className="text-xs text-muted-foreground mt-1">Counted across all bills</p>
        </CardContent>
      </Card>
    </div>
  );
};
