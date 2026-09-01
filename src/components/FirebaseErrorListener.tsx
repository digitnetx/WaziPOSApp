'use client';

import { useEffect } from 'react';
import { errorEmitter } from '@/firebase/error-emitter';
import { useToast } from '@/hooks/use-toast';

export const FirebaseErrorListener = () => {
  const { toast } = useToast();

  useEffect(() => {
    errorEmitter.on('permission-error', (error) => {
      toast({
        variant: "destructive",
        title: "Permission Denied",
        description: error.message || "You don't have permission to perform this action.",
      });
    });
  }, [toast]);

  return null;
};
