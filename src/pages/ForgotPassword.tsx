
import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Lock } from 'lucide-react';
import { forgotPassword } from '../api/forgotpassword/forgotPassword';



const ForgotPassword: React.FC = () => {
  const [step, setStep] = useState<'email' | 'Success'>('email');
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleEmailSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    
    if (!email.match(/^[^@\s]+@[^@\s]+\.[^@\s]+$/)) {
      setError('Please enter a valid email address.');
      setLoading(false);
      return;
    }

    try {
      const response = await forgotPassword(email);
      console.log("Forgot password response:", response);
      
      if (response.status === 'Success' || response.status?.toLowerCase() === 'success' || response.statusCode === 200) {
        setStep('Success');
        setMessage(response.data?.message || response.statusMessage || 'Password reset link has been sent to your email.');
      } else {
        setError(response.statusMessage || 'Failed to send reset link. Please try again.');
      }
    } catch (err: any) {
      console.error('Forgot password error:', err);
      let errorMsg = 'Failed to send reset link. Please try again.';
      if (err.response?.data?.message) {
        errorMsg = err.response.data.message;
      } else if (err.response?.data?.statusMessage) {
        errorMsg = err.response.data.statusMessage;
      } else if (Array.isArray(err.response?.data?.data) && err.response.data.data[0]?.message) {
        errorMsg = err.response.data.data[0].message;
      } else if (err.message) {
        errorMsg = err.message;
      }
      setError(errorMsg);
    } finally {
      setLoading(false);
    }
  };



  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center px-4">
      <div className="max-w-md w-full bg-white rounded-xl shadow-lg p-8">
        <div className="text-center mb-8">
          <div className="mx-auto w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mb-4">
            <Lock className="w-8 h-8 text-blue-600" />
          </div>
          <h1 className="text-2xl font-bold text-gray-900">Forgot Password</h1>
          <p className="text-gray-600 mt-2">
            {step === 'email' && 'Enter your email to reset your password.'}
            {step === 'Success' && 'A password reset link has been sent to your email.'}
          </p>
        </div>
        {step === 'email' && (
          <form onSubmit={handleEmailSubmit} className="space-y-6">
            <Input
              label="Email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Enter your email"
              required
              disabled={loading}
            />
            {error && (
              <div className="bg-red-50 border border-red-200 rounded-lg p-3">
                <p className="text-sm text-red-600">{error}</p>
              </div>
            )}
            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? 'Processing...' : 'Send Reset Link'}
            </Button>
            
            <div className="text-center mt-4">
              <Link to="/login" className="text-blue-600 text-sm hover:underline">Back to Login</Link>
            </div>
          </form>
        )}

        {step === 'Success' && (
          <div className="bg-green-50 border border-green-200 rounded-lg p-4 text-center">
            <p className="text-green-700">{message}</p>
            <Link to="/login">
              <Button className="w-full mt-4">Go to Login</Button>
            </Link>
          </div>
        )}
      </div>
    </div>
  );
};

export default ForgotPassword;