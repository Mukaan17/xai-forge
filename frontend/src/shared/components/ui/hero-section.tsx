import * as React from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight, Menu, X } from 'lucide-react'
import { Button } from '@/shared/components/ui/button'
import { AnimatedGroup } from '@/shared/components/ui/animated-group'
import { cn } from '@/lib/utils'
import LoadingScreen from '@/shared/components/ui/loading-screen'
import { motion, AnimatePresence } from 'framer-motion'
import { FeaturesSection } from '@/shared/components/ui/features-section'
import AboutSection2 from '@/shared/components/ui/about-section-2'
import { SocialConnect } from '@/shared/components/ui/connect-with-us'
import AboutSection1 from '@/shared/components/ui/about-section-1'

const transitionVariants = {
    item: {
        hidden: {
            opacity: 0,
            filter: 'blur(12px)',
            y: 12,
        },
        visible: {
            opacity: 1,
            filter: 'blur(0px)',
            y: 0,
            transition: {
                type: 'spring',
                bounce: 0.3,
                duration: 1.5,
            },
        },
    },
}

export function HeroSection() {
    // Check navigation flag synchronously on mount to avoid race conditions
    const navigatedFromApp = typeof window !== 'undefined' ? sessionStorage.getItem('xai-forge-navigated-to-hero') : null;
    
    // Determine if we need hero animation synchronously (before first render)
    const needsHeroAnimation = !navigatedFromApp; // Only animate when coming from loading screen
    
    const [showLoading, setShowLoading] = React.useState(() => {
        // Initialize state based on navigation flag
        return !navigatedFromApp;
    });
    const [fadeOut, setFadeOut] = React.useState(false);
    const [heroFadeIn, setHeroFadeIn] = React.useState(() => {
        // If navigated from app, hero should be visible immediately (App.tsx handles animation)
        return !!navigatedFromApp;
    });
    
    // Extract hero content to avoid duplication
    const heroContent = (
        <>
            <HeroHeader />
            <main id="home" className="overflow-hidden">
                <div
                    aria-hidden
                    className="z-[2] absolute inset-0 pointer-events-none isolate opacity-50 contain-strict hidden lg:block">
                    <div className="w-[35rem] h-[80rem] -translate-y-[350px] absolute left-0 top-0 -rotate-45 rounded-full bg-[radial-gradient(68.54%_68.72%_at_55.02%_31.46%,hsla(0,0%,85%,.08)_0,hsla(0,0%,55%,.02)_50%,hsla(0,0%,45%,0)_80%)]" />
                    <div className="h-[80rem] absolute left-0 top-0 w-56 -rotate-45 rounded-full bg-[radial-gradient(50%_50%_at_50%_50%,hsla(0,0%,85%,.06)_0,hsla(0,0%,45%,.02)_80%,transparent_100%)] [translate:5%_-50%]" />
                    <div className="h-[80rem] -translate-y-[350px] absolute left-0 top-0 w-56 -rotate-45 bg-[radial-gradient(50%_50%_at_50%_50%,hsla(0,0%,85%,.04)_0,hsla(0,0%,45%,.02)_80%,transparent_100%)]" />
                </div>
                <section>
                    <div className="relative pt-24 md:pt-36">
                        <AnimatedGroup
                            variants={{
                                container: {
                                    visible: {
                                        transition: {
                                            delayChildren: 1,
                                        },
                                    },
                                },
                                item: {
                                    hidden: {
                                        opacity: 0,
                                        y: 20,
                                    },
                                    visible: {
                                        opacity: 1,
                                        y: 0,
                                        transition: {
                                            type: 'spring',
                                            bounce: 0.3,
                                            duration: 2,
                                        },
                                    },
                                },
                            }}
                            className="absolute inset-0 -z-20">
                            <img
                                src="https://images.unsplash.com/photo-1555949963-aa79dcee981c?w=3276&h=4095&fit=crop&q=80"
                                alt="background"
                                className="absolute inset-x-0 top-56 -z-20 hidden lg:top-32 lg:block opacity-20"
                                width="3276"
                                height="4095"
                            />
                        </AnimatedGroup>
                        <div aria-hidden className="absolute inset-0 -z-10 size-full [background:radial-gradient(125%_125%_at_50%_100%,transparent_0%,var(--color-background)_75%)]" />
                        <div className="mx-auto max-w-7xl px-6">
                            <div className="text-center sm:mx-auto lg:mr-auto lg:mt-0">
                                <AnimatedGroup variants={transitionVariants}>
                                    <Link
                                        to="/login"
                                        className="hover:bg-muted group mx-auto flex w-fit items-center gap-4 rounded-full border border-border bg-card p-1 pl-4 shadow-md shadow-black/5 transition-all duration-300">
                                        <span className="text-foreground text-sm">Introducing AI-Powered ML Operations</span>
                                        <span className="block h-4 w-0.5 border-l border-border bg-muted"></span>

                                        <div className="bg-background group-hover:bg-muted size-6 overflow-hidden rounded-full duration-500">
                                            <div className="flex w-12 -translate-x-1/2 duration-500 ease-in-out group-hover:translate-x-0">
                                                <span className="flex size-6">
                                                    <ArrowRight className="m-auto size-3" />
                                                </span>
                                                <span className="flex size-6">
                                                    <ArrowRight className="m-auto size-3" />
                                                </span>
                                            </div>
                                        </div>
                                    </Link>
                        
                                    <h1
                                        className="mt-8 max-w-4xl mx-auto text-balance text-6xl md:text-7xl lg:mt-16 xl:text-[5.25rem] font-bold bg-gradient-to-r from-primary via-secondary to-primary bg-clip-text text-transparent">
                                        Modern ML Operations Platform
                                    </h1>
                                    <p
                                        className="mx-auto mt-8 max-w-2xl text-balance text-lg text-muted-foreground">
                                        Build, train, and deploy machine learning models with ease. XAI-Forge provides everything you need to manage your ML workflow from datasets to predictions.
                                    </p>
                                </AnimatedGroup>

                                <AnimatedGroup
                                    variants={{
                                        container: {
                                            visible: {
                                                transition: {
                                                    staggerChildren: 0.05,
                                                    delayChildren: 0.75,
                                                },
                                            },
                                        },
                                        ...transitionVariants,
                                    }}
                                    className="mt-12 flex flex-col items-center justify-center gap-2 md:flex-row">
                                    <div
                                        key={1}
                                        className="bg-primary/10 rounded-[14px] border border-primary/20 p-0.5">
                                        <Button
                                            asChild
                                            size="lg"
                                            className="rounded-xl px-5 text-base bg-primary text-primary-foreground hover:bg-primary/90">
                                            <Link to="/login">
                                                <span className="text-nowrap">Get Started</span>
                                            </Link>
                                        </Button>
                                    </div>
                                    <Button
                                        key={2}
                                        asChild
                                        size="lg"
                                        variant="ghost"
                                        className="rounded-xl px-5">
                                        <Link to="/register">
                                            <span className="text-nowrap">Sign Up</span>
                                        </Link>
                                    </Button>
                                </AnimatedGroup>
                            </div>
                        </div>

                        <AnimatedGroup
                            variants={{
                                container: {
                                    visible: {
                                        transition: {
                                            staggerChildren: 0.05,
                                            delayChildren: 0.75,
                                        },
                                    },
                                },
                                ...transitionVariants,
                            }}>
                            <div className="relative -mr-56 mt-8 overflow-hidden px-2 sm:mr-0 sm:mt-12 md:mt-20">
                                <div
                                    aria-hidden
                                    className="bg-gradient-to-b to-background absolute inset-0 z-10 from-transparent from-35%"
                                />
                                <div className="inset-shadow-2xs ring-background dark:inset-shadow-white/20 bg-background relative mx-auto max-w-6xl overflow-hidden rounded-2xl shadow-lg shadow-zinc-950/15 ring-1">
                                    <img
                                        className="bg-background aspect-15/8 relative rounded-2xl"
                                        src="https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=2700&h=1440&fit=crop&q=80"
                                        alt="XAI-Forge Dashboard"
                                        width="2700"
                                        height="1440"
                                    />
                                </div>
                            </div>
                        </AnimatedGroup>
                    </div>
                </section>
                <AboutSection2 />
                <FeaturesSection />
                <AboutSection1 />
                <SocialConnect />
                <HeroFooter />
            </main>
        </>
    );

    React.useEffect(() => {
        // Hide scrollbar on hero page - apply to both html and body
        document.documentElement.classList.add('hide-scrollbar');
        document.body.classList.add('hide-scrollbar');
        
        // Clear the navigation flag after checking
        if (navigatedFromApp) {
            sessionStorage.removeItem('xai-forge-navigated-to-hero');
            // Navigated from another page - App.tsx handles animation, hero is already visible
            // No need to do anything, heroFadeIn is already true from initial state
        } else {
            // Page reload or initial load - show loading screen with same sequence
            // Timing: phrase1 (~1s) + delay (1.2s) + phrase2 (~1s) + delay (1.2s) + phrase3 (~0.6s) + extra display time (1.5s) = ~7.5s
            // Extended display time for "Made Easy" so users can see it longer for better UX
            const timer = setTimeout(() => {
                setFadeOut(true)
                // Start hero animation as loading screen starts fading out
                // This creates a smooth overlap where hero animates as loading fades
                setHeroFadeIn(true)
                setTimeout(() => {
                    setShowLoading(false)
                }, 1500) // Wait for fade-out transition to complete
            }, 7500) // Extended from 5800ms to 7500ms to show "Made Easy" longer

            return () => {
                clearTimeout(timer)
                // Remove scrollbar hide class when component unmounts
                document.documentElement.classList.remove('hide-scrollbar');
                document.body.classList.remove('hide-scrollbar');
            }
        }
        
        return () => {
            // Remove scrollbar hide class when component unmounts
            document.documentElement.classList.remove('hide-scrollbar');
            document.body.classList.remove('hide-scrollbar');
        }
    }, [navigatedFromApp])

    return (
        <>
            {showLoading && (
                <div className={`fixed inset-0 z-50 transition-opacity duration-1500 ${fadeOut ? 'opacity-0 pointer-events-none' : 'opacity-100'}`}>
                    <LoadingScreen />
                </div>
            )}
            <AnimatePresence mode="wait">
                {heroFadeIn && (
                    <motion.div
                        key="hero-content"
                        initial={{ opacity: 0, x: 100 }}
                        animate={{ opacity: 1, x: 0 }}
                        exit={{ opacity: 0, x: 100 }}
                        transition={{ duration: 0.4, ease: [0.22, 1, 0.36, 1] }}
                        style={{ pointerEvents: showLoading ? 'none' : 'auto' }}
                    >
                        {heroContent}
                    </motion.div>
                )}
            </AnimatePresence>
        </>
    )
}

