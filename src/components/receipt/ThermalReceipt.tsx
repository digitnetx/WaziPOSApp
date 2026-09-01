
"use client";

import React from 'react';
import { Receipt } from '@/lib/types';

interface ThermalReceiptProps {
  receipt: Receipt;
  className?: string;
  id?: string;
  paperWidth?: '58mm' | '80mm';
}

export const ThermalReceipt: React.FC<ThermalReceiptProps> = ({ 
  receipt, 
  className, 
  id,
  paperWidth = '58mm' 
}) => {
  // Format values for thermal consistency
  const formattedAmount = `TZS ${new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(receipt.amount)}`;

  const [expiryDay, expiryTime] = (receipt.expiryDate || "").split(' ');
  const cleanExpiryTime = expiryTime ? expiryTime.replace(/:/g, '') : '';
  const formattedExpireDate = `${expiryDay} ${cleanExpiryTime}`;
  
  const [printDay, printTime] = (receipt.printedAt || "").split(' ');
  const isoPrintDate = `${printDay}T${printTime}`;

  // Container styling based on Arial 14px specification
  const containerStyle: React.CSSProperties = {
    width: paperWidth === '58mm' ? '58mm' : '80mm',
    backgroundColor: '#fff',
    color: '#000',
    fontFamily: 'Arial, Helvetica, sans-serif',
    fontSize: '14px',
    lineHeight: '1.45',
    padding: '35px 15px 40px 15px',
    boxSizing: 'border-box',
    textAlign: 'left',
    margin: '0 auto',
  };

  const boldStyle: React.CSSProperties = { fontWeight: 700 };
  const labelStyle: React.CSSProperties = { fontWeight: 400 };

  return (
    <div id={id} style={containerStyle} className={className}>
      {/* Header Section - Ministry and Title with exact vertical centering */}
      <div style={{ textAlign: 'center' }}>
        <div style={{ fontSize: '14px', fontWeight: 400 }}>
          Ministry of Blue Economy and Fisheries
        </div>
        
        {/* 'Government Bill' positioned exactly halfway between Ministry and Body */}
        <div style={{ margin: '35px 0', fontSize: '17px', fontWeight: 700 }}>
          Government Bill
        </div>
      </div>

      {/* Body Section */}
      <div style={{ marginBottom: '0px' }}>
        <div style={{ marginBottom: '4px' }}>
          <span style={labelStyle}>BillItem : </span>
          <span style={boldStyle}>{receipt.billItem}</span><br />
          <span style={boldStyle}>(TZS)</span>
        </div>

        <div style={{ marginBottom: '4px' }}>
          <span style={labelStyle}>Payer name : </span>
          <span style={boldStyle}>{receipt.customerName}</span>
        </div>

        <div style={{ marginBottom: '4px' }}>
          <span style={labelStyle}>Payer phone : </span>
          <span style={boldStyle}>{receipt.customerPhone}</span>
        </div>

        <div style={{ marginBottom: '4px' }}>
          <span style={labelStyle}>Amount : </span>
          <span style={boldStyle}>{formattedAmount}</span>
        </div>

        <div style={{ marginBottom: '4px' }}>
          <span style={labelStyle}>Pay option : </span>
          <span style={boldStyle}>{receipt.paymentOption}</span>
        </div>

        <div style={{ marginBottom: '4px' }}>
          <span style={labelStyle}>Expire Date : </span>
          <span style={boldStyle}>{formattedExpireDate}</span>
        </div>

        <div>
          <span style={labelStyle}>ControlNumber : </span>
          <span style={boldStyle}>{receipt.controlNumber}</span>
        </div>
      </div>

      {/* Instruction Section - No space after ControlNumber */}
      <div style={{ fontSize: '13.5px', lineHeight: '1.4', marginBottom: '35px' }}>
        Lipa kupitia Benki (NMB/BOT/PBZ) na<br />
        Mawakala wake au Mitandao ya Simu<br />
        (kwa kuchagua "Malipo ya Serikali")<br />
        Piga namba 0777350786 kwa maelezo<br />
        Zaidi.
      </div>

      {/* POS and Footer - Label and Value on the same line */}
      <div style={{ fontSize: '13px', lineHeight: '1.4' }}>
        <div style={{ marginBottom: '4px' }}>
          <span style={labelStyle}>POS center : </span>
          <span style={boldStyle}>{receipt.posCenterName}</span>
        </div>

        <div style={{ marginBottom: '4px' }}>
          <span style={labelStyle}>Printed on : </span>
          <span style={boldStyle}>{isoPrintDate}</span>
        </div>

        <div>
          <span style={labelStyle}>Printed By : </span>
          <span style={boldStyle}>{receipt.printedBy}</span>
        </div>
      </div>
    </div>
  );
};
