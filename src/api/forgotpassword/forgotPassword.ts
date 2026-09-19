import apiClient from '../../lib/api';

export interface ForgotPasswordResponse {
  status: string;
  statusMessage: string;
  data: any;
  statusCode: number;
}

export async function forgotPassword(email: string): Promise<ForgotPasswordResponse> {
  const cleanEmail = email.trim();
  const response = await apiClient.post(
    `/api/v1/auth/forget-password?email=${encodeURIComponent(cleanEmail)}`,
    { email: cleanEmail }
  );

  const resData = response.data || {};
  const message =
    resData.statusMessage ||
    resData.message ||
    'Password reset link has been sent to your email.';

  return {
    status: 'Success',
    statusMessage: message,
    data: {
      message,
      ...(resData.data || {}),
    },
    statusCode: response.status || 200,
  };
}

export async function resetPassword(
  token: string | null,
  newPassword: string
): Promise<ForgotPasswordResponse> {
  const cleanToken = (token || '').trim();
  const cleanPassword = (newPassword || '').trim();

  const response = await apiClient.post(
    `/api/v1/auth/reset-password?token=${encodeURIComponent(cleanToken)}&newPassword=${encodeURIComponent(cleanPassword)}`,
    {
      token: cleanToken,
      newPassword: cleanPassword,
    }
  );

  const resData = response.data || {};
  const message =
    resData.statusMessage ||
    resData.message ||
    'Password has been reset successfully.';

  return {
    status: 'Success',
    statusMessage: message,
    data: resData.data || null,
    statusCode: response.status || 200,
  };
}
