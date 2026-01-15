import * as React from 'react';
import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Button } from '@/shared/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/shared/components/ui/card';
import { CheckCircle2, XCircle, Loader2, Mail, AlertCircle } from 'lucide-react';
import { authApi } from '../api/authApi';
import { toast } from '@/shared/lib/toast';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';

export function VerifyEmailPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get('token');
  
  const [status, setStatus] = useState<'verifying' | 'success' | 'error' | 'resend'>('verifying');
  const [email, setEmail] = useState('');
  const [isResending, setIsResending] = useState(false);

  useEffect(() => {
    if (token) {
      verifyEmail(token);
    } else {
      setStatus('resend');
    }
  }, [token]);

  const verifyEmail = async (verificationToken: string) => {
    try {
      await authApi.verifyEmail(verificationToken);
      setStatus('success');
      toast.success('Email verified successfully!');
      setTimeout(() => {
        navigate('/login');
      }, 3000);
    } catch (error: any) {
      setStatus('error');
      toast.error(error?.response?.data?.error || 'Failed to verify email. The link may have expired.');
    }
  };

  const handleResend = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email) {
      toast.error('Please enter your email address');
      return;
    }

    setIsResending(true);
    try {
      await authApi.resendVerificationEmail(email);
      toast.success('Verification email sent! Please check your inbox.');
      setStatus('verifying');
    } catch (error: any) {
      toast.error(error?.response?.data?.error || 'Failed to resend verification email');
    } finally {
      setIsResending(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-8 bg-background">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
        className="w-full max-w-md"
      >
        <Card className="border-border/60 bg-card/50 backdrop-blur-sm">
          <CardHeader className="text-center space-y-4">
            {status === 'verifying' && (
              <>
                <div className="flex justify-center">
                  <Loader2 className="h-12 w-12 text-primary animate-spin" />
                </div>
                <CardTitle className="text-2xl">Verifying Email</CardTitle>
                <CardDescription>Please wait while we verify your email address...</CardDescription>
              </>
            )}
            
            {status === 'success' && (
              <>
                <motion.div
                  initial={{ scale: 0 }}
                  animate={{ scale: 1 }}
                  transition={{ type: 'spring', bounce: 0.5, duration: 0.6 }}
                  className="flex justify-center"
                >
                  <CheckCircle2 className="h-16 w-16 text-green-500" />
                </motion.div>
                <CardTitle className="text-2xl">Email Verified!</CardTitle>
                <CardDescription>
                  Your email has been successfully verified. Redirecting to login...
                </CardDescription>
              </>
            )}
            
            {status === 'error' && (
              <>
                <motion.div
                  initial={{ scale: 0 }}
                  animate={{ scale: 1 }}
                  transition={{ type: 'spring', bounce: 0.5, duration: 0.6 }}
                  className="flex justify-center"
                >
                  <XCircle className="h-16 w-16 text-red-500" />
                </motion.div>
                <CardTitle className="text-2xl">Verification Failed</CardTitle>
                <CardDescription>
                  The verification link is invalid or has expired. Please request a new verification email.
                </CardDescription>
              </>
            )}
            
            {status === 'resend' && (
              <>
                <div className="flex justify-center">
                  <Mail className="h-12 w-12 text-primary" />
                </div>
                <CardTitle className="text-2xl">Verify Your Email</CardTitle>
                <CardDescription>
                  Enter your email address to receive a new verification link.
                </CardDescription>
              </>
            )}
          </CardHeader>
          
          <CardContent>
            {(status === 'error' || status === 'resend') && (
              <form onSubmit={handleResend} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="email">Email Address</Label>
                  <Input
                    id="email"
                    type="email"
                    placeholder="your@email.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    disabled={isResending}
                  />
                </div>
                <Button
                  type="submit"
                  className="w-full"
                  disabled={isResending}
                >
                  {isResending ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Sending...
                    </>
                  ) : (
                    <>
                      <Mail className="mr-2 h-4 w-4" />
                      Resend Verification Email
                    </>
                  )}
                </Button>
              </form>
            )}
            
            {status === 'success' && (
              <div className="text-center space-y-4">
                <AlertCircle className="h-8 w-8 text-muted-foreground mx-auto" />
                <p className="text-sm text-muted-foreground">
                  You will be redirected to the login page in a few seconds.
                </p>
                <Button
                  variant="outline"
                  onClick={() => navigate('/login')}
                  className="w-full"
                >
                  Go to Login
                </Button>
              </div>
            )}
            
            {status !== 'verifying' && status !== 'success' && (
              <div className="text-center mt-4">
                <Button
                  variant="ghost"
                  onClick={() => navigate('/login')}
                  className="text-sm"
                >
                  Back to Login
                </Button>
              </div>
            )}
          </CardContent>
        </Card>
      </motion.div>
    </div>
  );
}