const HeroFooter = () => {
    return (
        <footer className="border-t border-border bg-background">
            <div className="mx-auto max-w-7xl px-6 py-12 md:py-16">
                <div className="grid grid-cols-1 gap-8 md:grid-cols-4">
                    <div className="space-y-4">
                        <div className="flex items-center space-x-2">
                            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-primary to-secondary flex items-center justify-center">
                                <span className="text-primary-foreground font-bold text-lg">X</span>
                            </div>
                            <span className="text-xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
                                XAI-Forge
                            </span>
                        </div>
                        <p className="text-sm text-muted-foreground max-w-xs">
                            Modern ML Operations Platform for building, training, and deploying machine learning models with ease.
                        </p>
                    </div>
                    
                    <div className="space-y-4">
                        <h3 className="text-sm font-semibold">Product</h3>
                        <ul className="space-y-3 text-sm">
                            <li>
                                <a
                                    href="#features"
                                    onClick={(e) => {
                                        e.preventDefault();
                                        const element = document.querySelector('#features');
                                        if (element) {
                                            element.scrollIntoView({ behavior: 'smooth', block: 'start' });
                                        }
                                    }}
                                    className="text-muted-foreground hover:text-foreground transition-colors cursor-pointer">
                                    Features
                                </a>
                            </li>
                            <li>
                                <Link to="#solutions" className="text-muted-foreground hover:text-foreground transition-colors">
                                    Solutions
                                </Link>
                            </li>
                            <li>
                                <Link to="/dashboard" className="text-muted-foreground hover:text-foreground transition-colors">
                                    Dashboard
                                </Link>
                            </li>
                            <li>
                                <Link to="/models" className="text-muted-foreground hover:text-foreground transition-colors">
                                    Models
                                </Link>
                            </li>
                        </ul>
                    </div>
                    
                    <div className="space-y-4">
                        <h3 className="text-sm font-semibold">Resources</h3>
                        <ul className="space-y-3 text-sm">
                            <li>
                                <Link to="#about" className="text-muted-foreground hover:text-foreground transition-colors">
                                    About
                                </Link>
                            </li>
                            <li>
                                <Link to="/docs" className="text-muted-foreground hover:text-foreground transition-colors">
                                    Documentation
                                </Link>
                            </li>
                            <li>
                                <Link to="/support" className="text-muted-foreground hover:text-foreground transition-colors">
                                    Support
                                </Link>
                            </li>
                            <li>
                                <Link to="/contact" className="text-muted-foreground hover:text-foreground transition-colors">
                                    Contact
                                </Link>
                            </li>
                        </ul>
                    </div>
                    
                    <div className="space-y-4">
                        <h3 className="text-sm font-semibold">Get Started</h3>
                        <ul className="space-y-3 text-sm">
                            <li>
                                <Link to="/register" className="text-muted-foreground hover:text-foreground transition-colors">
                                    Sign Up
                                </Link>
                            </li>
                            <li>
                                <Link to="/login" className="text-muted-foreground hover:text-foreground transition-colors">
                                    Login
                                </Link>
                            </li>
                            <li>
                                <Link to="/pricing" className="text-muted-foreground hover:text-foreground transition-colors">
                                    Pricing
                                </Link>
                            </li>
                        </ul>
                    </div>
                </div>
                
                <div className="mt-12 border-t border-border pt-8">
                    <div className="flex flex-col items-center justify-between gap-4 md:flex-row">
                        <p className="text-sm text-muted-foreground">
                            © {new Date().getFullYear()} XAI-Forge. All rights reserved.
                        </p>
                        <div className="flex items-center gap-6">
                            <Link to="/privacy" className="text-sm text-muted-foreground hover:text-foreground transition-colors">
                                Privacy Policy
                            </Link>
                            <Link to="/terms" className="text-sm text-muted-foreground hover:text-foreground transition-colors">
                                Terms of Service
                            </Link>
                        </div>
                    </div>
                </div>
            </div>
        </footer>
    )
}

