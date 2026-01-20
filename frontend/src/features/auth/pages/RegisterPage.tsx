import * as React from 'react';
import { useState } from 'react';
import { flushSync } from 'react-dom';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { Checkbox } from '@/shared/components/ui/checkbox';
import { Eye, EyeOff, X } from 'lucide-react';
import { AnimatedCharacters } from '@/shared/components/ui/animated-characters';
import { ResetPasswordForm } from '@/shared/components/ui/reset-password-form';
import { toast } from '@/shared/lib/toast';
import { cn } from '@/lib/utils';
import { motion, AnimatePresence } from 'framer-motion';

export function RegisterPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { register, login, isLoading } = useAuth();

  // Set navigation flag when register page mounts
  React.useEffect(() => {
    sessionStorage.setItem('xai-forge-navigated-to-hero', 'true');
  }, []);
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [organization, setOrganization] = useState('');
  const [role, setRole] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isTyping, setIsTyping] = useState(false);
  const [showLogin, setShowLogin] = useState(false);
  const [showForgotPassword, setShowForgotPassword] = useState(false);
  const [isTransitioningForward, setIsTransitioningForward] = useState(true);
  const transitionDirectionRef = React.useRef(true);
  // Login form state
  const [loginUsername, setLoginUsername] = useState('');
  const [loginPassword, setLoginPassword] = useState('');
  const [showLoginPassword, setShowLoginPassword] = useState(false);
  const [isLoginTyping, setIsLoginTyping] = useState(false);
  // Forgot password state
  const [forgotPasswordEmail, setForgotPasswordEmail] = useState('');
  const [isCheckingEmail, setIsCheckingEmail] = useState(false);
  const [emailError, setEmailError] = useState<string | null>(null);
  const [showResetForm, setShowResetForm] = useState(false);
  const [isSendingOtp, setIsSendingOtp] = useState(false);
  // Reset password form state for character animation
  const [resetPassword, setResetPassword] = useState('');
  const [showResetPassword, setShowResetPassword] = useState(false);
  const [isResetPasswordTyping, setIsResetPasswordTyping] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    register({ 
      username, 
      email, 
      password,
      firstName,
      lastName,
      organization: organization.trim() || undefined,
      role: role.trim() || undefined
    });
  };

  const handleLoginClick = (e: React.MouseEvent) => {
    e.preventDefault();
    transitionDirectionRef.current = false; // Moving backward: register -> login
    flushSync(() => {
      setIsTransitioningForward(false);
      transitionDirectionRef.current = false;
    });
    setShowLogin(true);
    window.history.replaceState(null, '', '/login');
  };

  const handleLoginSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    login({ username: loginUsername, password: loginPassword });
  };

  const handleBackToRegister = () => {
    transitionDirectionRef.current = true; // Moving forward: login -> register
    setIsTransitioningForward(true);
    setShowLogin(false);
    window.history.replaceState(null, '', '/register');
  };

  const handleForgotPasswordClick = (e: React.MouseEvent) => {
    e.preventDefault();
    transitionDirectionRef.current = true; // Moving forward: login -> forgot password
    setIsTransitioningForward(true);
    setShowForgotPassword(true);
    window.history.replaceState(null, '', '/forgot-password');
  };

  const handleCheckEmail = async (e: React.FormEvent) => {
    e.preventDefault();
    setEmailError(null);
    setIsCheckingEmail(true);

    try {
      // TODO: Replace with actual API call - see MOCKED_FEATURES.md
      // Mocked email validation - accepts any email for testing
      await new Promise(resolve => setTimeout(resolve, 1000)); // Simulate network delay
      
      // Mock: Always return true for any email
      const emailExists = true; // MOCKED - Replace with: await authApi.checkEmailExists(forgotPasswordEmail);
      console.log('[MOCKED] Checking email:', forgotPasswordEmail, 'Exists:', emailExists);
      
      if (emailExists) {
        // TODO: Replace with actual API call - see MOCKED_FEATURES.md
        // Mocked OTP sending
        setIsSendingOtp(true);
        await new Promise(resolve => setTimeout(resolve, 1000)); // Simulate network delay
        
        // Mock: OTP is always 123456 for testing
        const mockOtp = '123456';
        console.log('[MOCKED] OTP sent to', forgotPasswordEmail, '- Use OTP:', mockOtp);
        // Replace with: await authApi.sendPasswordResetOtp(forgotPasswordEmail);
        
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
      // TODO: Replace with actual API call - see MOCKED_FEATURES.md
      // Mocked OTP verification - accepts 123456 for testing
      await new Promise(resolve => setTimeout(resolve, 1000)); // Simulate network delay
      
      // Mock: Only accept OTP 123456
      const isValid = code === '123456'; // MOCKED - Replace with: await authApi.verifyPasswordResetOtp(forgotPasswordEmail, code);
      console.log('[MOCKED] Verifying OTP:', code, 'Valid:', isValid);
      
      if (isValid) {
        toast.success('Code verified successfully');
      } else {
        toast.error('Invalid verification code. Use 123456 for testing.');
      }
      return isValid;
    } catch (error: any) {
      const errorMessage = error?.message || 'Invalid verification code';
      toast.error(errorMessage);
      return false;
    }
  };

  const handleResetPassword = async (newPassword: string): Promise<void> => {
    try {
      // TODO: Replace with actual API call - see MOCKED_FEATURES.md
      // Mocked password reset
      await new Promise(resolve => setTimeout(resolve, 1000)); // Simulate network delay
      
      // Mock: Log the password reset
      console.log('[MOCKED] Resetting password for:', forgotPasswordEmail);
      console.log('[MOCKED] New password:', newPassword);
      // Replace with: await authApi.resetPassword(forgotPasswordEmail, newPassword);
      
      toast.success('Password reset successfully! Redirecting to login...');
      setTimeout(() => {
        setShowForgotPassword(false);
        setShowResetForm(false);
        setForgotPasswordEmail('');
        setEmailError(null);
        setResetPassword('');
        setShowResetPassword(false);
        setIsResetPasswordTyping(false);
        setShowLogin(true);
        window.history.replaceState(null, '', '/login');
      }, 1500);
    } catch (error: any) {
      const errorMessage = error?.message || 'Failed to reset password';
      toast.error(errorMessage);
      throw error;
    }
  };

  const handleCancelForgotPassword = () => {
    // Update direction state synchronously before triggering exit
    flushSync(() => {
      setIsTransitioningForward(false);
      transitionDirectionRef.current = false;
    });
    // Now trigger the exit animation
    setShowForgotPassword(false);
    setShowResetForm(false);
    setForgotPasswordEmail('');
    setEmailError(null);
    setResetPassword('');
    setShowResetPassword(false);
    setIsResetPasswordTyping(false);
    window.history.replaceState(null, '', '/login');
  };

  const handleResetPasswordChange = (password: string, showPassword: boolean) => {
    setResetPassword(password);
    setShowResetPassword(showPassword);
    setIsResetPasswordTyping(password.length > 0);
  };

  return (
    <div className="min-h-screen grid lg:grid-cols-2 relative">
      {/* Close Button with Circular Hover Region */}
      <div className="absolute top-6 right-6 z-50 group w-20 h-20 flex items-center justify-center">
        <button
          onClick={() => {
            // Ensure flag is set before navigating
            sessionStorage.setItem('xai-forge-navigated-to-hero', 'true');
            // Set flag to indicate closing (exit to right)
            sessionStorage.setItem('xai-forge-closing-to-hero', 'true');
            // Store the current pathname so we know which page we're closing from
            sessionStorage.setItem('xai-forge-closing-from-path', location.pathname);
            navigate('/');
          }}
          className="w-10 h-10 rounded-full bg-background/80 backdrop-blur-sm border border-border/60 group-hover:bg-background group-hover:border-border transition-all duration-300 flex items-center justify-center p-0"
          aria-label="Close"
        >
          <X className="size-5 text-foreground group-hover:text-red-500 group-hover:rotate-90 transition-all duration-300" />
        </button>
      </div>

      {/* Left Content Section with Characters */}
      <div className="relative hidden lg:flex flex-col justify-between bg-gradient-to-br from-primary/90 via-primary to-primary/80 p-12 text-primary-foreground">
        <div className="relative z-20">
          <div className="flex items-center gap-2 text-lg font-semibold">
            <Logo className="text-white" />
          </div>
        </div>

        <div className="relative z-20 flex items-end justify-center h-[500px]">
          <AnimatedCharacters 
            isTyping={
              showResetForm ? isResetPasswordTyping :
              showLogin ? isLoginTyping : 
              isTyping
            } 
            password={
              showResetForm ? resetPassword :
              showLogin ? loginPassword : 
              password
            } 
            showPassword={
              showResetForm ? showResetPassword :
              showLogin ? showLoginPassword : 
              showPassword
            }
          />
        </div>

        <div className="relative z-20 flex items-center gap-8 text-sm text-primary-foreground/60">
          <Link to="/privacy" className="hover:text-primary-foreground transition-colors">
            Privacy Policy
          </Link>
          <Link to="/terms" className="hover:text-primary-foreground transition-colors">
            Terms of Service
          </Link>
          <Link to="/contact" className="hover:text-primary-foreground transition-colors">
            Contact
          </Link>
        </div>

        {/* Decorative elements */}
        <div className="absolute inset-0 bg-grid-white/[0.05] bg-[size:20px_20px]" />
        <div className="absolute top-1/4 right-1/4 size-64 bg-primary-foreground/10 rounded-full blur-3xl" />
        <div className="absolute bottom-1/4 left-1/4 size-96 bg-primary-foreground/5 rounded-full blur-3xl" />
      </div>

      {/* Right Section - Register, Login, or Forgot Password */}
      <div className="flex items-center justify-center p-8 bg-background relative">
        <AnimatePresence mode="wait">
          {!showLogin && !showForgotPassword ? (
            <motion.div
              key="register-form"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: !isTransitioningForward ? 20 : -20 }}
              transition={{ duration: 0.3, ease: [0.22, 1, 0.36, 1] }}
              className="w-full max-w-[420px]"
            >
              {/* Mobile Logo */}
              <div className="lg:hidden flex items-center justify-center gap-2 text-lg font-semibold mb-12">
                <Logo className="text-black" />
              </div>

              {/* Header */}
              <div className="text-center mb-10">
                <h1 className="text-3xl font-bold tracking-tight mb-2">Create Account</h1>
                <p className="text-muted-foreground text-sm">Sign up to start using XAI-Forge</p>
              </div>

              {/* Register Form */}
              <form onSubmit={handleSubmit} className="space-y-5">
            <div className="space-y-2">
              <Label htmlFor="username" className="text-sm font-medium">Username</Label>
              <Input
                id="username"
                type="text"
                placeholder="Enter your username"
                value={username}
                autoComplete="off"
                onChange={(e) => setUsername(e.target.value)}
                onFocus={() => setIsTyping(true)}
                onBlur={() => setIsTyping(false)}
                required
                disabled={isLoading}
                className="h-12 bg-background border-border/60 focus:border-primary"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="firstName" className="text-sm font-medium">First Name</Label>
                <Input
                  id="firstName"
                  type="text"
                  placeholder="John"
                  value={firstName}
                  autoComplete="given-name"
                  onChange={(e) => setFirstName(e.target.value)}
                  onFocus={() => setIsTyping(true)}
                  onBlur={() => setIsTyping(false)}
                  required
                  disabled={isLoading}
                  className="h-12 bg-background border-border/60 focus:border-primary"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="lastName" className="text-sm font-medium">Last Name</Label>
                <Input
                  id="lastName"
                  type="text"
                  placeholder="Doe"
                  value={lastName}
                  autoComplete="family-name"
                  onChange={(e) => setLastName(e.target.value)}
                  onFocus={() => setIsTyping(true)}
                  onBlur={() => setIsTyping(false)}
                  required
                  disabled={isLoading}
                  className="h-12 bg-background border-border/60 focus:border-primary"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="email" className="text-sm font-medium">Email</Label>
              <Input
                id="email"
                type="email"
                placeholder="anna@gmail.com"
                value={email}
                autoComplete="email"
                onChange={(e) => setEmail(e.target.value)}
                onFocus={() => setIsTyping(true)}
                onBlur={() => setIsTyping(false)}
                required
                disabled={isLoading}
                className="h-12 bg-background border-border/60 focus:border-primary"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="organization" className="text-sm font-medium">
                  Company <span className="text-muted-foreground text-xs font-normal">(Optional)</span>
                </Label>
                <Input
                  id="organization"
                  type="text"
                  placeholder="Acme Corp"
                  value={organization}
                  autoComplete="organization"
                  onChange={(e) => setOrganization(e.target.value)}
                  onFocus={() => setIsTyping(true)}
                  onBlur={() => setIsTyping(false)}
                  disabled={isLoading}
                  className="h-12 bg-background border-border/60 focus:border-primary"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="role" className="text-sm font-medium">
                  Role <span className="text-muted-foreground text-xs font-normal">(Optional)</span>
                </Label>
                <Input
                  id="role"
                  type="text"
                  placeholder="Software Engineer"
                  value={role}
                  autoComplete="organization-title"
                  onChange={(e) => setRole(e.target.value)}
                  onFocus={() => setIsTyping(true)}
                  onBlur={() => setIsTyping(false)}
                  disabled={isLoading}
                  className="h-12 bg-background border-border/60 focus:border-primary"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="password" className="text-sm font-medium">Password</Label>
              <div className="relative">
              <Input
                id="password"
                  type={showPassword ? "text" : "password"}
                  placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                disabled={isLoading}
                  className="h-12 pr-10 bg-background border-border/60 focus:border-primary"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
                >
                  {showPassword ? (
                    <EyeOff className="size-5" />
                  ) : (
                    <Eye className="size-5" />
                  )}
                </button>
              </div>
            </div>

            <div className="flex items-center space-x-2">
              <Checkbox id="terms" required />
              <Label
                htmlFor="terms"
                className="text-sm font-normal cursor-pointer"
              >
                I agree to the Terms of Service and Privacy Policy
              </Label>
            </div>

            <Button 
              type="submit" 
              className="w-full h-12 text-base font-medium" 
              size="lg" 
              disabled={isLoading}
            >
              {isLoading ? "Creating account..." : "Sign up"}
            </Button>
          </form>

              {/* Login Link */}
              <div className="text-center text-sm text-muted-foreground mt-8">
                Already have an account?{" "}
                <button
                  onClick={handleLoginClick}
                  className="text-foreground font-medium hover:underline"
                >
                  Log in
                </button>
              </div>
            </motion.div>
          ) : showLogin && !showForgotPassword ? (
            <motion.div
              key="login-form"
              initial={{ opacity: 0, x: !isTransitioningForward ? -20 : 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: isTransitioningForward ? -20 : 20 }}
              transition={{ duration: 0.3, ease: [0.22, 1, 0.36, 1] }}
              className="w-full max-w-[420px]"
            >
              {/* Mobile Logo */}
              <div className="lg:hidden flex items-center justify-center gap-2 text-lg font-semibold mb-12">
                <Logo className="text-black" />
              </div>

              {/* Header */}
              <div className="text-center mb-10">
                <h1 className="text-3xl font-bold tracking-tight mb-2">Welcome back!</h1>
                <p className="text-muted-foreground text-sm">Please enter your details</p>
              </div>

              {/* Login Form */}
              <form onSubmit={handleLoginSubmit} className="space-y-5">
                <div className="space-y-2">
                  <Label htmlFor="login-username" className="text-sm font-medium">Username</Label>
                  <Input
                    id="login-username"
                    type="text"
                    placeholder="Enter your username"
                    value={loginUsername}
                    autoComplete="off"
                    onChange={(e) => setLoginUsername(e.target.value)}
                    onFocus={() => setIsLoginTyping(true)}
                    onBlur={() => setIsLoginTyping(false)}
                    required
                    disabled={isLoading}
                    className="h-12 bg-background border-border/60 focus:border-primary"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="login-password" className="text-sm font-medium">Password</Label>
                  <div className="relative">
                    <Input
                      id="login-password"
                      type={showLoginPassword ? "text" : "password"}
                      placeholder="••••••••"
                      value={loginPassword}
                      onChange={(e) => setLoginPassword(e.target.value)}
                      required
                      disabled={isLoading}
                      className="h-12 pr-10 bg-background border-border/60 focus:border-primary"
                    />
                    <button
                      type="button"
                      onClick={() => setShowLoginPassword(!showLoginPassword)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
                    >
                      {showLoginPassword ? (
                        <EyeOff className="size-5" />
                      ) : (
                        <Eye className="size-5" />
                      )}
                    </button>
                  </div>
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <Checkbox id="remember" />
                    <Label
                      htmlFor="remember"
                      className="text-sm font-normal cursor-pointer"
                    >
                      Remember for 30 days
                    </Label>
                  </div>
                  <button
                    type="button"
                    onClick={handleForgotPasswordClick}
                    className="text-sm text-primary hover:underline font-medium"
                  >
                    Forgot password?
                  </button>
                </div>

                <Button 
                  type="submit" 
                  className="w-full h-12 text-base font-medium" 
                  size="lg" 
                  disabled={isLoading}
                >
                  {isLoading ? "Signing in..." : "Log in"}
                </Button>
              </form>

              {/* Sign Up Link */}
              <div className="text-center text-sm text-muted-foreground mt-8">
                Don't have an account?{" "}
                <button
                  onClick={handleBackToRegister}
                  className="text-foreground font-medium hover:underline"
                >
                  Sign Up
                </button>
              </div>
            </motion.div>
          ) : (
            <motion.div
              key="forgot-password-form"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: !isTransitioningForward ? 20 : -20 }}
              transition={{ duration: 0.3, ease: [0.22, 1, 0.36, 1] }}
              className="w-full max-w-[420px]"
            >
              {/* Mobile Logo */}
              <div className="lg:hidden flex items-center justify-center gap-2 text-lg font-semibold mb-12">
                <Logo className="text-black" />
              </div>

              {!showResetForm ? (
                <>
                  {/* Header */}
                  <div className="text-center mb-10">
                    <h1 className="text-3xl font-bold tracking-tight mb-2">Forgot Password?</h1>
                    <p className="text-muted-foreground text-sm">
                      Enter your email address and we'll send you a verification code to reset your password.
                    </p>
                  </div>

                  {/* Forgot Password Form */}
                  <form onSubmit={handleCheckEmail} className="space-y-5">
                    <div className="space-y-2">
                      <Label htmlFor="email" className="text-sm font-medium">
                        Email Address
                      </Label>
                      <Input
                        id="email"
                        type="email"
                        placeholder="Enter your email"
                        value={forgotPasswordEmail}
                        onChange={(e) => {
                          setForgotPasswordEmail(e.target.value);
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

                  {/* Back to Login Link */}
                  <div className="text-center text-sm text-muted-foreground mt-8">
                    Remember your password?{" "}
                    <button
                      onClick={handleCancelForgotPassword}
                      className="text-foreground font-medium hover:underline"
                    >
                      Log in
                    </button>
                  </div>
                </>
              ) : (
                <ResetPasswordForm
                  email={forgotPasswordEmail}
                  onVerifyCode={handleVerifyCode}
                  onSubmit={handleResetPassword}
                  onCancel={handleCancelForgotPassword}
                  onPasswordChange={handleResetPasswordChange}
                />
              )}
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
}

const Logo = ({ className }: { className?: string }) => {
  const textColor = className?.includes('text-white') ? 'text-white' : className?.includes('text-black') ? 'text-black' : '';
  return (
    <div className={cn('flex items-center space-x-2', className)}>
      <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-primary to-secondary flex items-center justify-center">
        <span className="text-primary-foreground font-bold text-lg">X</span>
      </div>
      <span className={cn('text-xl font-bold', textColor)}>
        XAI-Forge
      </span>
    </div>
  );
}

