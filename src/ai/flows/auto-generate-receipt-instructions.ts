
'use server';
/**
 * @fileOverview This file implements a Genkit flow for automatically generating
 * receipt instructions in either Swahili or English based on visitor type
 * and POS center.
 *
 * - autoGenerateReceiptInstructions - A function that generates customized receipt notes/instructions.
 * - AutoGenerateReceiptInstructionsInput - The input type for the function.
 * - AutoGenerateReceiptInstructionsOutput - The return type for the function.
 */

import { ai } from '@/ai/genkit';
import { z } from 'genkit';

// Input Schema
const AutoGenerateReceiptInstructionsInputSchema = z.object({
  visitorType: z.string().describe('The type of visitor (e.g., "international tourist", "local resident", "school group").'),
  posCenterName: z.string().describe('The name of the POS center (e.g., "CHANGU BAWE MINERAL CONSERVATION AREA").'),
  language: z.enum(['Swahili', 'English']).describe('The desired language for the instructions (Swahili or English).')
});
export type AutoGenerateReceiptInstructionsInput = z.infer<typeof AutoGenerateReceiptInstructionsInputSchema>;

// Output Schema
const AutoGenerateReceiptInstructionsOutputSchema = z.object({
  instructions: z.string().describe('The generated receipt notes/instructions.')
});
export type AutoGenerateReceiptInstructionsOutput = z.infer<typeof AutoGenerateReceiptInstructionsOutputSchema>;

// Wrapper function to call the flow
export async function autoGenerateReceiptInstructions(
  input: AutoGenerateReceiptInstructionsInput
): Promise<AutoGenerateReceiptInstructionsOutput> {
  return autoGenerateReceiptInstructionsFlow(input);
}

// Define the prompt
const autoGenerateInstructionsPrompt = ai.definePrompt({
  name: 'autoGenerateReceiptInstructionsPrompt',
  input: { schema: AutoGenerateReceiptInstructionsInputSchema },
  output: { schema: AutoGenerateReceiptInstructionsOutputSchema },
  prompt: `You are an AI assistant that generates clear, concise, and customized receipt notes/instructions for visitors at a POS center.
The instructions should be relevant to the visitor type and POS center, always polite, and strictly in the specified language.

Visitor Type: {{{visitorType}}}
POS Center: {{{posCenterName}}}
Language: {{{language}}}

Generate helpful notes and instructions for the receipt. Include a thank you message and a suggestion for who to contact for further inquiries. If a phone number is needed, use '0777350786' as a contact number for more details.

Output ONLY the generated instructions, without any introductory or concluding remarks.`
});

// Define the flow
const autoGenerateReceiptInstructionsFlow = ai.defineFlow(
  {
    name: 'autoGenerateReceiptInstructionsFlow',
    inputSchema: AutoGenerateReceiptInstructionsInputSchema,
    outputSchema: AutoGenerateReceiptInstructionsOutputSchema,
  },
  async (input) => {
    const { output } = await autoGenerateInstructionsPrompt(input);
    return output!;
  }
);