const menuItems = [
    { name: 'Home', href: '#home' },
    { name: 'Features', href: '#features' },
    { name: 'About', href: '#about' },
    { name: 'Contact', href: '#contact' },
]

const HeroHeader = () => {
    const [menuState, setMenuState] = React.useState(false)
    const [isScrolled, setIsScrolled] = React.useState(false)

    React.useEffect(() => {
        const handleScroll = () => {
            setIsScrolled(window.scrollY > 50)
        }
        window.addEventListener('scroll', handleScroll)
        return () => window.removeEventListener('scroll', handleScroll)
    }, [])
    return (
        <header className="pointer-events-auto">
            <nav
                data-state={menuState && 'active'}
                className="fixed z-20 w-full px-2 group pointer-events-auto">
                <div className={cn('mx-auto mt-2 max-w-6xl px-6 transition-all duration-300 lg:px-12', isScrolled && 'bg-background/50 max-w-4xl rounded-2xl border backdrop-blur-lg lg:px-5')}>
                    <div className="relative flex flex-wrap items-center justify-between gap-6 py-3 lg:gap-0 lg:py-4">
                        <div className="flex w-full justify-between lg:w-auto">
                            <Link
                                to="/"
                                aria-label="home"
                                className="flex items-center space-x-2">
                                <Logo />
                            </Link>

                            <button
                                onClick={() => setMenuState(!menuState)}
                                aria-label={menuState == true ? 'Close Menu' : 'Open Menu'}
                                className="relative z-20 -m-2.5 -mr-4 block cursor-pointer p-2.5 lg:hidden">
                                <Menu className="in-data-[state=active]:rotate-180 group-data-[state=active]:scale-0 group-data-[state=active]:opacity-0 m-auto size-6 duration-200" />
                                <X className="group-data-[state=active]:rotate-0 group-data-[state=active]:scale-100 group-data-[state=active]:opacity-100 absolute inset-0 m-auto size-6 -rotate-180 scale-0 opacity-0 duration-200" />
                            </button>
                        </div>

                        <div className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 hidden lg:block">
                            <ul className="flex gap-8 text-sm">
                                {menuItems.map((item, index) => (
                                    <li key={index} className="flex items-center">
                                        <a
                                            href={item.href}
                                            onClick={(e) => {
                                                e.preventDefault();
                                                if (item.href === '#home') {
                                                    // Scroll to top of page
                                                    window.scrollTo({ top: 0, behavior: 'smooth' });
                                                } else {
                                                    const element = document.querySelector(item.href);
                                                    if (element) {
                                                        element.scrollIntoView({ behavior: 'smooth', block: 'start' });
                                                    }
                                                }
                                            }}
                                            className="text-muted-foreground hover:text-accent-foreground flex items-center duration-150 cursor-pointer">
                                            <span>{item.name}</span>
                                        </a>
                                    </li>
                                ))}
                            </ul>
                        </div>

                        <div className="bg-background group-data-[state=active]:block lg:group-data-[state=active]:flex mb-6 hidden w-full flex-wrap items-center justify-end space-y-8 rounded-3xl border p-6 shadow-2xl shadow-zinc-300/20 md:flex-nowrap lg:m-0 lg:flex lg:w-fit lg:gap-6 lg:space-y-0 lg:border-0 lg:bg-transparent lg:p-0 lg:shadow-none dark:shadow-none dark:lg:bg-transparent">
                            <div className="lg:hidden">
                                <ul className="space-y-6 text-base">
                                    {menuItems.map((item, index) => (
                                        <li key={index}>
                                            <a
                                                href={item.href}
                                                onClick={(e) => {
                                                    e.preventDefault();
                                                    setMenuState(false); // Close mobile menu
                                                    if (item.href === '#home') {
                                                        // Scroll to top of page
                                                        window.scrollTo({ top: 0, behavior: 'smooth' });
                                                    } else {
                                                        const element = document.querySelector(item.href);
                                                        if (element) {
                                                            element.scrollIntoView({ behavior: 'smooth', block: 'start' });
                                                        }
                                                    }
                                                }}
                                                className="text-muted-foreground hover:text-accent-foreground block duration-150 cursor-pointer">
                                                <span>{item.name}</span>
                                            </a>
                                        </li>
                                    ))}
                                </ul>
                            </div>
                            <div className="flex w-full flex-col space-y-3 sm:flex-row sm:gap-3 sm:space-y-0 md:w-fit">
                                <div 
                                    className={cn(
                                        isScrolled && 'lg:hidden',
                                        'relative overflow-hidden rounded-md inline-block [&:hover_.login-fill]:translate-x-0'
                                    )}
                                >
                                    <div 
                                        className="absolute inset-0 bg-emerald-100 translate-x-full transition-transform duration-300 ease-out rounded-md login-fill pointer-events-none"
                                    />
                                    <Button
                                        asChild
                                        variant="outline"
                                        size="sm"
                                        className="h-8 relative z-10 hover:border-emerald-200 hover:!bg-transparent transition-colors duration-200 bg-transparent text-foreground [&:hover_span]:text-black pointer-events-auto">
                                        <Link to="/login" className="pointer-events-auto">
                                            <span className="relative z-10 transition-colors duration-200">Login</span>
                                        </Link>
                                    </Button>
                                </div>
                                <Button
                                    asChild
                                    size="sm"
                                    className={cn(isScrolled && 'lg:hidden', 'h-8 pointer-events-auto')}>
                                    <Link to="/register" className="pointer-events-auto">
                                        <span>Sign Up</span>
                                    </Link>
                                </Button>
                                <Button
                                    asChild
                                    size="sm"
                                    className={cn(isScrolled ? 'lg:inline-flex' : 'hidden', 'pointer-events-auto')}>
                                    <Link to="/login" className="pointer-events-auto">
                                        <span>Get Started</span>
                                    </Link>
                                </Button>
                            </div>
                        </div>
                    </div>
                </div>
            </nav>
        </header>
    )
}

const Logo = ({ className }: { className?: string }) => {
    return (
        <div className={cn('flex items-center space-x-2', className)}>
            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-primary to-secondary flex items-center justify-center">
                <span className="text-primary-foreground font-bold text-lg">X</span>
            </div>
            <span className="text-xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
                XAI-Forge
            </span>
        </div>
    )
}
