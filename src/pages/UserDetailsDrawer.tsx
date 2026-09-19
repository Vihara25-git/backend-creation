import React, { useState } from 'react';
import { Button } from '../components/ui/Button';
import { FiEye, FiEyeOff } from "react-icons/fi";
import { Toast } from '../components/ui/Toast';
import AuthService from '../services/authService';

interface ChangePasswordSectionProps {
  userId?: string | number;
  email?: string;
}

const ChangePasswordSection: React.FC<ChangePasswordSectionProps> = ({ userId, email }) => {
  const [showForm, setShowForm] = useState(false);
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showOldPassword, setShowOldPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  
  // Toast state
  const [toast, setToast] = useState<{
    isOpen: boolean;
    message: string;
    type: 'success' | 'error';
  }>({
    isOpen: false,
    message: '',
    type: 'success'
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    // Validation
    if (!oldPassword || !newPassword || !confirmPassword) {
      setToast({
        isOpen: true,
        message: 'All fields are required.',
        type: 'error'
      });
      return;
    }
    if (newPassword !== confirmPassword) {
      setToast({
        isOpen: true,
        message: 'Passwords do not match.',
        type: 'error'
      });
      return;
    }

    setLoading(true);
    try {
      await AuthService.changePassword(
        oldPassword,
        newPassword,
        userId ? Number(userId) : undefined,
        email
      );
      setToast({
        isOpen: true,
        message: 'Password changed successfully!',
        type: 'success'
      });
      setShowForm(false);
      setOldPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (error: any) {
      console.error('Change password error:', error);

      let errorMessage = 'Error changing password. Please try again.';
      if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.response?.data?.statusMessage) {
        errorMessage = error.response.data.statusMessage;
      } else if (Array.isArray(error.response?.data?.data) && error.response.data.data[0]?.message) {
        errorMessage = error.response.data.data[0].message;
      } else if (error.response?.status === 400) {
        errorMessage = 'Invalid password or request. Please check your current password.';
      } else if (error.response?.status === 401) {
        errorMessage = 'Authentication failed. Please login again.';
      } else if (error.message) {
        errorMessage = error.message;
      }

      setToast({
        isOpen: true,
        message: errorMessage,
        type: 'error'
      });
    } finally {
      setLoading(false);
    }
  };

  const closeToast = () => {
    setToast(prev => ({ ...prev, isOpen: false }));
  };

  return (
    <div>
      {!showForm ? (
        <Button variant="primary" size="sm" className="w-full" onClick={() => setShowForm(true)}>
          Change Password
        </Button>
      ) : (
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="relative">
            <input
              type={showOldPassword ? "text" : "password"}
              className="w-full border rounded-lg px-3 py-2 pr-10"
              placeholder="Old Password"
              value={oldPassword}
              onChange={e => setOldPassword(e.target.value)}
              required
            />
            <button
              type="button"
              className="absolute right-3 top-1/2 -translate-y-1/2"
              onClick={() => setShowOldPassword(!showOldPassword)}
            >
              {showOldPassword ? <FiEye /> : <FiEyeOff />}
            </button>
          </div>
          
          <div className="relative">
            <input
              type={showNewPassword ? "text" : "password"}
              className="w-full border rounded-lg px-3 py-2 pr-10"
              placeholder="New Password"
              value={newPassword}
              onChange={e => setNewPassword(e.target.value)}
              required
            />
            <button
              type="button"
              className="absolute right-3 top-1/2 -translate-y-1/2"
              onClick={() => setShowNewPassword(!showNewPassword)}
            >
              {showNewPassword ? <FiEye /> : <FiEyeOff />}
            </button>
          </div>
          
          <div className="relative">
            <input
              type={showConfirmPassword ? "text" : "password"}
              className="w-full border rounded-lg px-3 py-2 pr-10"
              placeholder="Confirm New Password"
              value={confirmPassword}
              onChange={e => setConfirmPassword(e.target.value)}
              required
            />
            <button
              type="button"
              className="absolute right-3 top-1/2 -translate-y-1/2"
              onClick={() => setShowConfirmPassword(!showConfirmPassword)}
            >
              {showConfirmPassword ? <FiEye /> : <FiEyeOff />}
            </button>
          </div>
          
          <div className="flex gap-2">
            <Button type="submit" variant="primary" size="sm" disabled={loading}>
              {loading ? 'Changing...' : 'Submit'}
            </Button>
            <Button type="button" variant="ghost" size="sm" onClick={() => setShowForm(false)}>
              Cancel
            </Button>
          </div>
        </form>
      )}
      
      {}
      <Toast
        message={toast.message}
        type={toast.type}
        isOpen={toast.isOpen}
        onClose={closeToast}
        duration={3000}
      />
    </div>
  );
};

interface UserDetailsDrawerProps {
  user?: {
    firstName?: string;
    lastName?: string;
    userId?: string;
    username?: string;
    email?: string;
    role?: string;
  };
  onClose: () => void;
}

const UserDetailsDrawer: React.FC<UserDetailsDrawerProps> = ({ user, onClose }) => {
  return (
    <div className="fixed top-0 right-0 h-full w-80 bg-white shadow-2xl z-[100] animate-slideIn flex flex-col border-l border-gray-200">
      <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100">
        <h2 className="text-lg font-bold text-blue-700">User Details</h2>
        <Button variant="ghost" size="sm" className="p-2 rounded-xl" onClick={onClose}>
          <span className="text-gray-500">✕</span>
        </Button>
      </div>
      <div className="px-6 py-6 flex-1 flex flex-col gap-4">
        <div className="flex items-center gap-3">
          <div className="w-12 h-12 bg-gradient-to-r from-blue-500 to-blue-600 rounded-lg flex items-center justify-center text-white font-bold">
            {user?.firstName?.charAt(0) + '' + user?.lastName?.charAt(0)}
          </div>
          <div>
            <p className="text-xl font-semibold text-gray-900">{user?.firstName + " " + user?.lastName}</p>
            <p className="text-sm text-gray-500 capitalize">{user?.role}</p>
          </div>
        </div>
        <div className="mt-4">
          <p className="text-sm text-gray-700"><span className="font-semibold">Email:</span> {user?.email || 'N/A'}</p>
          <hr className="my-4" />
          <h3 className="text-md font-semibold text-gray-800 mb-2">Change Password</h3>
          <ChangePasswordSection userId={user?.userId} email={user?.email} />
        </div>
      </div>
    </div>
  );
};

export default UserDetailsDrawer;