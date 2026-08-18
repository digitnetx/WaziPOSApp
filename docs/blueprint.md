# **App Name**: Wazi POS

## Core Features:

- Intelligent Receipt Form: Standardized entry form for billing visitor fees with automated logic for partial/exact payments and expiry tracking.
- AI Instruction Tool: An AI-powered tool that automatically drafts customized Swahili or English transaction instructions in the 'Notes' section based on visitor type and center rules.
- Thermal Preview Engine: CSS-isolated layout to view and manage 58mm/80mm receipt previews that accurately mirror physical thermal prints.
- Control Number Vault: Auto-generation of unique 12-digit Tanzanian-style control numbers stored in Firestore for financial integrity.
- Admin Control Center: Role-based authentication allowing staff to view receipt history and daily revenue stats in a dedicated dashboard.
- Browser-Native Thermal Printing: Direct 'Print' functionality utilizing media queries for Bluetooth and mobile thermal printers without middleware.
- Historical Audit & Export: Persistent archive of all receipts searchable by Control Number with one-click export to Excel or PDF reports.

## Style Guidelines:

- Primary color: Deep Estate Green (#233D2D) to evoke trust and its relevance to conservation areas.
- Background color: Paper Tint White (#F7F9F7), a soft white with a hint of green saturation to reduce outdoor screen glare.
- Accent color: Sage Shadow (#6F8F7B) used for subtle borders and secondary UI elements to maintain a professional, low-distraction environment.
- Main font pairing: 'Inter' for UI labels and dashboard data due to its objective clarity; 'Source Code Pro' for receipt previews to mimic the mechanical look of POS characters.
- Functional, medium-weight outline icons that ensure high legibility even under direct sunlight during outdoor operations.
- Compact, mobile-first design featuring bottom-anchored actions for efficient thumb navigation by kiosk staff.
- Micro-interactions when a receipt is 'signed' and generated to provide instant confirmation that the Control Number has been synced.