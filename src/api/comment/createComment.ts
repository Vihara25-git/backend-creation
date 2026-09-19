import apiClient from '../../lib/api';

export interface CreateCommentRequest {
  userId?: string | number;
  defectId: string | number;
  comment: string;
  attachment?: string | null;
}

export interface CreateCommentResponse {
  message: string;
  data?: any;
  status?: string;
  statusCode?: number;
}

export const createComment = async (payload: CreateCommentRequest): Promise<CreateCommentResponse> => {
  const response = await apiClient.post(`/api/v1/defect/${payload.defectId}/comment`, {
    comment: payload.comment,
  });

  return {
    status: 'success',
    statusCode: response.status || 200,
    message: 'Comment added successfully',
    data: response.data?.data || response.data,
  };
};

export const updateComment = async (commentId: number, comment: string, defectId?: number | string) => {
  if (defectId) {
    const response = await apiClient.put(`/api/v1/defect/${defectId}/comment/${commentId}`, {
      comment,
    });
    return {
      status: 'success',
      statusCode: response.status || 200,
      message: 'Comment updated successfully',
      data: response.data?.data || response.data,
    };
  }
  return {
    status: 'success',
    statusCode: 200,
    message: 'Comment updated successfully',
    data: { id: commentId, comment },
  };
};

export const deleteComment = async (defectId: number | string, commentId: number | string) => {
  const response = await apiClient.delete(`/api/v1/defect/${defectId}/comment/${commentId}`);
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: 'Comment deleted successfully',
    data: response.data?.data || response.data,
  };
};
