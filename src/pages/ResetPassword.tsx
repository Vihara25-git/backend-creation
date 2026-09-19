import React, { useState } from 'react';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Lock, Eye, EyeOff } from 'lucide-react';
import { Link, useSearchParams } from 'react-router-dom';
import { resetPassword } from '../api/forgotpassword/forgotPassword';

const ResetPassword: React.FC = () => {
    const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showNew, setShowNew] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [searchParams] = useSearchParams();
const token = searchParams.get('token');
const [loading, setLoading] = useState(false);

const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  setError('');

  if (!token || !token.trim()) {
    setError('Invalid or missing reset token.');
    return;
  }

  if (newPassword.length < 8) {
    setError('Password must be at least 8 characters and contain uppercase, lowercase, number, and special character.');
    return;
  }

  if (newPassword !== confirmPassword) {
    setError('New passwords do not match');
    return;
  }

  try {
    setLoading(true);

    const response = await resetPassword(
      token,
      newPassword
    );

    if (response.status === 'Success' || response.status?.toLowerCase() === 'success' || response.statusCode === 200) {
      setSuccess(true);
      setSuccessMessage(response.statusMessage || 'Password has been reset successfully.');
    } else {
      setError(response.statusMessage || 'Failed to reset password.');
    }
  } catch (err: any) {
    let errorMsg = 'Failed to reset password.';
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
          <h1 className="text-2xl font-bold text-gray-900">Reset Password</h1>
        </div>
        {success ? (
          <div className="bg-green-50 border border-green-200 rounded-lg p-4 text-center">
            <p className="text-green-700">{successMessage}</p>
            <Link to={'/login'}><Button className='mt-3'> Go to Login</Button></Link>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-6">
            
           
            <Input
              label="New Password"
              type={showNew ? 'text' : 'password'}
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              placeholder="Enter your new password"
              required
              rightIcon={
                <button
                  type="button"
                  tabIndex={-1}
                  onClick={() => setShowNew((v) => !v)}
                  className="focus:outline-none"
                  aria-label={showNew ? 'Hide password' : 'Show password'}
                >
                  {showNew ? <Eye className="w-5 h-5" /> : <EyeOff className="w-5 h-5" />}
                </button>
              }
            />
            <Input
              label="Confirm New Password"
              type={showConfirm ? 'text' : 'password'}
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="Confirm your new password"
              required
              rightIcon={
                <button
                  type="button"
                  tabIndex={-1}
                  onClick={() => setShowConfirm((v) => !v)}
                  className="focus:outline-none"
                  aria-label={showConfirm ? 'Hide password' : 'Show password'}
                >
                  {showConfirm ? <Eye className="w-5 h-5" /> : <EyeOff className="w-5 h-5" />}
                </button>
              }
            />
            {error && (
              <div className="bg-red-50 border border-red-200 rounded-lg p-3">
                <p className="text-sm text-red-600">{error}</p>
              </div>
            )}
           <Button disabled={loading} type="submit" className="w-full">
              {loading ? 'Resetting Password...' : 'Reset Password'}
           </Button>
          </form>
        )}
      </div>
    </div>
  );
};

export default ResetPassword; 