import { useState } from "react";
import { motion } from "framer-motion";
import { Activity, Mail, Lock, Eye, EyeOff, ArrowRight } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { useLogin } from "@/hooks/useAuth";

const DEMO_CREDENTIALS = [
  { role: "Admin", email: "admin@clinic.com", password: "admin123" },
  { role: "Doctor", email: "sarahsmith@clinic.com", password: "password123" },
  { role: "Patient", email: "patient@clinic.com", password: "patient123" },
];

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);

  const { mutate: login, isPending, error } = useLogin();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    login({ email, password });
  };

  const fillDemo = (demoEmail: string, demoPassword: string) => {
    setEmail(demoEmail);
    setPassword(demoPassword);
  };

  return (
      <div className="min-h-screen flex bg-background">
        {/* Left panel */}
        <div className="hidden lg:flex lg:w-1/2 bg-sidebar relative overflow-hidden items-center justify-center p-12">
          <div className="absolute inset-0 opacity-10">
            <div className="absolute top-20 left-20 w-72 h-72 rounded-full bg-primary blur-3xl" />
            <div className="absolute bottom-20 right-20 w-96 h-96 rounded-full bg-info blur-3xl" />
          </div>
          <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6 }}
              className="relative z-10 text-center space-y-6"
          >
            <div className="flex items-center justify-center gap-3 mb-8">
              <div className="h-14 w-14 rounded-2xl bg-primary flex items-center justify-center">
                <Activity className="h-8 w-8 text-primary-foreground" />
              </div>
              <span className="text-3xl font-bold text-sidebar-primary-foreground tracking-tight">
              Clinic Portal
            </span>
            </div>
            <h1 className="text-4xl font-bold text-sidebar-foreground leading-tight">
              Better care starts
              <br />
              with better tools
            </h1>
            <p className="text-sidebar-muted text-lg max-w-md mx-auto">
              Book appointments, manage consultations, and keep your team in sync, all in one place.
            </p>
          </motion.div>
        </div>

        {/* Right panel */}
        <div className="flex-1 flex items-center justify-center p-8">
          <motion.div
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.5, delay: 0.2 }}
              className="w-full max-w-md space-y-8"
          >
            <div className="lg:hidden flex items-center gap-3 justify-center mb-4">
              <div className="h-10 w-10 rounded-xl bg-primary flex items-center justify-center">
                <Activity className="h-6 w-6 text-primary-foreground" />
              </div>
              <span className="text-2xl font-bold text-foreground">Clinic Portal</span>
            </div>

            <div>
              <h2 className="text-2xl font-bold text-foreground">Welcome back</h2>
              <p className="text-muted-foreground mt-1">
                Sign in to access your dashboard
              </p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-5">
              <div className="space-y-2">
                <label className="text-sm font-medium text-foreground">Email</label>
                <div className="relative">
                  <Mail className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                  <Input
                      type="email"
                      placeholder="Enter your email"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      className="pl-10"
                      required
                  />
                </div>
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium text-foreground">
                  Password
                </label>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                  <Input
                      type={showPassword ? "text" : "password"}
                      placeholder="Enter your password"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      className="pl-10 pr-10"
                      required
                  />
                  <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                  >
                    {showPassword ? (
                        <EyeOff className="h-4 w-4" />
                    ) : (
                        <Eye className="h-4 w-4" />
                    )}
                  </button>
                </div>
              </div>

              {error && (
                  <motion.p
                      initial={{ opacity: 0, y: -4 }}
                      animate={{ opacity: 1, y: 0 }}
                      className="text-sm text-destructive font-medium"
                  >
                    {/* Error message is shown via toast; this is a fallback inline hint */}
                    Invalid email or password.
                  </motion.p>
              )}

              <Button
                  type="submit"
                  className="w-full gap-2"
                  size="lg"
                  disabled={isPending}
              >
                {isPending ? "Signing in..." : "Sign In"}
                {!isPending && <ArrowRight className="h-4 w-4" />}
              </Button>
            </form>

            {/* Demo accounts */}
            <div className="space-y-3">
              <div className="relative">
                <div className="absolute inset-0 flex items-center">
                  <span className="w-full border-t border-border" />
                </div>
                <div className="relative flex justify-center text-xs uppercase">
                <span className="bg-background px-2 text-muted-foreground">
                  Demo Accounts
                </span>
                </div>
              </div>

              <div className="grid gap-2">
                {DEMO_CREDENTIALS.map((cred) => (
                    <button
                        key={cred.role}
                        onClick={() => fillDemo(cred.email, cred.password)}
                        className="flex items-center justify-between p-3 rounded-lg border border-border bg-card hover:bg-accent transition-colors text-left"
                    >
                      <div>
                    <span className="text-sm font-medium text-foreground">
                      {cred.role}
                    </span>
                        <span className="text-xs text-muted-foreground ml-2">
                      {cred.email}
                    </span>
                      </div>
                      <ArrowRight className="h-3.5 w-3.5 text-muted-foreground" />
                    </button>
                ))}
              </div>
            </div>
          </motion.div>
        </div>
      </div>
  );
}