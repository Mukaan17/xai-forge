import * as React from 'react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { X } from 'lucide-react';
import { ResetPasswordForm } from '@/shared/components/ui/reset-password-form';
import { authApi } from '../api/authApi';
import { toast } from '@/shared/lib/toast';
import { cn } from '@/lib/utils';

export function ForgotPasswordPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [isCheckingEmail, setIsCheckingEmail] = useState(false);
  const [emailError, setEmailError] = useState<string | null>(null);
  const [showResetForm, setShowResetForm] = useState(false);
  const [isSendingOtp, setIsSendingOtp] = useState(false);

  const handleCheckEmail = async (e: React.FormEvent) => {
    e.preventDefault();
    setEmailError(null);
    setIsCheckingEmail(true);

    try {
      // Check if email exists
      const emailExists = await authApi.checkEmailExists(email);
      
      if (emailExists) {
        // Send OTP
        setIsSendingOtp(true);
        await authApi.sendPasswordResetOtp(email);
        setShowResetForm(true);
        toast.success('Verification code sent to your email');
      } else {
        setEmailError('No account found with this email address');
      }
    } catch (error: any) {
      const errorMessage = error?.message || 'Failed to process request. Please try again.';
      setEmailError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setIsCheckingEmail(false);
      setIsSendingOtp(false);
    }
  };

  const handleVerifyCode = async (code: string): Promise<boolean> => {
    try {
      const isValid = await authApi.verifyPasswordResetOtp(email, code);
      if (isValid) {
        toast.success('Code verified successfully');
      }
      return isValid;
    } catch (error: any) {
      const errorMessage = error?.message || 'Invalid verification code';
      toast.error(errorMessage);
      return false;
    }
  };

  const handleResetPassword = async (password: string): Promise<void> => {
    try {
      await authApi.resetPassword(email, password);
      toast.success('Password reset successfully! Redirecting to login...');
      setTimeout(() => {
        navigate('/login');
      }, 1500);
    } catch (error: any) {
      const errorMessage = error?.message || 'Failed to reset password';
      toast.error(errorMessage);
      throw error;
    }
  };

  const handleCancel = () => {
    navigate('/login');
  };

  if (showResetForm) {
    return (
      <div className="min-h-screen flex items-center justify-center p-8 bg-background relative">
        {/* Close Button */}
        <button
          onClick={handleCancel}
          className="absolute top-6 right-6 z-50 p-2 rounded-full bg-background/80 backdrop-blur-sm border border-border/60 hover:bg-background hover:border-border transition-all duration-300"
          aria-label="Close"
        >
          <X className="size-5 text-foreground hover:text-red-500 hover:rotate-90 transition-all duration-300" />
        </button>

        <ResetPasswordForm
          email={email}
          onVerifyCode={handleVerifyCode}
          onSubmit={handleResetPassword}
          onCancel={handleCancel}
        />
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-8 bg-background relative">
      {/* Close Button */}
      <button
        onClick={() => navigate('/login')}
        className="absolute top-6 right-6 z-50 p-2 rounded-full bg-background/80 backdrop-blur-sm border border-border/60 hover:bg-background hover:border-border transition-all duration-300"
        aria-label="Close"
      >
        <X className="size-5 text-foreground hover:text-red-500 hover:rotate-90 transition-all duration-300" />
      </button>

      <div className="w-full max-w-md">
        <div className="rounded-lg border bg-card p-8 text-card-foreground shadow-sm">
          <h1 className="mb-2 text-2xl font-semibold">Forgot Password?</h1>
          <p className="mb-6 text-sm text-muted-foreground">
            Enter your email address and we'll send you a verification code to reset your password.
          </p>

          <form onSubmit={handleCheckEmail} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email" className="text-sm font-medium">
                Email Address
              </Label>
              <Input
                id="email"
                type="email"
                placeholder="Enter your email"
                value={email}
                onChange={(e) => {
                  setEmail(e.target.value);
                  setEmailError(null);
                }}
                required
                disabled={isCheckingEmail || isSendingOtp}
                className={cn(
                  "h-12 bg-background border-border/60 focus:border-primary",
                  emailError && "border-destructive focus-visible:ring-destructive/20"
                )}
                aria-invalid={!!emailError}
              />
              {emailError && (
                <p className="text-sm text-destructive">{emailError}</p>
              )}
            </div>

            <Button
              type="submit"
              className="w-full h-12 text-base font-medium"
              size="lg"
              disabled={isCheckingEmail || isSendingOtp}
            >
              {isCheckingEmail || isSendingOtp
                ? 'Processing...'
                : 'Send Verification Code'}
            </Button>
          </form>

          <div className="mt-6 text-center text-sm text-muted-foreground">
            Remember your password?{' '}
            <button
              onClick={() => navigate('/login')}
              className="text-foreground font-medium hover:underline"
            >
              Log in
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
