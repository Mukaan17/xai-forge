import React, { useState } from 'react';
import { flushSync } from 'react-dom';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { Button } from '@/shared/components/ui/button';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import { Checkbox } from '@/shared/components/ui/checkbox';
import { Eye, EyeOff, X, AlertCircle } from 'lucide-react';
import { AnimatedCharacters } from '@/shared/components/ui/animated-characters';
import { ResetPasswordForm } from '@/shared/components/ui/reset-password-form';
import { authApi } from '../api/authApi';
import { toast } from '@/shared/lib/toast';
import { cn } from '@/lib/utils';
import { motion, AnimatePresence } from 'framer-motion';
import { useFormValidation, validationRules } from '@/shared/hooks/useFormValidation';
import { FormField } from '@/shared/components/ui/form-field';

export function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, register, isLoading } = useAuth();

  // Set navigation flag when login page mounts
  React.useEffect(() => {
    sessionStorage.setItem('xai-forge-navigated-to-hero', 'true');
  }, []);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isTyping, setIsTyping] = useState(false);
  const [showForgotPassword, setShowForgotPassword] = useState(false);
  const [showRegister, setShowRegister] = useState(false);
  const [isTransitioningForward, setIsTransitioningForward] = useState(true);
  const transitionDirectionRef = React.useRef(true);
  const [forgotPasswordEmail, setForgotPasswordEmail] = useState('');
  const [isCheckingEmail, setIsCheckingEmail] = useState(false);
  const [emailError, setEmailError] = useState<string | null>(null);
  const [showResetForm, setShowResetForm] = useState(false);
  const [isSendingOtp, setIsSendingOtp] = useState(false);
  // Reset password form state for character animation
  const [resetPassword, setResetPassword] = useState('');
  const [showResetPassword, setShowResetPassword] = useState(false);
  const [isResetPasswordTyping, setIsResetPasswordTyping] = useState(false);
  // Register form state
  const [registerUsername, setRegisterUsername] = useState('');
  const [registerEmail, setRegisterEmail] = useState('');
  const [registerPassword, setRegisterPassword] = useState('');
  const [registerFirstName, setRegisterFirstName] = useState('');
  const [registerLastName, setRegisterLastName] = useState('');
  const [registerOrganization, setRegisterOrganization] = useState('');
  const [registerRole, setRegisterRole] = useState('');
  const [showRegisterPassword, setShowRegisterPassword] = useState(false);
  const [isRegisterTyping, setIsRegisterTyping] = useState(false);

  // Form validation
  const loginValidation = useFormValidation({
    username: {
      required: true,
      rules: [validationRules.notEmpty],
    },
    password: {
      required: true,
      rules: [validationRules.notEmpty],
    },
  });

  const registerValidation = useFormValidation({
    registerUsername: {
      required: true,
      rules: [validationRules.username],
    },
    registerEmail: {
      required: true,
      rules: [validationRules.email],
    },
    registerPassword: {
      required: true,
      rules: [validationRules.password],
    },
  });

  // Initialize state based on URL
  React.useEffect(() => {
    if (location.pathname === '/forgot-password') {
      setShowForgotPassword(true);
    } else if (location.pathname === '/register') {
      setShowRegister(true);
    }
  }, [location.pathname]);

  // Auto-login for testing if URL has test=true parameter
  React.useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('test') === 'true') {
      // Auto-login for testing
      const testLogin = async () => {
        try {
          const response = await fetch('http://localhost:8080/api/v1/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: 'testuser', password: 'Test123!' })
          });
          const data = await response.json();
          localStorage.setItem('token', data.token);
          localStorage.setItem('auth-storage', JSON.stringify({
            state: { user: data.user, token: data.token, isAuthenticated: true },
            version: 0
          }));
          window.location.href = '/dashboard';
        } catch (error) {
          console.error('Auto-login failed:', error);
        }
      };
      testLogin();
    }
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const isValid = loginValidation.validateForm({ username, password });
    if (!isValid) return;
    login({ username, password });
  };

  const handleRegisterClick = (e: React.MouseEvent) => {
    e.preventDefault();
    transitionDirectionRef.current = true; // Moving forward: login -> register
    setIsTransitioningForward(true);
    setShowRegister(true);
    window.history.replaceState(null, '', '/register');
  };

  const handleRegisterSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const isValid = registerValidation.validateForm({
      registerUsername,
      registerEmail,
      registerPassword,
    });
    if (!isValid) return;
    register({ 
      username: registerUsername, 
      email: registerEmail, 
      password: registerPassword,
      firstName: registerFirstName,
      lastName: registerLastName,
      organization: registerOrganization.trim() || undefined,
      role: registerRole.trim() || undefined
    });
  };

  const handleBackToLogin = () => {
    // Update direction state synchronously before triggering exit
    flushSync(() => {
      setIsTransitioningForward(false);
      transitionDirectionRef.current = false;
    });
    // Now trigger the exit animation
    setShowRegister(false);
    window.history.replaceState(null, '', '/login');
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
        window.history.replaceState(null, '', '/login');
      }, 1500);
    } catch (error: any) {
      const errorMessage = error?.message || 'Failed to reset password';
      toast.error(errorMessage);
      throw error;
    }
  };

  const handleResetPasswordChange = (password: string, showPassword: boolean) => {
    setResetPassword(password);
    setShowResetPassword(showPassword);
    setIsResetPasswordTyping(password.length > 0);
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
              showRegister ? isRegisterTyping : 
              isTyping
            } 
            password={
              showResetForm ? resetPassword :
              showRegister ? registerPassword : 
              password
            } 
            showPassword={
              showResetForm ? showResetPassword :
              showRegister ? showRegisterPassword : 
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

      {/* Right Section - Login, Register, or Forgot Password */}
      <div className="flex items-center justify-center p-8 bg-background relative">
        <AnimatePresence mode="wait">
          {!showForgotPassword && !showRegister ? (
            <motion.div
              key="login-form"
              initial={{ opacity: 0, x: !isTransitioningForward ? -20 : 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
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
              <form onSubmit={handleSubmit} className="space-y-5">
                <div className="space-y-2">
                  <Label htmlFor="username" className={cn("text-sm font-medium", loginValidation.errors.username && "text-destructive")}>
                    Username
                  </Label>
                  <Input
                    id="username"
                    type="text"
                    placeholder="Enter your username"
                    value={username}
                    autoComplete="off"
                    onChange={(e) => {
                      setUsername(e.target.value);
                      loginValidation.validateField('username', e.target.value);
                    }}
                    onFocus={() => setIsTyping(true)}
                    onBlur={() => {
                      setIsTyping(false);
                      loginValidation.validateField('username', username);
                    }}
                    required
                    disabled={isLoading}
                    className={cn(
                      "h-12 bg-background border-border/60 focus:border-primary",
                      loginValidation.errors.username && "border-destructive focus-visible:ring-destructive"
                    )}
                    aria-invalid={!!loginValidation.errors.username}
                    aria-describedby={loginValidation.errors.username ? "username-error" : undefined}
                  />
                  {loginValidation.errors.username && (
                    <div id="username-error" className="flex items-center gap-1.5 text-sm text-destructive" role="alert">
                      <AlertCircle className="w-4 h-4" />
                      <span>{loginValidation.errors.username}</span>
                    </div>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="password" className={cn("text-sm font-medium", loginValidation.errors.password && "text-destructive")}>
                    Password
                  </Label>
                  <div className="relative">
                    <Input
                      id="password"
                      type={showPassword ? "text" : "password"}
                      placeholder="••••••••"
                      value={password}
                      onChange={(e) => {
                        setPassword(e.target.value);
                        loginValidation.validateField('password', e.target.value);
                      }}
                      onBlur={() => loginValidation.validateField('password', password)}
                      required
                      disabled={isLoading}
                      className={cn(
                        "h-12 pr-10 bg-background border-border/60 focus:border-primary",
                        loginValidation.errors.password && "border-destructive focus-visible:ring-destructive"
                      )}
                      aria-invalid={!!loginValidation.errors.password}
                      aria-describedby={loginValidation.errors.password ? "password-error" : undefined}
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
                  {loginValidation.errors.password && (
                    <div id="password-error" className="flex items-center gap-1.5 text-sm text-destructive" role="alert">
                      <AlertCircle className="w-4 h-4" />
                      <span>{loginValidation.errors.password}</span>
                    </div>
                  )}
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
                  onClick={handleRegisterClick}
                  className="text-foreground font-medium hover:underline"
                >
                  Sign Up
                </button>
              </div>
            </motion.div>
          ) : showRegister ? (
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
              <form onSubmit={handleRegisterSubmit} className="space-y-5">
                <div className="space-y-2">
                  <Label htmlFor="register-username" className="text-sm font-medium">Username</Label>
                  <Input
                    id="register-username"
                    type="text"
                    placeholder="Enter your username"
                    value={registerUsername}
                    autoComplete="off"
                    onChange={(e) => setRegisterUsername(e.target.value)}
                    onFocus={() => setIsRegisterTyping(true)}
                    onBlur={() => setIsRegisterTyping(false)}
                    required
                    disabled={isLoading}
                    className="h-12 bg-background border-border/60 focus:border-primary"
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="register-firstName" className="text-sm font-medium">First Name</Label>
                    <Input
                      id="register-firstName"
                      type="text"
                      placeholder="John"
                      value={registerFirstName}
                      autoComplete="given-name"
                      onChange={(e) => setRegisterFirstName(e.target.value)}
                      onFocus={() => setIsRegisterTyping(true)}
                      onBlur={() => setIsRegisterTyping(false)}
                      required
                      disabled={isLoading}
                      className="h-12 bg-background border-border/60 focus:border-primary"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="register-lastName" className="text-sm font-medium">Last Name</Label>
                    <Input
                      id="register-lastName"
                      type="text"
                      placeholder="Doe"
                      value={registerLastName}
                      autoComplete="family-name"
                      onChange={(e) => setRegisterLastName(e.target.value)}
                      onFocus={() => setIsRegisterTyping(true)}
                      onBlur={() => setIsRegisterTyping(false)}
                      required
                      disabled={isLoading}
                      className="h-12 bg-background border-border/60 focus:border-primary"
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="register-email" className="text-sm font-medium">Email</Label>
                  <Input
                    id="register-email"
                    type="email"
                    placeholder="anna@gmail.com"
                    value={registerEmail}
                    autoComplete="email"
                    onChange={(e) => setRegisterEmail(e.target.value)}
                    onFocus={() => setIsRegisterTyping(true)}
                    onBlur={() => setIsRegisterTyping(false)}
                    required
                    disabled={isLoading}
                    className="h-12 bg-background border-border/60 focus:border-primary"
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="register-organization" className="text-sm font-medium">
                      Company <span className="text-muted-foreground text-xs font-normal">(Optional)</span>
                    </Label>
                    <Input
                      id="register-organization"
                      type="text"
                      placeholder="Acme Corp"
                      value={registerOrganization}
                      autoComplete="organization"
                      onChange={(e) => setRegisterOrganization(e.target.value)}
                      onFocus={() => setIsRegisterTyping(true)}
                      onBlur={() => setIsRegisterTyping(false)}
                      disabled={isLoading}
                      className="h-12 bg-background border-border/60 focus:border-primary"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="register-role" className="text-sm font-medium">
                      Role <span className="text-muted-foreground text-xs font-normal">(Optional)</span>
                    </Label>
                    <Input
                      id="register-role"
                      type="text"
                      placeholder="Software Engineer"
                      value={registerRole}
                      autoComplete="organization-title"
                      onChange={(e) => setRegisterRole(e.target.value)}
                      onFocus={() => setIsRegisterTyping(true)}
                      onBlur={() => setIsRegisterTyping(false)}
                      disabled={isLoading}
                      className="h-12 bg-background border-border/60 focus:border-primary"
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="register-password" className="text-sm font-medium">Password</Label>
                  <div className="relative">
                    <Input
                      id="register-password"
                      type={showRegisterPassword ? "text" : "password"}
                      placeholder="••••••••"
                      value={registerPassword}
                      onChange={(e) => setRegisterPassword(e.target.value)}
                      required
                      disabled={isLoading}
                      className="h-12 pr-10 bg-background border-border/60 focus:border-primary"
                    />
                    <button
                      type="button"
                      onClick={() => setShowRegisterPassword(!showRegisterPassword)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
                    >
                      {showRegisterPassword ? (
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
                  onClick={handleBackToLogin}
                  className="text-foreground font-medium hover:underline"
                >
                  Log in
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
                      <Label htmlFor="email" className="text-sm font-medium">Email Address</Label>
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
                      {isCheckingEmail || isSendingOtp ? "Sending..." : "Send Verification Code"}
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

